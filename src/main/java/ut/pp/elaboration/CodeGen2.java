package ut.pp.elaboration;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import ut.pp.elaboration.haskell.HaskellProcess;
import ut.pp.elaboration.model.*;
import ut.pp.elaboration.model.enums.Instructions;
import ut.pp.elaboration.model.enums.Operators;
import ut.pp.elaboration.model.enums.Registers;
import ut.pp.elaboration.model.enums.Targets;
import ut.pp.parser.MyLangBaseListener;
import ut.pp.parser.MyLangBaseVisitor;
import ut.pp.parser.MyLangLexer;
import ut.pp.parser.MyLangParser;

import javax.print.DocFlavor;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class CodeGen2 extends MyLangBaseVisitor<List<Instruction>> {


    Sprockell sp;
    ParseTreeProperty<Registers> registers;
    Register reghandler;
    ScopeTable scope;
    Result res;
    Map<Integer,List<Instruction>> threads = new HashMap<>();
    List<Instruction> instructions = new ArrayList<>();

    public static void main(String args[]) throws Exception {
        String code = "shared int money = 0;\n" +
                "parallel {\n" +
                "thread {   int wait = 100; while (wait > 0){\n " +
                "        wait = wait - 1;\n" +
                "lock" +
                "            money = money + 4;\n" +
                "unlock" +
                "    }\n" +
                "}\n" +
                "thread {\n" +
                "    int wait = 100; while (wait > 0){\n" +
                "        wait = wait - 1;\n" +
                            "lock" +
                "            money = money - 2;\n" +
                            "unlock" +
                "    }\n" +
                "thread {\n" +
                "    int wait = 100; while (wait > 0){\n" +
                "        wait = wait - 5;\n" +
                "lock" +
                "            money = money + 5;\n" +
                "unlock" +
                "    }\n" +
                "}\n" +
                "thread { lock money = money + 31; unlock }" +
                "thread { lock money = money + 89; unlock }" +
                "}" +
                "print(money);\n";
        MyLangLexer myLangLexer = new MyLangLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(myLangLexer);
        MyLangParser parser = new MyLangParser(tokens);
        ParseTree tree = parser.program();
        CodeGen2 c = new CodeGen2();
        List<Instruction> instructions = c.genCode(tree);
        for (Instruction i : instructions){
            System.out.println(" ," + i.toString());
        }
//        System.out.println(instructions.toString());
//        System.out.println(program.getMemory());
    }

    public List<Instruction> getInstructions() {
        return instructions;
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
     *
     * @return register
     */
    private Registers getRegister(MyLangParser.ExprContext expr) {
        return registers.get(expr);

    }

    @Override
    public List<Instruction> visitProgram(MyLangParser.ProgramContext ctx) {
        boolean threaded = false;
        List<Instruction> InstructionList = new ArrayList<>();
        ThreadSp pss = res.getThread(ctx);
        for (MyLangParser.InstructionContext context : ctx.instruction()) {
            if (context.getStart().getText().equals("parallel")){
                threaded = true;
                for (ThreadSp threadSp:
                     pss.getChildren()) {
                    InstructionList.add(sp.writeToMemory(Registers.reg0,threadSp.getThreadnr()));
                    InstructionList.add(sp.testAndSet(0));
                    Registers reg = reghandler.acquire();
                    InstructionList.add(sp.receive(reg));
                    InstructionList.add(sp.compute(Operators.Equal, reg,Registers.reg0, reg));
                    InstructionList.add(sp.branch(reg,new Target(Targets.Rel,-3)));
                    InstructionList.add(sp.loadToRegister("1",0,reg,0));
                    InstructionList.add(sp.writeToMemory(reg,0));

                    reghandler.release(reg);


                }

                for (ThreadSp threadSp:
                        pss.getChildren()) {
                    InstructionList.add(sp.testAndSet(threadSp.getThreadnr()));
                    Registers reg = reghandler.acquire();
                    InstructionList.add(sp.receive(reg));
                    InstructionList.add(sp.compute(Operators.Equal, reg,Registers.reg0, reg));
                    InstructionList.add(sp.branch(reg,new Target(Targets.Rel,-3)));
                    reghandler.release(reg);
                }

                visit(context);

            }
            else{
                InstructionList.addAll(visit(context));

            }
        }
        InstructionList.add(sp.endProgram());

        if (threaded){
            Set<Integer> threadList = threads.keySet();
            List<Instruction> ThreadInstructionList = new ArrayList<>();
            Registers reg = reghandler.acquire();

//            ThreadInstructionList.add(sp.compute(Operators.Equal, Registers.regSprID,Registers.reg0, reg));
//            ThreadInstructionList.add(sp.branch(reg,new Target(Targets.Rel,5)));
            ThreadInstructionList.add(sp.loadToRegister("1",0,reg,0));
            ThreadInstructionList.add(sp.writeToMemory(reg,Registers.regSprID));
//            ThreadInstructionList.add(sp.branch(reg,new Target(Targets.Rel,-3)));
            reghandler.release(reg);

            int below_up_size = 4 * (threads.size()-1);
            int downsize = 0;
            List<Integer> threadList2 = new ArrayList<>();
            for (Integer i : threadList){
                Registers reg2 = reghandler.acquire();
                ThreadInstructionList.add(sp.loadToRegister(i.toString(),0,reg2,0));
                ThreadInstructionList.add(sp.compute(Operators.Sub,reg2,Registers.regSprID,reg2));
                ThreadInstructionList.add(sp.branch(reg2,new Target(Targets.Rel,2)));
                ThreadInstructionList.add(sp.relJump(InstructionList.size()+downsize+below_up_size+1));
                below_up_size = below_up_size - 4;
                downsize += threads.get(i).size();
                threadList2.add(i);
                reghandler.release(reg2);
            }
            ThreadInstructionList.addAll(InstructionList);
            for (Integer x : threadList2){
                ThreadInstructionList.addAll(threads.get(x));
            }
            return ThreadInstructionList;
        }

        return InstructionList;
    }

    @Override
    public List<Instruction> visitStatementInst(MyLangParser.StatementInstContext ctx) {
        return visit(ctx.statement());
    }


    @Override
    public List<Instruction> visitDeclStat(MyLangParser.DeclStatContext ctx) {
        return visit(ctx.declaration());
    }

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
        if (ctx.access() != null && ctx.access().SHARED() != null){
            InstructionList.add(sp.writeToMemory(reg,res.getOffset(ctx)));
        }
        else {
            InstructionList.add(sp.storeInMemory(varname, reg, res.getOffset(ctx)));
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
    @Override
    public List<Instruction> visitLockConstruct(MyLangParser.LockConstructContext ctx){
        List<Instruction> InstructionList = new ArrayList<>();
        Registers reg = reghandler.acquire();
        InstructionList.add(sp.testAndSet(7));
        InstructionList.add(sp.receive(reg));
        InstructionList.add(sp.compute(Operators.Equal, reg,Registers.reg0, reg));
        InstructionList.add(sp.branch(reg,new Target(Targets.Rel,-3)));
        reghandler.release(reg);
        for (MyLangParser.InstructionContext context : ctx.instruction()) {
            InstructionList.addAll(visit(context));
        }
        InstructionList.add(sp.writeToMemory(Registers.reg0,7));
        return InstructionList;

    }

    @Override
    public List<Instruction> visitChangeAss(MyLangParser.ChangeAssContext ctx) {
        List<Instruction> InstructionList = new ArrayList<>();
        String varname = ctx.ID().toString();
        InstructionList.addAll(visit(ctx.expr()));
        Registers reg = getRegister(ctx.expr());
        if (res.getGlobal(ctx)){
            InstructionList.add(sp.writeToMemory(reg,res.getOffset(ctx)));
        }
        else{
            InstructionList.add(sp.storeInMemory(varname, reg, res.getOffset(ctx)));

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
        String child = ctx.children.get(0).getText();
        if (res.getGlobal(ctx)){
            Registers reg = reghandler.acquire();
            InstructionList.add(sp.readInst( res.getOffset(ctx)));
            InstructionList.add(sp.receive(register));
//            InstructionList.add(sp.compute(Operators.Equal, register,Registers.reg0, reg));
//            InstructionList.add(sp.branch(reg,new Target(Targets.Rel,-3)));
            reghandler.release(reg);

        }
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
    @Override public List<Instruction> visitThreadConstruct(MyLangParser.ThreadConstructContext ctx){
        List<Instruction> InstructionList = new ArrayList<>();
        InstructionList.add(sp.testAndSet(res.getThread(ctx).getThreadnr()));
        Registers reg = reghandler.acquire();
        InstructionList.add(sp.receive(reg));
        InstructionList.add(sp.compute(Operators.Equal, reg,Registers.reg0, reg));
        InstructionList.add(sp.branch(reg,new Target(Targets.Rel,-3)));
        reghandler.release(reg);
        InstructionList.add(sp.writeToMemory(Registers.reg0,0));

        for (MyLangParser.InstructionContext context : ctx.instruction()) {
            InstructionList.addAll(visit(context));
            }
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
    @Override
    public List<Instruction> visitIfConstruct(MyLangParser.IfConstructContext ctx) {
        scope.openScope();
        List<Instruction> InstructionList = new ArrayList<>();
        InstructionList.addAll(visit(ctx.expr()));
        List<Instruction> firstBlock = visit(ctx.block(0));
        List<Instruction> secondBlock = ctx.block().size() > 1 ? visit(ctx.block(1)) : new ArrayList<>();
        InstructionList.add(sp.branch(getRegister(ctx.expr()),new Target(Targets.Rel,2)));
        InstructionList.add(sp.relJump(firstBlock.size()+2));
        InstructionList.addAll(firstBlock);
        InstructionList.add(sp.relJump(secondBlock.size()+1));
        InstructionList.addAll(secondBlock);
        scope.closeScope();
        return InstructionList;
    }

    @Override
    public List<Instruction> visitWhileConstruct(MyLangParser.WhileConstructContext ctx){
        scope.openScope();
        List<Instruction> InstructionList = new ArrayList<>();
        InstructionList.addAll(visit(ctx.expr()));
        int exprsize = InstructionList.size();
        List<Instruction> block = visit(ctx.block());
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
}


