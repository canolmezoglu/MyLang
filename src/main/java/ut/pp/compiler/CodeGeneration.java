package ut.pp.compiler;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import ut.pp.compiler.model.*;
import ut.pp.compiler.model.enums.*;
import ut.pp.compiler.model.VariableData;
import ut.pp.parser.MyLangBaseVisitor;
import ut.pp.parser.MyLangLexer;
import ut.pp.parser.MyLangParser;

import java.util.*;

public class CodeGeneration extends MyLangBaseVisitor<List<Instruction>> {
    /**
     * Code Generated for Sprockell program with no errors
     */
    Sprockell sp;
    ParseTreeProperty<Registers> registers;
    Register reghandler;
    ScopeTable scope;
    Result res;
    boolean threaded = false;
    Map<Integer,List<Instruction>> threads = new HashMap<>();
    Map<String,List<Instruction>> functions = new HashMap<>();
    FunctionData currentfunctionData = null;
    int currentMemoryUsage;
    HashMap<String,String> pointer_map;
    HashMap<String,Integer> var_address;
    HashMap<String,Boolean> var_global;

    /**
     * Generates Sprockell code for the String inputted.
     * @param args
     * @throws Exception
     */
    public static void main(String args[]) throws Exception {
        String code ="int a =900;\n" +
                "int b=100;\n" +
                "print(a/b);";
        MyLangLexer myLangLexer = new MyLangLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(myLangLexer);
        MyLangParser parser = new MyLangParser(tokens);
        ParseTree tree = parser.program();
        CodeGeneration c = new CodeGeneration();
        List<Instruction> instructions = c.genCode(tree);
        for (Instruction i : instructions){
            System.out.println(" ," + i.toString());
        }
    }

    /**
     * Start the tree visitor for code generation after running the Code Scanner
     * @param tree
     * @return
     * @throws Exception
     */
    public List<Instruction> genCode(ParseTree tree) throws Exception {
        sp = new Sprockell();
        scope = new ScopeTable();
        registers = new ParseTreeProperty<>();
        reghandler = new Register();
        pointer_map=new HashMap<>();
        var_address=new HashMap<>();
        var_global=new HashMap<>();
        Scanner scanner = new Scanner();
        res = scanner.check(tree);
        return this.visit(tree);
    }




    /**
     * Visits a program context.
     * @param ctx
     * @return a list of instructions
     */

    // small explanation for concurrency:
    // in the beginning, every thread except 0 sets their shared memory location to 1
    // main thread still continues with memory location 1
    // the other threads jump to their code blocks
    // on top of their code blocks, there is a test and set loop
    // the test and set loop continues until the shared memory location of
    // the thread is 0. if it is 0 , the thread changes it 1 and exits the loop.
    // when the main thread wants to spawn another thread, it sets their memory
    // location to 0.
    // for this part i'll use || to signify left and right happen concurrently.
    // the main thread then loops until its shared location is 2  || the spawned thread exits the loop as its mem location is 0 finally and sets its mem location to 2
    // the main thread goes on and starts other threads || the spawned thread executes its instructios
    // the main thread then loops until the shared location of the spawned thread is 0. || the spawned thread, after code block is executed, sets its memory location to 0
    @Override
    public List<Instruction> visitProgram(MyLangParser.ProgramContext ctx) {

        // small explanation for the shared memory:
        // 1 ... 7 ->  used to trigger threads and store shared variables
        // 0-> lock used for shared memory access

        // InstructionList is the code block that the main thread executes
        List<Instruction> InstructionList = new ArrayList<>();

        for (MyLangParser.InstructionContext context : ctx.instruction()) {
            // If the current block is a function, do not add the instructions to
            // the codebase directly.
            if (context.getStart().getText().equals("function")){
                visit(context);
            }

            // If it is not related , visit and to the instructionList.
            else{
                InstructionList.addAll(visit(context));
            }
        }
        // End the code block for the main thread.
        InstructionList.add(sp.endProgram());

        // If the given program has threads, create the jumping
        // system so each thread gets to their code block.
        if (threaded){
            Set<Integer> set_of_threads = threads.keySet();
            List<Instruction> ThreadInstructionList = new ArrayList<>();

            // The shared memory location of every thread is their Sprockell ID
            // Set the shared memory location to 1 so the thread loops until triggered.
            ThreadInstructionList.add(sp.compute(Operators.Equal, Registers.regSprID,Registers.reg0, Registers.regA));
            ThreadInstructionList.add(sp.branch(Registers.regA,new Target(Targets.Rel,3)));

            ThreadInstructionList.add(sp.loadToMemory("1",Registers.regA));
            ThreadInstructionList.add(sp.writeToMemory(Registers.regA,Registers.regSprID));

            // The jumping calculations are the done below.

            // This is the amount of lines above the main thread code block,
            // which the process has to jump over. These consist of the jump comments
            // so the processes jump to their respective code blocks in the place
            // after the main threads code block ends.

            int lines_above_main_thread = 4 * (threads.size()-1);

            // This is the lines below the main threads code block, where the
            // code blocks that the other threads execute exists. When a code block is added
            // this number has to be incremented by the amount of lines as the next thread
            // can jump over that.

            int lines_below_main_thread = 0;

            List<Integer> threadList = new ArrayList<>();
            for (Integer i : set_of_threads){


                // Test if the process executing the code has the same ID as the one
                // that needs to jump.
                ThreadInstructionList.add(sp.loadToMemory(i.toString(),Registers.regA));
                ThreadInstructionList.add(sp.compute(Operators.Sub,Registers.regA,Registers.regSprID,Registers.regA));
                ThreadInstructionList.add(sp.branch(Registers.regA,new Target(Targets.Rel,2)));

                reghandler.release(Registers.regA);

                // If the test was correct, jump to your code blocks beginning.
                // Else, execute the following lines.
                ThreadInstructionList.add(
                        sp.relJump(InstructionList.size()+lines_below_main_thread+lines_above_main_thread+1));
                lines_above_main_thread = lines_above_main_thread - 4;
                lines_below_main_thread += threads.get(i).size();

                // Add the ID's so when the actual code blocks are added in the correct
                // order. Sets do not preserve order.
                threadList.add(i);
            }
            // Add the main threads instructions after the jump instructions.
            // As the main thread's ID is not equal to the other threads, it will branch over
            // all the jumps and execute the code block.
            ThreadInstructionList.addAll(InstructionList);

            // Add the code blocks for the threads.
            for (Integer x : threadList){
                ThreadInstructionList.addAll(threads.get(x));
            }
            // If not threaded, it will just return the main threads instructions
            // and skip over all the jumps.
            Map<String, Integer> jumpLocationsForFunctions = new HashMap<>();
            for (String function_name : this.functions.keySet() ){
                jumpLocationsForFunctions.put(function_name,ThreadInstructionList.size());
                ThreadInstructionList.addAll(this.functions.get(function_name));
            }
            for (int i=0;i<ThreadInstructionList.size();i++){

                if (ThreadInstructionList.get(i).getInstr() == Instructions.Fake){
                    FakeOperator of = (FakeOperator) ThreadInstructionList.get(i).getArgs().get(0);
                    ThreadInstructionList.set(i,sp.absJump(jumpLocationsForFunctions.get(of.getFunctionName())));
                }
            }
            return ThreadInstructionList;
        }
        // If not threaded, it will just return the main threads instructions
        // and skip over all the jumps.
        Map<String, Integer> jumpLocationsForFunctions = new HashMap<>();
        for (String function_name : this.functions.keySet() ){
            jumpLocationsForFunctions.put(function_name,InstructionList.size());
            InstructionList.addAll(this.functions.get(function_name));
        }
        for (int i=0;i<InstructionList.size();i++){

            if (InstructionList.get(i).getInstr() == Instructions.Fake){
                FakeOperator of = (FakeOperator) InstructionList.get(i).getArgs().get(0);
                InstructionList.set(i,sp.absJump(jumpLocationsForFunctions.get(of.getFunctionName())));

            }
        }
        return InstructionList;
    }

    /**
     * Visit an individual statement in the program
     * @param ctx
     * @return Visitor for a statement
     */
    @Override
    public List<Instruction> visitStatementInst(MyLangParser.StatementInstContext ctx) {
        return visit(ctx.statement());
    }

    /**
     * Visit the if construct in the program
     * @param ctx
     * @return Visitor for if construct
     */
    @Override
    public List<Instruction> visitIfInst(MyLangParser.IfInstContext ctx) {
        return super.visitIfInst(ctx);
    }

    /**
     * Visit the declaration statememnt
     * @param ctx
     * @return Visitor for declaration
     */
    @Override
    public List<Instruction> visitDeclStat(MyLangParser.DeclStatContext ctx) {
        return visit(ctx.declaration());
    }

    /**
     * Visits a variable declaration, decrypts the expression and loads the
     * value inside the expression in the memory location / offset specified
     * in the Result res that the checker returns.
     * @param ctx
     * @return
     */
    @Override
    public List<Instruction> visitDeclaration(MyLangParser.DeclarationContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        String varname = ctx.ID().toString();
        if (ctx.type().BOOLEAN() != null) {
            scope.declare(varname, MyType.BOOLEAN, ctx.getStart(),ctx.access() != null && ctx.access().SHARED() != null);
        }
        if (ctx.type().INTEGER() != null) {
            scope.declare(varname, MyType.NUM, ctx.getStart(),ctx.access() != null && ctx.access().SHARED() != null);
        }
        InstructionList.addAll(visit(ctx.expr()));
        InstructionList.add(sp.pop(Registers.regA));

        var_address.put(varname,res.getOffset(ctx));
        if(res.getGlobal(ctx)!=null){
            var_global.put(varname,res.getGlobal(ctx));
        }
        else{
            var_global.put(varname,false);
        }

        // If the variable is shared, write it to the shared memory.
        if (ctx.access() != null && ctx.access().SHARED() != null){
            InstructionList.add(sp.writeToMemory(Registers.regA,res.getOffset(ctx)));
        }  //functions have different address space
        else if (this.currentfunctionData != null) {
            InstructionList.add(sp.loadToMemory(Integer.toString(res.getOffset(ctx) + 1),Registers.regB));
            InstructionList.add(sp.compute(Operators.Sub,Registers.regF,Registers.regB,Registers.regB));
            InstructionList.add(sp.storeInMemory(Registers.regA,Registers.regB));
        }
        // If the variable is not shared, write it to the local memory.

        else {
            InstructionList.add(sp.storeInMemory(varname, Registers.regA, res.getOffset(ctx)));
            currentMemoryUsage = res.getOffset(ctx) ;
        }
        return InstructionList;
    }

    /**
     * Visits every 1d array element declaration, decrypts the expression and loads the
     * value inside the expression in the memory location / offset specified
     * in the Result res that the checker returns
     * @param ctx
     * @return list of sprockell instructions
     */
    @Override
    public List<Instruction> visitDeclareArray(MyLangParser.DeclareArrayContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        String array_name = ctx.ID().toString();
        MyType type =null;
        if (ctx.type().BOOLEAN() != null) {
            type = MyType.BOOLEAN;
        }
        if (ctx.type().INTEGER() != null) {
            type = MyType.NUM;
        }
        List<MyLangParser.ExprContext> values = ctx.darray().expr();
        for(int i=0;i<values.size();i++){
            scope.declare(array_name+"%"+i, type, ctx.getStart(),ctx.access() != null && ctx.access().SHARED() != null);
            InstructionList.addAll(visit(ctx.darray().expr(i)));
            InstructionList.add(sp.pop(Registers.regA));
            var_address.put(array_name+"%"+i,res.getOffset(ctx.darray().expr(i)));
            if(res.getGlobal(ctx.darray().expr(i))!=null) {
                var_global.put(array_name + "%" + i, res.getGlobal(ctx.darray().expr(i)));
            }
            else{
                var_global.put(array_name + "%" + i,false);
            }

            if (ctx.access() != null && ctx.access().SHARED() != null){
                InstructionList.add(sp.writeToMemory(Registers.regA,res.getOffset(ctx.darray().expr(i))));
            }
            else if (this.currentfunctionData != null) {
                InstructionList.add(sp.loadToMemory(Integer.toString(res.getOffset(ctx.darray().expr(i))),Registers.regB));

                InstructionList.add(sp.compute(Operators.Sub,Registers.regF,Registers.regB,Registers.regB));
                InstructionList.add(sp.writeToMemory(Registers.regA,Registers.regB));
            }
            else {
                InstructionList.add(sp.storeInMemory(Registers.regA, res.getOffset(ctx.darray().expr(i))));
                currentMemoryUsage = res.getOffset(ctx.darray().expr(i));
            }
        }
        return InstructionList;

    }
    /**
     * Visits every 2d array element declaration, decrypts the expression and loads the
     * value inside the expression in the memory location / offset specified
     * in the Result res that the checker returns
     * @param ctx
     * @return list of sprockell instructions
     */
    @Override
    public List<Instruction> visitDeclare2dArray(MyLangParser.Declare2dArrayContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        String array_name = ctx.ID().toString();
        MyType type =null;
        if (ctx.type().BOOLEAN() != null) {
            type = MyType.BOOLEAN;
        }
        if (ctx.type().INTEGER() != null) {
            type = MyType.NUM;
        }
        List<MyLangParser.DarrayContext> rows_list = ctx.darray();
        for(int i=0;i< rows_list.size();i++){
            for(int j=0;j<rows_list.get(i).expr().size();j++){
                scope.declare(array_name+"%"+i+"%"+j, type, ctx.getStart(),ctx.access() != null && ctx.access().SHARED() != null);
                InstructionList.addAll(visit(ctx.darray(i).expr(j)));
                InstructionList.add(sp.pop(Registers.regA));
                var_address.put(array_name+"%"+i+"%"+j,res.getOffset(ctx.darray(i).expr(j)));
                if(res.getGlobal(ctx.darray(i).expr(j))!=null) {
                    var_global.put(array_name+"%"+i+"%"+j,res.getGlobal(ctx.darray(i).expr(j)));
                }
                else{
                    var_global.put(array_name+"%"+i+"%"+j,false);
                }

                if (ctx.access() != null && ctx.access().SHARED() != null){
                    InstructionList.add(sp.writeToMemory(Registers.regA,res.getOffset(ctx.darray(i).expr(j))));
                }
                else if (this.currentfunctionData != null) {
                    Registers memoryAddress = reghandler.acquire();
                    InstructionList.add(sp.loadToMemory(Integer.toString(res.getOffset(ctx.darray(i).expr(j))),memoryAddress));

                    InstructionList.add(sp.compute(Operators.Sub,Registers.regF,Registers.regB,Registers.regB));
                    InstructionList.add(sp.writeToMemory(Registers.regA,Registers.regB));
                }
                else {
                    InstructionList.add(sp.storeInMemory( Registers.regA, res.getOffset(ctx.darray(i).expr(j))));
                    currentMemoryUsage = res.getOffset(ctx.darray(i).expr(j));
                }
            }
        }
        return InstructionList;
    }
    /**
     * Visits every enum element, decrypts the expression and loads the
     * value inside the expression in the memory location / offset specified
     * in the Result res that the checker returns
     * @param ctx
     * @return list of sprockell instructions
     */
    @Override
    public List<Instruction> visitDeclareEnum(MyLangParser.DeclareEnumContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        String enum_name = ctx.ID().toString();
        MyType type =null;
        if (ctx.type().BOOLEAN() != null) {
            type = MyType.BOOLEAN;
        }
        if (ctx.type().INTEGER() != null) {
            type = MyType.NUM;
        }
        for(int i=0;i<ctx.enumAssign().ID().size();i++) {
            String enum_val_id = ctx.enumAssign().ID(0).getText();
            scope.declare(enum_name + "." + enum_val_id, type, ctx.getStart(), ctx.access() != null && ctx.access().SHARED() != null);
            InstructionList.addAll(visit(ctx.enumAssign().expr(i)));
            InstructionList.add(sp.pop(Registers.regA));
            var_address.put(enum_name + "." + enum_val_id,res.getOffset(ctx.enumAssign().expr(i)));
            if(res.getGlobal(ctx.enumAssign().expr(i))!=null){
                var_global.put(enum_name + "." + enum_val_id,res.getGlobal(ctx.enumAssign().expr(i)));
            }
            else{
                var_global.put(enum_name + "." + enum_val_id,false);
            }

            if (ctx.access() != null && ctx.access().SHARED() != null) {
                InstructionList.add(sp.writeToMemory(Registers.regA, res.getOffset(ctx.enumAssign().expr(i))));
            }
            else if (this.currentfunctionData != null) {
                InstructionList.add(sp.loadToMemory(Integer.toString(res.getOffset(ctx.enumAssign().expr(i))),Registers.regB));
                InstructionList.add(sp.compute(Operators.Sub,Registers.regF,Registers.regB,Registers.regB));
                InstructionList.add(sp.writeToMemory(Registers.regA,Registers.regB));
            }
            else {
                InstructionList.add(sp.storeInMemory(Registers.regA, res.getOffset(ctx.enumAssign().expr(i))));
                currentMemoryUsage = res.getOffset(ctx.enumAssign().expr(i));
            }
        }
        return InstructionList;
    }
    /**
     * Visits every pointer declaration, decrypts the expression and loads the
     * value inside the expression in the memory location / offset specified,
     * and stores the address of every identifier it points to
     * @param ctx
     * @return list of sprockell instructions
     */
    @Override
    public List<Instruction> visitDeclarePointer(MyLangParser.DeclarePointerContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        String pointer_name = ctx.ID().getText();
        String varname = ctx.factor().getText();
        pointer_map.put(pointer_name,varname);
        // gets the address of the value its pointing to
        InstructionList.add(sp.loadToMemory(Integer.toString(res.getOffset(ctx.factor())),Registers.regA));
        // stores that address in itself
        InstructionList.add(sp.storeInMemory(Registers.regA,res.getOffset(ctx)));
        currentMemoryUsage = res.getOffset(ctx) ;
        return InstructionList;
    }

    /**
     * Visit every Change statement
     * @param ctx
     * @return Visitor for Change statement
     */
    @Override
    public List<Instruction> visitChangeStat(MyLangParser.ChangeStatContext ctx) {
        return visit(ctx.changeAss());
    }
    /**
     * Visit every Lock statement
     * @param ctx
     * @return Visitor for Lock statement
     */
    @Override
    public List<Instruction> visitLockInst(MyLangParser.LockInstContext ctx){
        return visit(ctx.lockConstruct());
    }

    /**
     * Implements basic spinlock from https://en.wikipedia.org/wiki/Test-and-set
     * Adds the locking instructions first, then adds the instructions inside, then adds unlock statement.
     * Uses the 7th memory address, therefore that address cannot be used for anything else.
     * @param ctx
     * @return
     */
    @Override
    public List<Instruction> visitLockConstruct(MyLangParser.LockConstructContext ctx){
        List<Instruction> InstructionList = new ArrayList<>();

        // Waits/loops/spins until it receives the lock.
        InstructionList.add(sp.testAndSet(0));
        InstructionList.add(sp.receive(Registers.regA));
        InstructionList.add(sp.compute(Operators.Equal, Registers.regA,Registers.reg0, Registers.regA));
        InstructionList.add(sp.branch(Registers.regA,new Target(Targets.Rel,-3)));


        // Adds the instructions
        InstructionList.addAll(visit(ctx.block()));
        // Frees up the lock.
        InstructionList.add(sp.writeToMemory(Registers.reg0,0));
        return InstructionList;

    }
    /**
     * Visits a variable change assignments, decrypts the expression and loads the
     * value inside the expression in the memory location / offset specified
     * in the Result res that the checker returns.
     * @param ctx
     * @return
     */
    @Override
    public List<Instruction> visitChangeAss(MyLangParser.ChangeAssContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        String child = ctx.ID().toString();
        InstructionList.addAll(visit(ctx.expr()));
        InstructionList.add(sp.pop(Registers.regA));

        // If the variable is shared, write it to the shared memory.
        int offset =0;
        Boolean global=false;

        if(child.contains("&")){// this is a pointer
            child = pointer_map.get(child.substring(0,child.length()-1));
            offset = var_address.get(child);
            global = var_global.get(child);
        }
        else if(this.res.getDynamicArrayCall(ctx) !=null){
            InstructionList.addAll(this.res.getDynamicArrayCall(ctx).getChangeInstructions(res.getGlobal(ctx)));
            return InstructionList;
        }
        else{
            offset = res.getOffset(ctx);
            global = res.getGlobal(ctx);
        }

        if (global){
            InstructionList.add(sp.writeToMemory(Registers.regA,offset));
        }
        else if (this.currentfunctionData != null) {
            if (this.currentfunctionData.getVariable(child).isParameter()){
                InstructionList.add(sp.loadToMemory(Integer.toString(offset + 1),Registers.regB));
                InstructionList.add(sp.compute(Operators.Add,Registers.regF,Registers.regB,Registers.regB));
                if (this.currentfunctionData.getVariable(child).isPointer()) {
                    InstructionList.add(sp.getFromIndAddr(Registers.regB, Registers.regB));
                }

            }
            else {
                InstructionList.add(sp.loadToMemory(Integer.toString(offset +1), Registers.regB));
                InstructionList.add(sp.compute(Operators.Sub, Registers.regF, Registers.regB, Registers.regB));
                if (this.currentfunctionData.getVariable(child).isPointer()) {
                    InstructionList.add(sp.getFromIndAddr(Registers.regB, Registers.regB));
                }
            }
            InstructionList.add(sp.storeInMemory(Registers.regA,Registers.regB));

        }
        else{
            InstructionList.add(sp.storeInMemory(child, Registers.regA, offset));


        }
        return InstructionList;
    }

    /**
     * Code generation for a superior expression
     * @param ctx
     * @return list of sprockell instructions
     */
    @Override
    public List<Instruction> visitSuperiorExpr(MyLangParser.SuperiorExprContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        List<Instruction> expr0 = visit(ctx.term());
        if (ctx.superiorExpr() == null){
            InstructionList.addAll(expr0);
        }
        else {
            List<Instruction> expr1 = visit(ctx.superiorExpr());
            InstructionList.addAll(expr1);
            InstructionList.addAll(expr0);
            InstructionList.add(sp.pop(Registers.regB));
            InstructionList.add(sp.pop(Registers.regA));
            if (ctx.addOp() != null) {
                if (ctx.addOp().PLUS()!= null) {
                    InstructionList.add(sp.compute(Operators.Add, Registers.regA, Registers.regB, Registers.regA));
                } else if (ctx.addOp().MINUS() != null) {
                    InstructionList.add(sp.compute(Operators.Sub, Registers.regA, Registers.regB, Registers.regA));
                }
            } else {
                InstructionList.add(sp.compute(Operators.Or, Registers.regA, Registers.regB, Registers.regA));
            }
            InstructionList.add(sp.push(Registers.regA));
        }
        return InstructionList;
    }

    /**
     * Code generation for a primitive factor
     * @param ctx
     * @return list of sprockell instructions
     */
    @Override
    public List<Instruction> visitPrimitiveFactor(MyLangParser.PrimitiveFactorContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        InstructionList.addAll(visitPrimitive(ctx.primitive()));
        return InstructionList;

    }

    /**
     * Code generation for a primitive value
     * @param ctx
     * @return list of sprockell instructions
     */
    @Override
    public List<Instruction> visitPrimitive(MyLangParser.PrimitiveContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        String child = ctx.NUM() !=null ? ctx.NUM().getText() : ctx.booleanVal().getText();
        InstructionList.add(sp.loadToMemory(child,Registers.regA));
        InstructionList.add(sp.push(Registers.regA));
        return InstructionList;
    }

    /**
     * Code generation for an Identifier
     * @param ctx
     * @return list of sprockell instructions
     */
    @Override
    public List<Instruction> visitIdFactor(MyLangParser.IdFactorContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        String child = ctx.children.get(0).toString();

        int offset =0;
        Boolean global=false;

        if(child.contains("&")){// this is a pointer
            child = pointer_map.get(child.substring(0,child.length()-1));
            offset = var_address.get(child);
            global = var_global.get(child);
        }
        else if(this.res.getDynamicArrayCall(ctx) !=null){
            InstructionList.addAll(this.res.getDynamicArrayCall(ctx).getIDCallInstructions(res.getGlobal(ctx)));
            return InstructionList;
        }
        else{
            offset = res.getOffset(ctx);
            global = res.getGlobal(ctx);
        }

        // if variable is shared
        if (global){
            InstructionList.add(sp.readInst(offset));
            InstructionList.add(sp.receive(Registers.regA));
            InstructionList.add(sp.push(Registers.regA));


        }
        // if we're inside a function
        else if(this.currentfunctionData !=null){
            VariableData variableData = this.currentfunctionData.getVariable(child);
            if (variableData.isParameter()){
                // parameter is at the bottom of arp
                InstructionList.add(sp.loadToMemory(Integer.toString(offset + 1),Registers.regB));
                InstructionList.add(sp.compute(Operators.Add,Registers.regF,Registers.regB,Registers.regB));
                if (variableData.isPointer()){
                    InstructionList.add(sp.getFromIndAddr(Registers.regB,Registers.regB));
                }
            }
            else{
                InstructionList.add(sp.loadToMemory(Integer.toString(offset +1  ),Registers.regB));
                InstructionList.add(sp.compute(Operators.Sub,Registers.regF,Registers.regB,Registers.regB));
                if (variableData.isPointer()){
                    InstructionList.add(sp.getFromIndAddr(Registers.regB,Registers.regB));
                }

            }
            InstructionList.add(sp.getFromIndAddr(Registers.regA,Registers.regB));
            InstructionList.add(sp.push(Registers.regA));


        }
        // if we're in main thread
        else {
            InstructionList.add(sp.loadToMemory(Registers.regA,offset));
            InstructionList.add(sp.push(Registers.regA));
        }
        return InstructionList;

    }

    /**
     * Visit all parallel constructs in the program
     * @param ctx
     * @return Visitors for all parallel constructs
     */
    @Override public List<Instruction> visitParallelConstruct(MyLangParser.ParallelConstructContext ctx){
        for (MyLangParser.ThreadConstructContext threadConstructContext:
             ctx.threadConstruct()) {
            visit(threadConstructContext);
        }
        return null;
    }

    /**
     * Code Generation for a print statement
     * @param ctx
     * @return list of sprockell instructions
     */
    @Override
    public List<Instruction> visitPrintConstruct(MyLangParser.PrintConstructContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        InstructionList.addAll(visit(ctx.expr()));
        InstructionList.add(sp.pop(Registers.regA));
        InstructionList.add(sp.writeToIO(Registers.regA));
        return InstructionList;
    }

    /**
     * Code generation for a Term used in the program
     * @param ctx
     * @return list of sprockell instructions
     */
    @Override
    public List<Instruction> visitTerm(MyLangParser.TermContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        List<Instruction> expr0 = visit(ctx.factor());
        if (ctx.term() ==null){
            InstructionList.addAll(expr0);
        }
        else {
            List<Instruction> expr1 = visit(ctx.term());
            InstructionList.addAll(expr0);
            InstructionList.addAll(expr1);
            InstructionList.add(sp.pop(Registers.regA));
            InstructionList.add(sp.pop(Registers.regB));
            if (ctx.mult() != null) {
                if(ctx.mult().DIV()!=null){
                    // check if first operand is negative, if so make positive
                    InstructionList.add(sp.compute(Operators.GtE,Registers.regA,Registers.reg0,Registers.regC));
                    InstructionList.add(sp.branch(Registers.regC,new Target(Targets.Rel,2)));
                    InstructionList.add(sp.compute(Operators.Sub,Registers.reg0,Registers.regA,Registers.regA));
                    // check if second operand is negative, if so make positive

                    InstructionList.add(sp.compute(Operators.GtE,Registers.regB,Registers.reg0,Registers.regD));
                    InstructionList.add(sp.branch(Registers.regD,new Target(Targets.Rel,2)));
                    InstructionList.add(sp.compute(Operators.Sub,Registers.reg0,Registers.regB,Registers.regB));
                    // if only one of those numbers were negative, store 1 in regE
                    InstructionList.add(sp.compute(Operators.Xor,Registers.regC,Registers.regD,Registers.regE));
                    // division by subtraction
                    InstructionList.add(sp.compute(Operators.Add,Registers.reg0,Registers.reg0,Registers.regC));
                    InstructionList.add(sp.compute(Operators.GtE, Registers.regA, Registers.regB, Registers.regD));
                    InstructionList.add(sp.compute(Operators.Equal,Registers.regD,Registers.reg0,Registers.regD));
                    InstructionList.add(sp.branch(Registers.regD,new Target(Targets.Rel,4)));
                    InstructionList.add(sp.compute(Operators.Sub,Registers.regA,Registers.regB,Registers.regA));
                    InstructionList.add(sp.compute(Operators.Incr, Registers.regC, Registers.regC, Registers.regC));
                    InstructionList.add(sp.relJump(-5));
                    InstructionList.add(sp.compute(Operators.Add, Registers.regC, Registers.reg0, Registers.regA));
                    // if only one was negative, make the number negative
                    InstructionList.add(sp.compute(Operators.Equal,Registers.regE,Registers.reg0,Registers.regE));
                    InstructionList.add(sp.branch(Registers.regE,new Target(Targets.Rel,2)));
                    InstructionList.add(sp.compute(Operators.Sub, Registers.reg0, Registers.regA, Registers.regA));

                }
                else {
                    InstructionList.add(sp.compute(Operators.Mul, Registers.regA, Registers.regB, Registers.regA));
                }
            } else {
                InstructionList.add(sp.compute(Operators.And, Registers.regA, Registers.regB, Registers.regA));
            }
            InstructionList.add(sp.push(Registers.regA));
        }
        return InstructionList;
    }

    /**
     * Visits a thread construct, adds the lock on top of the code block
     * for the thread so the thread starts up when the main thread lets it start.
     * Adds the instructions in the "threads" Hashmap.
     * @param ctx
     * @return nothing as the instructions are not added to main threads code block.
     */
    @Override public List<Instruction> visitThreadConstruct(MyLangParser.ThreadConstructContext ctx){
        List<Instruction> InstructionList = new ArrayList<>();

        // Loop until main thread releases this thread's shared memory location.

        InstructionList.add(sp.testAndSet(res.getThread(ctx).getThreadnr()));
        InstructionList.add(sp.receive(Registers.regA));
        InstructionList.add(sp.compute(Operators.Equal, Registers.regA,Registers.reg0, Registers.regA));
        InstructionList.add(sp.branch(Registers.regA,new Target(Targets.Rel,-3)));



        // Allow the main thread to go on by writing 0 to its shared location.
        InstructionList.add(sp.loadToMemory("2",Registers.regA));
        InstructionList.add(sp.writeToMemory(Registers.regA,res.getThread(ctx).getThreadnr()));
        reghandler.release(Registers.regA);


        InstructionList.addAll(visit(ctx.block()));

        // Let the main thread know that it has finished up
        // executing by writing 0 to its shared memory location.
        InstructionList.add(sp.writeToMemory(Registers.reg0,res.getThread(ctx).getThreadnr()));
        InstructionList.add(sp.endProgram());
        threads.put(res.getThread(ctx).getThreadnr(),InstructionList);
        return null;


    }

    /**
     * Code generation for an expression in the program
     * @param ctx
     * @return list of sprockell instructions
     */
    @Override
    public List<Instruction> visitExpr(MyLangParser.ExprContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        List<Instruction> expr0 = visit(ctx.superiorExpr(0));
        if (ctx.superiorExpr().size() ==1){
            InstructionList.addAll(expr0);
        }
        else {
            List<Instruction> expr1 = visit(ctx.superiorExpr(1));
            InstructionList.addAll(expr0);
            InstructionList.addAll(expr1);
            InstructionList.add(sp.pop(Registers.regB));
            InstructionList.add(sp.pop(Registers.regA));
            switch (ctx.getChild(1).getText()) {
                case "==":
                    InstructionList.add(sp.compute(Operators.Equal, Registers.regA, Registers.regB, Registers.regA));

                    break;
                case "!=":
                    InstructionList.add(sp.compute(Operators.NEq, Registers.regA, Registers.regB, Registers.regA));

                    break;
                case ">":
                    InstructionList.add(sp.compute(Operators.Gt, Registers.regA, Registers.regB, Registers.regA));

                    break;
                case "<":
                    InstructionList.add(sp.compute(Operators.Lt, Registers.regA, Registers.regB, Registers.regA));

                    break;
                case ">=":
                    InstructionList.add(sp.compute(Operators.GtE, Registers.regA, Registers.regB, Registers.regA));

                    break;
                case "<=":
                    InstructionList.add(sp.compute(Operators.LtE, Registers.regA, Registers.regB, Registers.regA));

                    break;

            }
            InstructionList.add(sp.push(Registers.regA));

        }
        return InstructionList;
    }


    /**
     * Code generation for an expression within parenthesis
     * @param ctx
     * @return list of sprockell instructions
     */
    @Override
    public List<Instruction> visitParFactor(MyLangParser.ParFactorContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        InstructionList.addAll(visit(ctx.expr()));
        return InstructionList;
    }

    /**
     * Code generation for a Prefix operator
     * @param ctx
     * @return list of sprockell instructions
     */
    @Override
    public List<Instruction> visitPrefixFactor(MyLangParser.PrefixFactorContext ctx){
        List<Instruction> InstructionList = new ArrayList<>();
        InstructionList.addAll(visit(ctx.factor()));
        InstructionList.add(sp.pop(Registers.regA));
        if (ctx.prefixOp().NOT() != null){
            InstructionList.add(sp.loadToMemory("true",Registers.regB));
            InstructionList.add(sp.compute(Operators.Xor,Registers.regA,Registers.regB,Registers.regA));
        }
        else if (ctx.prefixOp().MINUS() != null){
            InstructionList.add(sp.compute(Operators.Sub,Registers.reg0,Registers.regA,Registers.regA));
        }
        InstructionList.add(sp.push(Registers.regA));

        return InstructionList;


    }

    /**
     * Visits an if construct. Since the visit expressions does not
     * directly add any code blocks and only return instruction lists ,
     * the order in which the instructions are added is changed.
     * At first the expression is added, then the blocks are visited and
     * instructions are obtained but not added to the InstructionList.
     * Jump instructions are created based on the sizes of the blocks,
     * and then blocks are added.
     * @param ctx
     * @return
     */
    @Override
    public List<Instruction> visitIfConstruct(MyLangParser.IfConstructContext ctx) {
        scope.openScope();
        List<Instruction> InstructionList = new ArrayList<>();
        // Visit the expr that controls the main flow.
        InstructionList.addAll(visit(ctx.expr()));
        InstructionList.add(sp.pop(Registers.regA));
        // Visit the blocks but do not add them.
        List<Instruction> firstBlock = visit(ctx.block(0));
        List<Instruction> secondBlock = ctx.block().size() > 1 ? visit(ctx.block(1)) : new ArrayList<>();
        // Create the jump based on the sizes of the blocks.
        InstructionList.add(sp.branch(Registers.regA,new Target(Targets.Rel,2)));
        InstructionList.add(sp.relJump(firstBlock.size()+2));
        InstructionList.addAll(firstBlock);
        InstructionList.add(sp.relJump(secondBlock.size()+1));
        InstructionList.addAll(secondBlock);

        scope.closeScope();
        return InstructionList;
    }

    /**
     * Visits a while construct, the blocks are first obtained but added after the jump instructions.
     * @param ctx
     * @return
     */
    @Override
    public List<Instruction> visitWhileConstruct(MyLangParser.WhileConstructContext ctx){
        scope.openScope();
        List<Instruction> InstructionList = new ArrayList<>();
        InstructionList.addAll(visit(ctx.expr()));
        InstructionList.add(sp.pop(Registers.regA));
        int exprsize = InstructionList.size();
        List<Instruction> block = visit(ctx.block());
        // Jump out the block if while loop is false
        InstructionList.add(sp.branch(Registers.regA,new Target(Targets.Rel,2)));
        InstructionList.add(sp.relJump(block.size()+2));

        InstructionList.addAll(block);

        InstructionList.add(sp.relJump(-1 * (block.size() + exprsize + 2)));
        scope.closeScope();
        return InstructionList;
    }

    /**
     * Visit a block inside an if, while etc and code generation
     * @param ctx
     * @return list of sprockell instructions
     */
    @Override  public  List<Instruction> visitBlock(MyLangParser.BlockContext ctx){
        List<Instruction> InstructionList = new ArrayList<>();
        for (MyLangParser.InstructionContext context : ctx.instruction()) {
            InstructionList.addAll(visit(context));
        }
        return InstructionList;
    }

    /**
     * Visit the return construct
     * @param ctx
     * @return Visitor for the return construct
     */
    @Override public List<Instruction> visitReturnInst(MyLangParser.ReturnInstContext ctx){
        return visit(ctx.returnConstruct());
    }

    /**
     * Visits a run procedure
     * @param ctx
     * @return
     */
    @Override public List<Instruction> visitRunProInst(MyLangParser.RunProInstContext ctx){
        return visit(ctx.runProcedureConstruct());
    }
    /**
     * Visits a run procedure.
     * Does not need to do anything extra
     * as the type checking is done inside scanner.
     * @param ctx
     * @return
     */
    @Override public List<Instruction> visitRunProcedureConstruct(MyLangParser.RunProcedureConstructContext ctx){
        return visit(ctx.factor());
    }

    /**
     *  Visits a return construct inside a function.
     *  Gets the jump location from ARP + 1 and jumps there.
     * @param ctx
     * @return
     */
    @Override public List<Instruction> visitReturnConstruct(MyLangParser.ReturnConstructContext ctx){
        List<Instruction> InstructionList = new ArrayList<>();
        if (ctx.expr() !=null) {
            InstructionList.addAll(visit(ctx.expr()));
        }
        InstructionList.add(sp.loadToMemory("1",Registers.regB));
        InstructionList.add(sp.compute(Operators.Add,Registers.regB,Registers.regF,Registers.regB));
        InstructionList.add(sp.getFromIndAddr(Registers.regB,Registers.regB));
        InstructionList.add(sp.IndJump(Registers.regB));
        return InstructionList;
    }
    @Override public List<Instruction> visitFunctionInst(MyLangParser.FunctionInstContext ctx){
        return visit(ctx.functionConstruct());
    }

    /**
     * Visits a function construct.
     * Changes function data so the
     * code generator can generate instructions for being inside
     * a function, such as having different memory locations.
     * @param ctx
     * @return
     */
    @Override public List<Instruction> visitFunctionConstruct(MyLangParser.FunctionConstructContext ctx){
        List<Instruction> InstructionList = new ArrayList<>();
        this.currentfunctionData = res.getFunctionData(ctx.ID(0).toString());
        InstructionList.addAll(visit(ctx.block()));
        // if function does not have a return, add the jump in the end
        if (!this.currentfunctionData.isLastLineHasReturn()) {
            InstructionList.add(sp.loadToMemory("1", Registers.regB));
            InstructionList.add(sp.compute(Operators.Add, Registers.regB, Registers.regF, Registers.regB));
            InstructionList.add(sp.getFromIndAddr(Registers.regB, Registers.regB));
            InstructionList.add(sp.IndJump(Registers.regB));
        }
        this.functions.put(ctx.ID(0).toString(),InstructionList);
        this.currentfunctionData = null;
        return null;
    }

    /**
     * Visits a parallel instruction and creates
     * thread spawning instructions.
     * @param context
     * @return
     */
    @Override public List<Instruction> visitParallelInst(MyLangParser.ParallelInstContext context){
        threaded = true;
        List<Instruction> InstructionList = new ArrayList<>();

        // This for loop adds instructions for the main thread to spawn the threads in a parallel block
        for (Integer thread_number:
                res.getChildren(context)) {
            // Inform the child thread that they can start
            InstructionList.add(sp.writeToMemory(Registers.reg0,thread_number));
            // Wait for the child thread to start ( the child thread will set memory addr 0 to be 1)
            InstructionList.add(sp.readInst(thread_number));
            InstructionList.add(sp.receive(Registers.regA));
            InstructionList.add(sp.loadToMemory("2",Registers.regB));
            InstructionList.add(sp.compute(Operators.Sub,Registers.regB,Registers.regA,Registers.regB));
            InstructionList.add(sp.branch(Registers.regA,new Target(Targets.Rel,-4)));



        }

        // This for loop adds instructions for the main thread to wait for the spawned threads to end
        for (Integer thread_number:
                res.getChildren(context)) {
            if (!res.getChildren(context).contains(thread_number)){
                continue;
            }
            // Wait until the spawned thread to set it's share memory location to 0
            // by testing if it is 0 or not
            InstructionList.add(sp.testAndSet(thread_number));
            InstructionList.add(sp.receive(Registers.regA));
            InstructionList.add(sp.compute(Operators.Equal, Registers.regA,Registers.reg0, Registers.regA));
            InstructionList.add(sp.branch(Registers.regA,new Target(Targets.Rel,-3)));

        }
        // Visit the code blocks for the thread
        // the visited code block is not added to the instructionList,
        // therefore it will not be executed by the main thread
        // the instructions that the thread will execute will be added
        // as a key-value (thread number -> list of instructions)
        // pair in the "threads" hashMap.
        visit(context.parallelConstruct());
        return InstructionList;
    }

    /**
     * Visits a function call and
     * creates dynamic memory allocation
     * for recursion using ARP's.
     * @param ctx
     * @return
     */
    @Override public List<Instruction> visitFuncCall(MyLangParser.FuncCallContext ctx){
        List<Instruction> InstructionList = new ArrayList<>();
        // if currently inside a function
        if (this.currentfunctionData !=null){
            FunctionData calledFunction = res.getFunctionData(ctx.ID().toString());
            // current memory usage 2 + parameters
            int arpAdd =  1 + currentfunctionData.getParameters().size() + calledFunction.getLocalDataSize() + 1;
            // visit all the parameters
            for (int i=0;i<ctx.expr().size();i++){
                InstructionList.addAll(visit(ctx.expr(i)));
                InstructionList.add(sp.pop(Registers.regA));
                // add the arp add to the regF but do not update the regF yet so variables from the previous
                // scope can be parsed.
                InstructionList.add(sp.loadToMemory(Integer.toString(arpAdd +i + 1 + 1+ 1),Registers.regB));
                InstructionList.add(sp.compute(Operators.Add,Registers.regF,Registers.regB,Registers.regB));
                InstructionList.add(sp.storeInMemory(Registers.regA,Registers.regB));
            }
            // store callee arp - 1 in regA, store caller ARP, which is
            // regF into IndAddr regA, which is arp - 1
            InstructionList.add(sp.loadToMemory(Integer.toString(arpAdd),Registers.regA));
            InstructionList.add(sp.compute(Operators.Add,Registers.regF,Registers.regA,Registers.regA));
            InstructionList.add(sp.storeInMemory(Registers.regF,Registers.regA));

            // set the arp correctly
            InstructionList.add(sp.compute(Operators.Incr,Registers.regA,Registers.regA,Registers.regA));
            InstructionList.add(sp.compute(Operators.Add,Registers.regA,Registers.reg0,Registers.regF));
            // store arp + 1 in regA for return address
            InstructionList.add(sp.loadToMemory("1",Registers.regA));
            InstructionList.add(sp.compute(Operators.Add,Registers.regF,Registers.regA,Registers.regA));
            // current pc counter + 3 will be the line to get back.
            InstructionList.add(sp.loadToMemory("3",Registers.regB));
            InstructionList.add(sp.compute(Operators.Add,Registers.regPC,Registers.regB,Registers.regB));
            InstructionList.add(sp.storeInMemory(Registers.regB,Registers.regA));
            // jump instruction to the callee is added here
            InstructionList.add(sp.fakeInst(ctx.ID().toString()));

            //reset arp to before
            InstructionList.add(sp.loadToMemory("1",Registers.regA));
            InstructionList.add(sp.compute(Operators.Sub,Registers.regF,Registers.regA,Registers.regA));
            InstructionList.add(sp.getFromIndAddr(Registers.regA,Registers.regA));
            InstructionList.add(sp.compute(Operators.Add,Registers.regA,Registers.reg0,Registers.regF));



            return InstructionList;

        }
        // if we're in main thread dont need dynamic memory
        else {

            FunctionData calledFunction = res.getFunctionData(ctx.ID().toString());
            // current mem  + local storage + caller arp (empty for here)
            int arpLocation = currentMemoryUsage +  calledFunction.getLocalDataSize() + 1 + 1 ;
            // go over the parameters
            for (int i=0;i<ctx.expr().size();i++){
                InstructionList.addAll(visit(ctx.expr(i)));
                InstructionList.add(sp.pop(Registers.regA));
                InstructionList.add(sp.loadToMemory(Integer.toString(arpLocation+1 +i + 1),Registers.regB));
                InstructionList.add(sp.storeInMemory(Registers.regA,Registers.regB));
            }

            // set arp to regF
            InstructionList.add(sp.loadToMemory(Integer.toString(arpLocation),Registers.regF));

            // put the return address in arp + 1
            InstructionList.add(sp.loadToMemory("1",Registers.regA));
            InstructionList.add(sp.compute(Operators.Add,Registers.regF,Registers.regA,Registers.regA));
            InstructionList.add(sp.loadToMemory("3",Registers.regB));
            InstructionList.add(sp.compute(Operators.Add,Registers.regPC,Registers.regB,Registers.regB));
            InstructionList.add(sp.storeInMemory(Registers.regB,Registers.regA));
            // jump to the callee
            InstructionList.add(sp.fakeInst(ctx.ID().toString()));


           return InstructionList;








        }
    }

}


