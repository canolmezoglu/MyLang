package ut.pp.elaboration;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import ut.pp.elaboration.model.ThreadSp;
import ut.pp.parser.MyLangBaseListener;
import ut.pp.parser.MyLangLexer;
import ut.pp.parser.MyLangParser;
import ut.pp.elaboration.ScopeTable;

import javax.lang.model.element.VariableElement;
import java.util.*;
import java.util.ArrayList;
import java.util.function.Function;

public class Checker extends MyLangBaseListener {
    private List<String> errors;
    private Result result;
    private ScopeTable scope;
    FunctionData currFunction;
    private ThreadSp active_thread;
    private List<ThreadSp> threads;
    public static int getNumberOfThreads(ParseTree tree){
        int occurences = 0;
        String[] keywords = tree.toStringTree().split(" ");
        for (String keyword : keywords){
            if (keyword.equals("thread")) occurences++;
        }
        return occurences;

    }
    public Result check (ParseTree tree) throws Exception {
        this.result = new Result();
        this.errors = new ArrayList<String>();
        this.scope = new ScopeTable(getNumberOfThreads(tree));
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
        if (this.currFunction !=null){
            setType(ctx,this.currFunction.getVariable(ctx.ID().toString()).type);
            setOffset(ctx,this.currFunction.getVariable(ctx.ID().toString()).getSizeCurr());
            result.setGlobal(ctx,false);

            return;
        }
        VariableData check = scope.check(ctx.ID().toString(),ctx.getStart());
        if(check!=null) {
            setType(ctx,check.type);
            setOffset(ctx,check.getSizeCurr());
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
        if (this.currFunction !=null){
            VariableData data = this.currFunction.getVariable(ctx.ID().toString());
            if (data !=null){
                if (data.type != getType(ctx.expr())) {
                    this.errors.add("you are changing a variable to an unexpected type");
                }
                setType(ctx, getType(ctx.expr()));
                result.setGlobal(ctx,false);

                setOffset(ctx,this.currFunction.getLocalDataSize());
                return;

            }
        }
        VariableData check = scope.check(ctx.ID().toString(),ctx.getStart());
        if(check!=null) {
            if (check.type != getType(ctx.expr())) {
                this.errors.add("you are changing a variable to an unexpected type");
            }
            setType(ctx, getType(ctx.expr()));
            setOffset(ctx,check.getSizeCurr());
            result.setGlobal(ctx,check.global);
            return;
        }
        this.errors.add("this variable you are changing does not exist");


    }

    @Override public void exitDeclaration(MyLangParser.DeclarationContext ctx) {

            if (ctx.type().BOOLEAN() != null) {
                if (getType(ctx.expr()) != MyType.BOOLEAN) {
                    this.errors.add("you are trying to assign an integer to a boolean variable");
                }
                setType(ctx, MyType.BOOLEAN);
                if (this.currFunction == null){
                    setOffset(ctx, scope.declare(ctx.ID().toString(), MyType.BOOLEAN, ctx.getStart(),ctx.access() != null && ctx.access().SHARED() != null).getSizeCurr());
                }
                else{
                    setOffset(ctx,this.currFunction.declare(ctx.ID().toString(),MyType.BOOLEAN));
                }
            } else if (ctx.type().INTEGER() != null) {
                if (getType(ctx.expr()) != MyType.NUM) {
                    this.errors.add("you are trying to assign an boolean to an integer variable");
                }
                setType(ctx, MyType.NUM);
                if (this.currFunction == null){
                    setOffset(ctx, scope.declare(ctx.ID().toString(), MyType.NUM, ctx.getStart(),ctx.access() != null && ctx.access().SHARED() != null).getSizeCurr());
                }
                else{
                    setOffset(ctx,this.currFunction.declare(ctx.ID().toString(),MyType.NUM));
                }
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
    @Override public void exitParallelConstruct(MyLangParser.ParallelConstructContext ctx){
        for (MyLangParser.ThreadConstructContext threadConstructContext : ctx.threadConstruct()){
            result.addChild(ctx,result.getThread(threadConstructContext).getThreadnr());
        }
    }
    @Override public void exitParallelInst(MyLangParser.ParallelInstContext ctx){
        result.addChild(ctx,result.getChildren(ctx.parallelConstruct()));
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
    @Override
    public void exitReturnConstruct(MyLangParser.ReturnConstructContext ctx){
        if (this.currFunction !=null){
            if (this.currFunction.returnType != getType(ctx.expr())){
                this.errors.add("this functions claims to return" + this.currFunction.returnType.toString() +
                        " but actually returns" + getType(ctx.expr()));
            }
        }
        else{
            this.errors.add("a return statement is called outside a function");
        }
    }

    @Override
    public void enterFunctionConstruct(MyLangParser.FunctionConstructContext ctx){
        this.currFunction = new FunctionData(ctx.type(0).getText() .equals( "int" ) ? MyType.NUM : MyType.BOOLEAN);
        if (ctx.ID() != null){
            //add enough offset to avoid register save + return addr + return val
            // 7 + arp will be the actual address

            for (int i=1; i < ctx.ID().size();i++){
                MyType type = ctx.type(i).getText() .equals( "int" ) ? MyType.NUM : MyType.BOOLEAN;
                this.currFunction.addParameter(ctx.ID(i).toString(),type);
            }

        }
        else{
            this.errors.add("you have not named this function");
        }
    }
    @Override
    public void enterFuncCallExpr(MyLangParser.FuncCallExprContext ctx) {
        if (!result.functionDataHashMapContains(ctx.ID().toString())) {
            this.errors.add("you are calling a function that doesnt exist yet");
        }
    }
    @Override
    public void exitFuncCallExpr(MyLangParser.FuncCallExprContext ctx){
        FunctionData functionData = result.getFunctionData(ctx.ID().toString());
        setType(ctx,functionData.returnType);
        for (int i=0; i < ctx.expr().size();i++){
            if (getType(ctx.expr(0)) != functionData.getVariable(functionData.parameters.get(i)).type){
                this.errors.add("the parameter type not equal to expected type");
            }

        }

    }
    @Override
    public void exitFunctionConstruct(MyLangParser.FunctionConstructContext ctx){
        if (this.result.functionDataHashMapContains(ctx.ID(0).toString())){
            this.errors.add("you cant have two functions with the same name");
        }
        this.result.putFunctionDataMap(ctx.ID(0).toString(),this.currFunction);
        this.currFunction = null;
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




