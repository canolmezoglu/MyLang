package ut.pp.elaboration;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import ut.pp.parser.MyLangBaseListener;
import ut.pp.parser.MyLangLexer;
import ut.pp.parser.MyLangParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Checker extends MyLangBaseListener {
    private List<String> errors=new ArrayList<>();
    private Result result=new Result();
    private ScopeTable scope=new ScopeTable();

    public static void main(String args[]) throws Exception {
        Checker c = new Checker();
        String input = "  int wait=1; while (wait > 0){\n" +
                "        int c=0; wait = wait - 1;\n"+
                "    } c=100;";
        MyLangLexer myLangLexer = new MyLangLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(myLangLexer);
        MyLangParser parser = new MyLangParser(tokens);
        ParseTree tree = parser.instruction();
        System.out.println(c.checkScope(tree));
    }

    public Set<String> checkScope(ParseTree tree){
        new ParseTreeWalker().walk(this, tree);
        return scope.errors;
    }

    public Result check (ParseTree tree) throws Exception {
        new ParseTreeWalker().walk(this, tree);
        this.errors.addAll((scope.errors));
        if (!this.errors.isEmpty()){
            throw new Exception(this.errors.toString());
        }
        return this.result;
    }

    @Override public void exitPrfExpr(MyLangParser.PrfExprContext ctx){
        if (ctx.prefixOp().MINUS() != null && getType(ctx.expr() )!= MyType.NUM){
                this.errors.add("Prefix operation has type mismatch, expected int, got bool");
        }
        else if (ctx.prefixOp().NOT() != null && getType(ctx.expr() )!= MyType.BOOLEAN){
            this.errors.add("Prefix operation has type mismatch, expected bool, got int");
        }
        setType(ctx, getType(ctx.expr()));
    }
    @Override public void exitMultExpr(MyLangParser.MultExprContext ctx){
        if (getType(ctx.expr(0)) != getType(ctx.expr(1))){
            this.errors.add("Multiplication has type mismatch");
        }
        setType(ctx, getType(ctx.expr(0)));
    }
    @Override public void exitAddExpr(MyLangParser.AddExprContext ctx){
        if (getType(ctx.expr(0)) != getType(ctx.expr(1))){
            if (ctx.addOp().PLUS() != null){
                this.errors.add("addition has type mismatch");
            }
            else if (ctx.addOp().MINUS() != null){
                this.errors.add("subtraction has type mismatch");
            }
        }
        setType(ctx, getType(ctx.expr(0)));
    }

    @Override public void exitCompExpr(MyLangParser.CompExprContext ctx){
        MyType leftType = getType(ctx.expr(0));
        if (leftType != MyType.NUM && leftType != getType(ctx.expr(1))){
            this.errors.add("compare expression does not compare booleans");
        }
        setType(ctx, getType(ctx.expr(0)));
    }
    @Override public void exitBoolExpr(MyLangParser.BoolExprContext ctx){
        MyType leftType = getType(ctx.expr(0));
        if (leftType != MyType.BOOLEAN && leftType != getType(ctx.expr(1))){
            this.errors.add("boolean expression does not work with integers");
        }
        setType(ctx, getType(ctx.expr(0)));
    }
    @Override public void exitParExpr(MyLangParser.ParExprContext ctx){
        setType(ctx,getType(ctx.expr()));
    }

    @Override public void exitPrimitiveExpr(MyLangParser.PrimitiveExprContext ctx){
        result.setType(ctx,getType(ctx.primitive()));
    }

    @Override public void exitIdExpr(MyLangParser.IdExprContext ctx){
        MyType check = scope.check(ctx.ID().toString(),ctx.getStart());
        if(check!=null) {
            setType(ctx,check);
        }
    }

    @Override public void exitPrimitive(MyLangParser.PrimitiveContext ctx) {
        if (ctx.NUM() != null) {
            result.setType(ctx,MyType.NUM);

        }
        else if (ctx.booleanVal() != null) {
            result.setType(ctx,MyType.BOOLEAN);
        }
    }

    @Override public void exitChangeAss(MyLangParser.ChangeAssContext ctx) {
        MyType check = scope.check(ctx.ID().toString(),ctx.getStart());
        if(check!=null) {
            if (check != getType(ctx.expr())) {
                this.errors.add("you are changing a variable to an unexpected type");
            }
            setType(ctx, getType(ctx.expr()));
        }
    }

    @Override public void exitDeclaration(MyLangParser.DeclarationContext ctx) {
        if (ctx.type().BOOLEAN() != null) {
            if (getType(ctx.expr()) != MyType.BOOLEAN) {
                this.errors.add("you are trying to assign an integer to a boolean variable");
            }
            setType(ctx, MyType.BOOLEAN);
            scope.declare(ctx.ID().toString(),MyType.BOOLEAN,ctx.getStart());
        }
        else if (ctx.type().INTEGER() != null ) {
            if (getType(ctx.expr()) != MyType.NUM) {
                this.errors.add("you are trying to assign an boolean to an integer variable");
            }
            setType(ctx, MyType.NUM);
            scope.declare(ctx.ID().toString(),MyType.NUM,ctx.getStart());
        }
        else{
            this.errors.add("Invalid type");
        }
    }

    @Override
    public void enterIfConstruct(MyLangParser.IfConstructContext ctx) {
        scope.openScope();
        super.enterIfConstruct(ctx);
    }

    @Override public void exitIfConstruct(MyLangParser.IfConstructContext ctx){
        scope.closeScope();
        if (getType(ctx.expr()) != MyType.BOOLEAN ){
            this.errors.add("if statement can only check a boolean");
        }
    }

    @Override
    public void enterWhileConstruct(MyLangParser.WhileConstructContext ctx) {
        scope.openScope();
        super.enterWhileConstruct(ctx);
    }

    @Override public void exitWhileConstruct(MyLangParser.WhileConstructContext ctx){
        scope.closeScope();
        if (getType(ctx.expr()) != MyType.BOOLEAN ){
            this.errors.add("while statement can only check a boolean");
        }
    }

    @Override
    public void enterThreadConstruct(MyLangParser.ThreadConstructContext ctx) {
        scope.openScope();
        super.enterThreadConstruct(ctx);
    }

    @Override
    public void exitThreadConstruct(MyLangParser.ThreadConstructContext ctx) {
        scope.closeScope();
        super.exitThreadConstruct(ctx);
    }

    public void setType(ParseTree node, MyType type) {
        this.result.setType(node, type);
    }
    public MyType getType (ParseTree node) {
        return this.result.getType(node);
    }
}




