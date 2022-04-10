package ut.pp.elaboration;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import ut.pp.elaboration.model.*;
import ut.pp.elaboration.model.enums.*;
import ut.pp.elaboration.model.interfaces.InstructionArgs;
import ut.pp.parser.MyLangBaseVisitor;
import ut.pp.parser.MyLangLexer;
import ut.pp.parser.MyLangParser;

import java.util.*;
import java.util.function.Function;

public class CodeGen extends MyLangBaseVisitor<List<Instruction>> {


    Sprockell sp;
    ParseTreeProperty<Registers> registers;
    Register reghandler;
    ScopeTable scope;
    Result res;
    Map<Integer,List<Instruction>> threads = new HashMap<>();
    Map<String,List<Instruction>> functions = new HashMap<>();
    FunctionData currentfunctionData = null;
    int currentMemoryUsage;

    public static void main(String args[]) throws Exception {
        String code =
                "function int addfive (int a) {" +
                        "return a+5; } " +
                "print (addfive(5););";
        MyLangLexer myLangLexer = new MyLangLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(myLangLexer);
        MyLangParser parser = new MyLangParser(tokens);
        ParseTree tree = parser.program();
        CodeGen c = new CodeGen();
        List<Instruction> instructions = c.genCode(tree);
        for (Instruction i : instructions){
            System.out.println(" ," + i.toString());
        }

    }


    public List<Instruction> genCode(ParseTree tree) throws Exception {
        sp = new Sprockell();
        scope = new ScopeTable();
        registers = new ParseTreeProperty<>();
        reghandler = new Register();
        Checker checker = new Checker();
        res = checker.check(tree);
        return this.visit(tree);
    }

    /**
     * Loads an expression to a register and returns it
     * Invokes the reghandler to acquire an empty register,
     * therefore, the returned register should be released
     * @return register
     */
    private Registers getRegister(MyLangParser.ExprContext expr) {
        return registers.get(expr);

    }

    /**
     * Visits a program context.
     * @param ctx
     * @return a list of instructions
     */

    // small explanation for concurrency:
    // in the beginning, every thread their shared memory location to 1
    // main thread still continues with memory location 1
    // the other threads jump to their code blocks
    // on top of their code blocks, there is a test and set loop
    // the test and set loop continues until the shared memory location of
    // the thread is 0. if it is 0 , the thread changes it 1 and exits the loop.
    // when the main thread wants to spawn another thread, it sets their memory
    // location to 0.
    // for this part i'll use || to signify left and right happen concurrently.
    // the main thread then loops until its shared location is 0 again.  || the spawned thread exits the loop as its mem location is open
    // the main thread, loops until its shared location is 0, when it is , sets it to 1. (test and set) || the spawned thread sets the main threads memory to 0 and executes its code block
    // the main thread then loops until the shared location of the spawned thread is 0. || the spawned thread, after code block is executed, sets its memory location to 0
    @Override
    public List<Instruction> visitProgram(MyLangParser.ProgramContext ctx) {
        boolean threaded = false;

        // small explanation for the shared memory:
        // 0 -> lock used for blocking the main thread
        // 1 ... 6 ->  used to trigger threads and store shared variables
        // 7 -> lock used for shared memory access

        // InstructionList is the code block that the main thread executes
        List<Instruction> InstructionList = new ArrayList<>();

        for (MyLangParser.InstructionContext context : ctx.instruction()) {
            // If the current block is parallel,
            // add the thread starting and stopping codes
            if (context.getStart().getText().equals("function")){
                visit(context);
            }
            else if (context.getStart().getText().equals("parallel")){
                threaded = true;

                // This for loop adds instructions for the main thread to spawn the threads in a parallel block
                for (Integer thread_number:
                     res.getChildren(context)) {
                    Registers reg = reghandler.acquire();
                    // Inform the child thread that they can start
                    InstructionList.add(sp.writeToMemory(Registers.reg0,thread_number));
                    // Wait for the child thread to start ( the child thread will set memory addr 0 to be 1)
                    InstructionList.add(sp.testAndSet(0));
                    InstructionList.add(sp.receive(reg));
                    InstructionList.add(sp.compute(Operators.Equal, reg,Registers.reg0, reg));
                    InstructionList.add(sp.branch(reg,new Target(Targets.Rel,-3)));

                    reghandler.release(reg);
                }

                // This for loop adds instructions for the main thread to wait for the spawned threads to end
                for (Integer thread_number:
                        res.getChildren(context)) {
                    if (!res.getChildren(context).contains(thread_number)){
                        continue;
                    }
                    Registers reg = reghandler.acquire();
                    // Wait until the spawned thread to set it's share memory location to 0
                    // by testing if it is 0 or not
                    InstructionList.add(sp.testAndSet(thread_number));
                    InstructionList.add(sp.receive(reg));
                    InstructionList.add(sp.compute(Operators.Equal, reg,Registers.reg0, reg));
                    InstructionList.add(sp.branch(reg,new Target(Targets.Rel,-3)));

                    reghandler.release(reg);
                }
                // Visit the code blocks for the thread
                // the visited code block is not added to the instructionList,
                // therefore it will not be executed by the main thread
                // the instructions that the thread will execute will be added
                // as a key-value (thread number -> list of instructions)
                // pair in the "threads" hashMap.
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
            Registers reg = reghandler.acquire();

            // The shared memory location of every thread is their Sprockell ID
            // Set the shared memory location to 1 so the thread loops until triggered.

            ThreadInstructionList.add(sp.loadToRegister("1",0,reg,0));
            ThreadInstructionList.add(sp.writeToMemory(reg,Registers.regSprID));

            reghandler.release(reg);
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

                Registers reg2 = reghandler.acquire();

                // Test if the process executing the code has the same ID as the one
                // that needs to jump.

                ThreadInstructionList.add(sp.loadToRegister(i.toString(),0,reg2,0));
                ThreadInstructionList.add(sp.compute(Operators.Sub,reg2,Registers.regSprID,reg2));
                ThreadInstructionList.add(sp.branch(reg2,new Target(Targets.Rel,2)));

                reghandler.release(reg2);

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

    //TODO understand if # stuff on the right of ANTLR file is necessary for Instructions
    @Override
    public List<Instruction> visitStatementInst(MyLangParser.StatementInstContext ctx) {
        return visit(ctx.statement());
    }


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
        Registers reg = getRegister(ctx.expr());

        // If the variable is shared, write it to the shared memory.
        if (ctx.access() != null && ctx.access().SHARED() != null){
            InstructionList.add(sp.writeToMemory(reg,res.getOffset(ctx)));
        }  //functions have different address space
        else if (this.currentfunctionData != null) {
            Registers memoryAddress = reghandler.acquire();
            InstructionList.add(sp.loadToRegister(Integer.toString(res.getOffset(ctx)),0,memoryAddress,0));
            InstructionList.add(sp.compute(Operators.Sub,Registers.regF,memoryAddress,memoryAddress));
            InstructionList.add(sp.writeToMemory(reg,memoryAddress));
            reghandler.release(memoryAddress);

        }
        // If the variable is not shared, write it to the local memory.

        else {
            InstructionList.add(sp.storeInMemory(varname, reg, res.getOffset(ctx)));
            currentMemoryUsage = res.getOffset(ctx) ;
        }
        reghandler.release(reg);
        return InstructionList;
    }

    @Override
    public List<Instruction> visitChangeStat(MyLangParser.ChangeStatContext ctx) {
        return visit(ctx.changeAss());
    }
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
        Registers reg = reghandler.acquire();

        // Waits/loops/spins until it receives the lock.
        InstructionList.add(sp.testAndSet(7));
        InstructionList.add(sp.receive(reg));
        InstructionList.add(sp.compute(Operators.Equal, reg,Registers.reg0, reg));
        InstructionList.add(sp.branch(reg,new Target(Targets.Rel,-3)));

        reghandler.release(reg);

        // Adds the instructions
        for (MyLangParser.InstructionContext context : ctx.instruction()) {
            InstructionList.addAll(visit(context));
        }
        // Frees up the lock.
        InstructionList.add(sp.writeToMemory(Registers.reg0,7));
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
        String varname = ctx.ID().toString();
        InstructionList.addAll(visit(ctx.expr()));
        Registers reg = getRegister(ctx.expr());

        // If the variable is shared, write it to the shared memory.

        if (res.getGlobal(ctx)){
            InstructionList.add(sp.writeToMemory(reg,res.getOffset(ctx)));
        }
        else if (this.currentfunctionData != null) {
            Registers memoryAddress = reghandler.acquire();
            InstructionList.add(sp.loadToRegister(Integer.toString(res.getOffset(ctx)),0,memoryAddress,0));
            InstructionList.add(sp.compute(Operators.Sub,Registers.regF,memoryAddress,memoryAddress));
            InstructionList.add(sp.writeToMemory(reg,memoryAddress));
            reghandler.release(memoryAddress);

        }
        else{
            InstructionList.add(sp.storeInMemory(varname, reg, res.getOffset(ctx)));
            currentMemoryUsage = res.getOffset(ctx) ;


        }
        reghandler.release(reg);
        return InstructionList;
    }

    @Override
    public List<Instruction> visitAddExpr(MyLangParser.AddExprContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        List<Instruction> expr0 = visit(ctx.expr(0));
        List<Instruction> expr1 = visit(ctx.expr(1));
        InstructionList.addAll(expr0);
        InstructionList.addAll(expr1);
        Registers reg1 = getRegister(ctx.expr(0));
        Registers reg2 = getRegister(ctx.expr(1));
        if (ctx.addOp().PLUS() != null) {
            InstructionList.add(sp.compute(Operators.Add, reg1, reg2, reg1));
        } else {
            InstructionList.add(sp.compute(Operators.Sub, reg1, reg2, reg1));
        }
        reghandler.release(reg2);
        registers.put(ctx, reg1);
        return InstructionList;
    }

    @Override
    public List<Instruction> visitPrimitiveExpr(MyLangParser.PrimitiveExprContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        InstructionList.addAll(visitPrimitive(ctx.primitive()));
        registers.put(ctx, registers.get(ctx.primitive()));
        return InstructionList;

    }

    @Override
    public List<Instruction> visitPrimitive(MyLangParser.PrimitiveContext ctx) {
        Registers register = reghandler.acquire();
        List<Instruction> InstructionList = new ArrayList<>();
        String child = ctx.NUM() !=null ? ctx.NUM().getText() : ctx.booleanVal().getText();
        InstructionList.add(sp.loadToRegister(child, scope.scope_num, register,0));
        registers.put(ctx, register);
        return InstructionList;
    }

    @Override
    public List<Instruction> visitIdExpr(MyLangParser.IdExprContext ctx) {
        Registers register = reghandler.acquire();
        List<Instruction> InstructionList = new ArrayList<>();
        String child = ctx.children.get(0).toString();
        // if variable is shared
        if (res.getGlobal(ctx)){
            Registers reg = reghandler.acquire();
            InstructionList.add(sp.readInst( res.getOffset(ctx)));
            InstructionList.add(sp.receive(register));
            reghandler.release(reg);

        }
        // if we're inside a function
        else if(this.currentfunctionData !=null){
            VariableData variableData = this.currentfunctionData.getVariable(child);
            Registers memoryAddress = reghandler.acquire();
            if (variableData.isParameter){
                // parameter is at the bottom of arp
                InstructionList.add(sp.loadToRegister(Integer.toString(res.getOffset(ctx) + 7),0,memoryAddress,0));
                InstructionList.add(sp.compute(Operators.Add,Registers.regF,memoryAddress,memoryAddress));
            }
            else{
                InstructionList.add(sp.loadToRegister(Integer.toString(res.getOffset(ctx) + 1 ),0,memoryAddress,0));
                InstructionList.add(sp.compute(Operators.Sub,Registers.regF,memoryAddress,memoryAddress));

            }
            reghandler.release(memoryAddress);
            InstructionList.add(sp.getFromIndAddr(register,memoryAddress));

        }
        // if we're in main thread
        else {
            InstructionList.add(sp.loadToRegister(child, scope.scope_num, register, res.getOffset(ctx)));
        }
        registers.put(ctx, register);
        return InstructionList;

    }
    @Override public List<Instruction> visitParallelConstruct(MyLangParser.ParallelConstructContext ctx){
        for (MyLangParser.ThreadConstructContext threadConstructContext:
             ctx.threadConstruct()) {
            visit(threadConstructContext);
        }
        return null;
    }

    @Override
    public List<Instruction> visitPrintConstruct(MyLangParser.PrintConstructContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        InstructionList.addAll(visit(ctx.expr()));
        Registers reg;
        reg = getRegister(ctx.expr());
        InstructionList.add(sp.writeToIO(reg));
        reghandler.release(reg);
        return InstructionList;
    }


    @Override
    public List<Instruction> visitMultExpr(MyLangParser.MultExprContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        List<Instruction> expr0 = visit(ctx.expr(0));
        List<Instruction> expr1 = visit(ctx.expr(1));
        InstructionList.addAll(expr0);
        InstructionList.addAll(expr1);
        Registers reg1 = getRegister(ctx.expr(0));
        Registers reg2 = getRegister(ctx.expr(1));
        InstructionList.add(sp.compute(Operators.Mul, reg1, reg2, reg1));
        reghandler.release(reg2);
        registers.put(ctx, reg1);
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

        Registers reg = reghandler.acquire();
        // Loop until main thread releases this thread's shared memory location.

        InstructionList.add(sp.testAndSet(res.getThread(ctx).getThreadnr()));
        InstructionList.add(sp.receive(reg));
        InstructionList.add(sp.compute(Operators.Equal, reg,Registers.reg0, reg));
        InstructionList.add(sp.branch(reg,new Target(Targets.Rel,-3)));

        reghandler.release(reg);

        // Allow the main thread to go on by writing 0 to its shared location.
        InstructionList.add(sp.writeToMemory(Registers.reg0,0));

        for (MyLangParser.InstructionContext context : ctx.instruction()) {
            InstructionList.addAll(visit(context));
            }

        // Let the main thread know that it has finished up
        // executing by writing 0 to its shared memory location.
        InstructionList.add(sp.writeToMemory(Registers.reg0,res.getThread(ctx).getThreadnr()));
        InstructionList.add(sp.endProgram());
        threads.put(res.getThread(ctx).getThreadnr(),InstructionList);
        return null;


    }

    @Override
    public List<Instruction> visitCompExpr(MyLangParser.CompExprContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        List<Instruction> expr0 = visit(ctx.expr(0));
        List<Instruction> expr1 = visit(ctx.expr(1));
        InstructionList.addAll(expr0);
        InstructionList.addAll(expr1);
        Registers reg1 = getRegister(ctx.expr(0));
        Registers reg2 = getRegister(ctx.expr(1));
        switch (ctx.getChild(1).getText()) {
            case "==":
                InstructionList.add(sp.compute(Operators.Equal, reg1, reg2, reg1));
                break;
            case "!=":
                InstructionList.add(sp.compute(Operators.NEq, reg1, reg2, reg1));
                break;
            case ">":
                InstructionList.add(sp.compute(Operators.Gt, reg1, reg2, reg1));
                break;
            case "<":
                InstructionList.add(sp.compute(Operators.Lt, reg1, reg2, reg1));
                break;
            case ">=":
                InstructionList.add(sp.compute(Operators.GtE, reg1, reg2, reg1));
                break;
            case "<=":
                InstructionList.add(sp.compute(Operators.LtE, reg1, reg2, reg1));
                break;

        }
        reghandler.release(reg2);
        registers.put(ctx, reg1);
        return InstructionList;
    }

    @Override
    public List<Instruction> visitBoolExpr(MyLangParser.BoolExprContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        List<Instruction> expr0 = visit(ctx.expr(0));
        List<Instruction> expr1 = visit(ctx.expr(1));
        InstructionList.addAll(expr0);
        InstructionList.addAll(expr1);
        Registers reg1 = getRegister(ctx.expr(0));
        Registers reg2 = getRegister(ctx.expr(1));
        if (ctx.booleanOp().AND() != null) {
            InstructionList.add(sp.compute(Operators.And, reg1, reg2, reg1));
        } else {
            InstructionList.add(sp.compute(Operators.Or, reg1, reg2, reg1));
        }
        reghandler.release(reg2);
        registers.put(ctx, reg1);
        return InstructionList;

    }

    @Override
    public List<Instruction> visitParExpr(MyLangParser.ParExprContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        InstructionList.addAll(visit(ctx.expr()));
        registers.put(ctx, getRegister(ctx.expr()));
        return InstructionList;
    }

    @Override
    public List<Instruction> visitPrfExpr(MyLangParser.PrfExprContext ctx){
        List<Instruction> InstructionList = new ArrayList<>();
        InstructionList.addAll(visit(ctx.expr()));
        Registers register = reghandler.acquire();
        Registers exprRegister = getRegister(ctx.expr());
        if (ctx.prefixOp().NOT() != null){
            InstructionList.add(sp.loadToRegister("false",scope.scope_num,register,0));
            InstructionList.add(sp.compute(Operators.Xor,exprRegister,register,exprRegister));
        }
        else if (ctx.prefixOp().MINUS() != null){
            InstructionList.add(sp.loadToRegister("0",scope.scope_num,register,0));
            InstructionList.add(sp.compute(Operators.Sub,exprRegister,register,exprRegister));
        }
        registers.put(ctx, exprRegister);
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
        // Visit the blocks but do not add them.
        List<Instruction> firstBlock = visit(ctx.block(0));
        List<Instruction> secondBlock = ctx.block().size() > 1 ? visit(ctx.block(1)) : new ArrayList<>();
        // Create the jump based on the sizes of the blocks.
        InstructionList.add(sp.branch(getRegister(ctx.expr()),new Target(Targets.Rel,2)));
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
        int exprsize = InstructionList.size();
        List<Instruction> block = visit(ctx.block());
        // Jump out the block if while loop is false
        InstructionList.add(sp.branch(getRegister(ctx.expr()),new Target(Targets.Rel,2)));
        InstructionList.add(sp.relJump(block.size()+2));

        InstructionList.addAll(block);

        InstructionList.add(sp.relJump(-1 * (block.size() + exprsize + 2)));
        scope.closeScope();
        return InstructionList;
    }

    @Override  public  List<Instruction> visitBlock(MyLangParser.BlockContext ctx){
        List<Instruction> InstructionList = new ArrayList<>();
        for (MyLangParser.InstructionContext context : ctx.instruction()) {
            InstructionList.addAll(visit(context));
        }
        return InstructionList;
    }
    @Override public List<Instruction> visitReturnInst(MyLangParser.ReturnInstContext ctx){
        return visit(ctx.returnConstruct());
    }
    @Override public List<Instruction> visitReturnConstruct(MyLangParser.ReturnConstructContext ctx){
        List<Instruction> InstructionList = new ArrayList<>();
        InstructionList.addAll(visit(ctx.expr()));
        Registers regReturnVal = getRegister(ctx.expr());
        Registers regReturnValAddr = reghandler.acquire();
        // arp + 2
        InstructionList.add(sp.loadToRegister("2",0,regReturnValAddr,0));
        InstructionList.add(sp.compute(Operators.Add,regReturnValAddr,Registers.regF,regReturnValAddr));
        InstructionList.add(sp.storeInMemory(regReturnVal,regReturnValAddr));
        reghandler.release(regReturnValAddr);
        reghandler.release(regReturnVal);
        return InstructionList;
    }
    @Override public List<Instruction> visitFunctionInst(MyLangParser.FunctionInstContext ctx){
        return visit(ctx.functionConstruct());
    }
    @Override public List<Instruction> visitFunctionConstruct(MyLangParser.FunctionConstructContext ctx){
        List<Instruction> InstructionList = new ArrayList<>();
        this.currentfunctionData = res.getFunctionData(ctx.ID(0).toString());
        InstructionList.addAll(visit(ctx.block()));
        Registers regReturnAddr = reghandler.acquire();
        // todo do this with incr instead
        InstructionList.add(sp.loadToRegister("1",0,regReturnAddr,0));
        InstructionList.add(sp.compute(Operators.Add,regReturnAddr,Registers.regF,regReturnAddr));
        InstructionList.add(sp.getFromIndAddr(regReturnAddr,regReturnAddr));
        InstructionList.add(sp.IndJump(regReturnAddr));
        this.functions.put(ctx.ID(0).toString(),InstructionList);
        reghandler.release(regReturnAddr);
        this.currentfunctionData = null;
        return null;
    }
    @Override public List<Instruction> visitFuncCallExpr(MyLangParser.FuncCallExprContext ctx){
        List<Instruction> InstructionList = new ArrayList<>();
        //TODO Fake inst
        if (this.currentfunctionData !=null){

        }
        // if we're in main thread dont need dynamic memory
        else {
            FunctionData calledFunction = res.getFunctionData(ctx.ID().toString());
            int arpLocation = currentMemoryUsage +  calledFunction.localDataSize + 1 + 5 + 1;
            for (int i=0;i<ctx.expr().size();i++){
                InstructionList.addAll(visit(ctx.expr(i)));
                Registers registers = reghandler.acquire();
                InstructionList.add(sp.loadToRegister(Integer.toString(arpLocation+7+1+i),0,registers,0));
                InstructionList.add(sp.storeInMemory(getRegister(ctx.expr(i)),registers));
                reghandler.release(registers);
                reghandler.release(getRegister(ctx.expr(i)));
            }

            //TODO SAVE REGHANDLER BEFORE GENERATING FUNCTION
            Registers reg = reghandler.acquire();
            InstructionList.add(sp.loadToRegister(Integer.toString(arpLocation),0,Registers.regF,0));
            InstructionList.add(sp.storeInMemory(Registers.regA, currentMemoryUsage + 1));
            InstructionList.add(sp.storeInMemory(Registers.regB, currentMemoryUsage + 2));
            InstructionList.add(sp.storeInMemory(Registers.regC, currentMemoryUsage + 3));
            InstructionList.add(sp.storeInMemory(Registers.regD, currentMemoryUsage + 4));
            InstructionList.add(sp.storeInMemory(Registers.regE, currentMemoryUsage + 5));


            InstructionList.add(sp.loadToRegister("1",0,Registers.regA,0));
            InstructionList.add(sp.compute(Operators.Add,Registers.regF,Registers.regA,Registers.regA));

            InstructionList.add(sp.loadToRegister("3",0,Registers.regB,0));
            InstructionList.add(sp.compute(Operators.Add,Registers.regPC,Registers.regB,Registers.regB));
            InstructionList.add(sp.storeInMemory(Registers.regB,Registers.regA));
            // jump here
            InstructionList.add(sp.fakeInst(ctx.ID().toString()));
            // jump here
            InstructionList.add(sp.loadToRegister("a",0,Registers.regA,currentMemoryUsage+1));
            InstructionList.add(sp.loadToRegister("a",0,Registers.regB,currentMemoryUsage+2));
            InstructionList.add(sp.loadToRegister("a",0,Registers.regC,currentMemoryUsage+3));
            InstructionList.add(sp.loadToRegister("a",0,Registers.regD,currentMemoryUsage+4));
            InstructionList.add(sp.loadToRegister("a",0,Registers.regE,currentMemoryUsage+5));

            InstructionList.add(sp.loadToRegister("a",0,reg,arpLocation+2));
            registers.put(ctx, reg);
            return InstructionList;








        }
        return null;
    }

}


