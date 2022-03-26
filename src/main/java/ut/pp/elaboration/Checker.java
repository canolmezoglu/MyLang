package ut.pp.elaboration;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import ut.pp.parser.MyLangBaseListener;
import org.antlr.v4.runtime.tree.ParseTree;
import ut.pp.parser.MyLangParser;

import java.sql.ResultSet;
import java.util.List;

public class Checker extends MyLangBaseListener {
    private List<String> errors;
    private Result result;
    public Result check (ParseTree tree) throws Exception {
        new ParseTreeWalker().walk(this, tree);
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
    //TODO getType should be done with scopetable
    @Override public void exitIdExpr(MyLangParser.IdExprContext ctx){
        // I GET THE TYPE FROM SCOPETABLE HERE FROM THE ID OF THE VARIABLE
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
        // I SHOULD GET THE TYPE FROM SCOPETABLE HERE
        MyType magicalTypeFromScopeTable = MyType.BOOLEAN;
        if (magicalTypeFromScopeTable != getType(ctx.expr())){
            this.errors.add("you are changing a variable to an unexpected type");
        }
        setType(ctx,getType(ctx.expr()));
    }

    //TODO type should be set here to the id of the variable
    @Override public void exitDeclaration(MyLangParser.DeclarationContext ctx) {
        if (ctx.type().BOOLEAN() != null) {
            if (getType(ctx.expr()) != MyType.BOOLEAN) {
                this.errors.add("you are trying to assign an integer to a boolean variable");
            }
            setType(ctx, MyType.BOOLEAN);
        }
        else if (ctx.type().INTEGER() != null ) {
            if (getType(ctx.expr()) != MyType.NUM) {
                this.errors.add("you are trying to assign an boolean to an integer variable");
            }
            setType(ctx, MyType.NUM);
        }
    }
    @Override public void exitIfConstruct(MyLangParser.IfConstructContext ctx){
        if (getType(ctx.expr()) != MyType.BOOLEAN ){
            this.errors.add("if statement can only check a boolean");
        }
    }

    @Override public void exitWhileConstruct(MyLangParser.WhileConstructContext ctx){
        if (getType(ctx.expr()) != MyType.BOOLEAN ){
            this.errors.add("while statement can only check a boolean");
        }
    }
    public void setType(ParseTree node, MyType type) {
        this.result.setType(node, type);
    }
    public MyType getType (ParseTree node) {
        return this.result.getType(node);
    }



}




