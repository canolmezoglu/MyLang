package ut.pp.elaboration;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import ut.pp.elaboration.model.ThreadSp;
import ut.pp.parser.MyLangBaseListener;
import ut.pp.parser.MyLangLexer;
import ut.pp.parser.MyLangParser;
import ut.pp.elaboration.ScopeTable;

import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Checker extends MyLangBaseListener {
    private List<String> errors;
    private Result result;
    private ScopeTable scope;
    private ThreadSp active_thread;
    private List<ThreadSp> threads;

    public Result check (ParseTree tree) throws Exception {
        this.result = new Result();
        this.errors = new ArrayList<String>();
        this.scope = new ScopeTable();
        this.threads = new ArrayList<>();
        new ParseTreeWalker().walk(this, tree);
        this.errors.addAll((scope.errors));
        if (!this.errors.isEmpty()){
            throw new Exception(this.errors.toString());
        }
        return this.result;
    }

    @Override public void enterProgram(MyLangParser.ProgramContext ctx){
        active_thread = new ThreadSp(0,0);
        threads.add(active_thread);

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
        MyType rightType = getType(ctx.expr(1));
        if (ctx.comp().EQ() != null || ctx.comp().NE() != null){
            if (leftType!=rightType){
                this.errors.add("you cannot compare different types in terms" +
                        "of being equal");
            }
        }
        else if (leftType != MyType.NUM && leftType != rightType){
            this.errors.add("you cannot compare boolean types");
        }
        setType(ctx, MyType.BOOLEAN);
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
        VariableData check = scope.check(ctx.ID().toString(),ctx.getStart());
        if(check!=null) {
            setType(ctx,check.type);
            setOffset(ctx,check.sizeCurr);
            result.setGlobal(ctx,check.global);

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
        VariableData check = scope.check(ctx.ID().toString(),ctx.getStart());
        if(check!=null) {
            if (check.type != getType(ctx.expr())) {
                this.errors.add("you are changing a variable to an unexpected type");
            }
            setType(ctx, getType(ctx.expr()));
            setOffset(ctx,check.sizeCurr);
            result.setGlobal(ctx,check.global);
        }
    }

    @Override public void exitDeclaration(MyLangParser.DeclarationContext ctx) {

            if (ctx.type().BOOLEAN() != null) {
                if (getType(ctx.expr()) != MyType.BOOLEAN) {
                    this.errors.add("you are trying to assign an integer to a boolean variable");
                }
                setType(ctx, MyType.BOOLEAN);

                setOffset(ctx, scope.declare(ctx.ID().toString(), MyType.BOOLEAN, ctx.getStart(),ctx.access() != null && ctx.access().SHARED() != null).sizeCurr);
            } else if (ctx.type().INTEGER() != null) {
                if (getType(ctx.expr()) != MyType.NUM) {
                    this.errors.add("you are trying to assign an boolean to an integer variable");
                }
                setType(ctx, MyType.NUM);
                setOffset(ctx, scope.declare(ctx.ID().toString(), MyType.NUM, ctx.getStart(),ctx.access() != null && ctx.access().SHARED() != null).sizeCurr);
            } else {
                this.errors.add("Invalid type");
            }
    }


    @Override
    public void enterIfConstruct(MyLangParser.IfConstructContext ctx) {
        scope.openScope();
    }

    @Override public void exitIfConstruct(MyLangParser.IfConstructContext ctx){
        scope.closeScope();
        result.setThread(ctx,active_thread);

        if (getType(ctx.expr()) != MyType.BOOLEAN ){
            this.errors.add("if statement can only check a boolean");
        }
    }

    @Override
    public void enterWhileConstruct(MyLangParser.WhileConstructContext ctx) {
        scope.openScope();
    }

    @Override public void exitWhileConstruct(MyLangParser.WhileConstructContext ctx){
        scope.closeScope();
        result.setThread(ctx,active_thread);

        if (getType(ctx.expr()) != MyType.BOOLEAN ){
            this.errors.add("while statement can only check a boolean");
        }
    }
    @Override public void exitStatementInst(MyLangParser.StatementInstContext ctx){
        result.setThread(ctx.statement(),active_thread);
    }



    @Override
    public void enterThreadConstruct(
            MyLangParser.ThreadConstructContext ctx
    ) {
        scope.openScope();

        active_thread = new ThreadSp(threads.size(),active_thread.getThreadnr());
        threads.get(active_thread.getParentnr()).addchild(active_thread);
        threads.add(active_thread);
    }

    @Override
    public void exitThreadConstruct(MyLangParser.ThreadConstructContext ctx) {
        result.setThread(ctx,active_thread);
        active_thread = threads.get(active_thread.getParentnr());
        scope.closeScope();

    }
    @Override
    public void exitPrintConstruct(MyLangParser.PrintConstructContext ctx){
        result.setThread(ctx,active_thread);
    }
    @Override
    public void exitProgram(MyLangParser.ProgramContext ctx){
        result.setThread(ctx,active_thread);
    }

    public void setType(ParseTree node, MyType type) {
        this.result.setType(node, type);
    }
    public MyType getType (ParseTree node) {
        return this.result.getType(node);
    }
    public void setOffset(ParseTree node, int offset) {
        this.result.setOffset(node, offset);
    }
    public int getOffset(ParseTree node){return this.result.getOffset(node);}

    public List<String> getErrors() {
        return this.errors;
    }
    public Set<String> getScopeErrors(){

        return scope.errors;
    }


}




