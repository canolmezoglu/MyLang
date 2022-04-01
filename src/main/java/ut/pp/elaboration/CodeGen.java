package ut.pp.elaboration;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import ut.pp.elaboration.haskell.HaskellProcess;
import ut.pp.elaboration.model.Register;
import ut.pp.elaboration.model.Sprockell;
import ut.pp.elaboration.model.Target;
import ut.pp.elaboration.model.enums.Operators;
import ut.pp.elaboration.model.enums.Registers;
import ut.pp.elaboration.model.enums.Targets;
import ut.pp.parser.MyLangBaseListener;
import ut.pp.parser.MyLangLexer;
import ut.pp.parser.MyLangParser;

import java.io.IOException;
import java.util.List;

public class CodeGen extends MyLangBaseListener {


    Sprockell sp;
    ParseTreeProperty<Registers> registers;
    Register reghandler;
    ScopeTable scope;

    public static void main(String args[]){
        String code = "int a=100; int b=120; int c=0; c=a+b; ;";
        MyLangLexer myLangLexer = new MyLangLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(myLangLexer);
        MyLangParser parser = new MyLangParser(tokens);
        ParseTree tree = parser.program();
        CodeGen c =new CodeGen();
        Sprockell program = c.genCode(tree);
        System.out.println(program.getInstructions());
        System.out.println(program.getMemory());
    }

    public Sprockell genCode(ParseTree tree){
        sp=new Sprockell();
        scope = new ScopeTable();
        registers = new ParseTreeProperty<>();
        reghandler=new Register();
        new ParseTreeWalker().walk(this, tree);
        return sp;
    }

    /**
     * Loads an expression to a register and returns it
     * Invokes the reghandler to acquire an empty register,
     * therefore, the returned register should be released
     * @return register
     */
    private Registers getRegister(MyLangParser.ExprContext expr){
        if(registers.get(expr)!=null){
            return registers.get(expr);
        }
        if(expr.getChildCount()==1){
            String child = expr.getChild(0).getText();
            return sp.loadToRegister(child,scope.scope_num,reghandler.acquire());
        }
        return null;
    }
    @Override public void exitDeclaration(MyLangParser.DeclarationContext ctx) {
        String varname =ctx.ID().toString();
        if (ctx.type().BOOLEAN() != null) {
            scope.declare(varname,MyType.BOOLEAN,ctx.getStart());
        }
        if (ctx.type().INTEGER() != null ) {

            scope.declare(varname,MyType.NUM,ctx.getStart());
        }
        Registers reg = getRegister(ctx.expr());
        sp.addToMemory(varname, scope.scope_num);
        sp.storeInMemory(varname,reg,scope.scope_num);
        reghandler.release(reg);
    }
    @Override public void exitChangeAss(MyLangParser.ChangeAssContext ctx) {
        String varname =ctx.ID().toString();
        Registers reg = getRegister(ctx.expr());
        sp.storeInMemory(varname,reg, scope.scope_num);
        reghandler.release(reg);
    }
    @Override public void exitAddExpr(MyLangParser.AddExprContext ctx){
        Registers reg1 = getRegister(ctx.expr(0));
        Registers reg2 = getRegister(ctx.expr(1));
        if(ctx.addOp().PLUS() !=null){
            sp.compute(Operators.Add,reg1,reg2,reg1);
        }
        else{
            sp.compute(Operators.Sub,reg1,reg2,reg1);
        }
        reghandler.release(reg2);
        registers.put(ctx,reg1);
    }
    @Override public void exitMultExpr(MyLangParser.MultExprContext ctx){
        Registers reg1 = getRegister(ctx.expr(0));
        Registers reg2 = getRegister(ctx.expr(1));
        sp.compute(Operators.Mul,reg1,reg2,reg1);
        reghandler.release(reg2);
        registers.put(ctx,reg1);
    }
    @Override public void exitCompExpr(MyLangParser.CompExprContext ctx){
        Registers reg1 = getRegister(ctx.expr(0));
        Registers reg2 = getRegister(ctx.expr(1));
        switch (ctx.getChild(1).getText()){
            case "==":
                sp.compute(Operators.Equal, reg1, reg2, reg1);
                break;
            case "!=":
                sp.compute(Operators.NEq, reg1, reg2, reg1);
                break;
            case ">":
                sp.compute(Operators.Gt, reg1, reg2, reg1);
                break;
            case "<":
                sp.compute(Operators.Lt, reg1, reg2, reg1);
                break;
            case ">=":
                sp.compute(Operators.GtE, reg1, reg2, reg1);
                break;
            case "<=":
                sp.compute(Operators.LtE, reg1, reg2, reg1);
                break;

        }
        reghandler.release(reg2);
        registers.put(ctx,reg1);
    }
    @Override
    public void exitBoolExpr(MyLangParser.BoolExprContext ctx){
        Registers reg1 = getRegister(ctx.expr(0));
        Registers reg2 = getRegister(ctx.expr(1));
        if(ctx.booleanOp().AND() != null){
            sp.compute(Operators.And,reg1,reg2,reg1);
        }
        else{
            sp.compute(Operators.Or,reg1,reg2,reg1);
        }
        reghandler.release(reg2);
        registers.put(ctx,reg1);
    }
    @Override public void exitParExpr(MyLangParser.ParExprContext ctx){
        registers.put(ctx,getRegister(ctx.expr()));
    }
    @Override
    public void exitPrintConstruct(MyLangParser.PrintConstructContext ctx) {
        Registers reg;
        reg = getRegister(ctx.expr());
        sp.writeToIO(reg);
        reghandler.release(reg);
    }

    @Override
    public void enterIfConstruct(MyLangParser.IfConstructContext ctx) {
        scope.openScope();

    }

    @Override public void exitIfConstruct(MyLangParser.IfConstructContext ctx){
        scope.closeScope();
    }

    @Override
    public void enterWhileConstruct(MyLangParser.WhileConstructContext ctx) {
        scope.openScope();
    }

    @Override public void exitWhileConstruct(MyLangParser.WhileConstructContext ctx){
        scope.closeScope();
    }

    @Override
    public void enterThreadConstruct(MyLangParser.ThreadConstructContext ctx) {
        scope.openScope();
    }

    @Override
    public void exitThreadConstruct(MyLangParser.ThreadConstructContext ctx) {
        scope.closeScope();
    }




}
