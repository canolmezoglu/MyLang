package ut.pp.elaboration;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import ut.pp.elaboration.model.ArraySp;
import ut.pp.elaboration.model.ThreadSp;
import ut.pp.elaboration.model.enums.Registers;
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
    private HashMap<String, ArraySp> arrays;

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
    public boolean checkDynamicArray(String iden, ParserRuleContext ctx) {
        boolean dynamicArray = false;
        VariableData mainVariableData = null;
        String[] arrayDynamic = iden.split("%");
        if (arrayDynamic.length > 1) {
            if (arrayDynamic.length < 3 && Character.isLetter(arrayDynamic[1].charAt(0))) {
                VariableData variableData = scope.check(arrayDynamic[0] + "%" + "0" , ctx.getStart());
                ArraySp arraySp =
                        new ArraySp(variableData.getSizeCurr());
                arraySp.addPointer(true, scope.check(arrayDynamic[1], ctx.getStart()).getSizeCurr());
                mainVariableData = variableData;
                result.addDynamicArrayCall(ctx, arraySp);
                dynamicArray = true;


            } else if (arrayDynamic.length == 3) {
                if (Character.isLetter(arrayDynamic[1].charAt(0)) && Character.isLetter(arrayDynamic[2].charAt(0))) {
                    VariableData variableData = scope.check(arrayDynamic[0] + "%" + "0"  + "%" + "0"  , ctx.getStart());
                    ArraySp arraySp =
                            new ArraySp(variableData.getSizeCurr());
                    arraySp.addPointer(
                            true, scope.check(arrayDynamic[1], ctx.getStart()).getSizeCurr(),
                            true, scope.check(arrayDynamic[2], ctx.getStart()).getSizeCurr());
                    result.addDynamicArrayCall(ctx, arraySp);
                    arraySp.setColumnSize(variableData.getColumnCount());
                    mainVariableData = variableData;

                    dynamicArray = true;

                } else if (Character.isLetter(arrayDynamic[1].charAt(0))) {
                    VariableData variableData = scope.check(arrayDynamic[0] + "%" + "0"  + "%" + "0"  ,  ctx.getStart());
                    ArraySp arraySp =
                            new ArraySp(variableData.getSizeCurr());
                    arraySp.addPointer(
                            true, scope.check(arrayDynamic[1], ctx.getStart()).getSizeCurr(),
                            false, Integer.parseInt(arrayDynamic[2]));
                    result.addDynamicArrayCall(ctx, arraySp);
                    arraySp.setColumnSize(variableData.getColumnCount());
                    mainVariableData = variableData;
                    dynamicArray = true;


                } else if (Character.isLetter(arrayDynamic[2].charAt(0))) {
                    VariableData variableData = scope.check(arrayDynamic[0] + "%" + "0"  + "%" + "0"   , ctx.getStart());
                    ArraySp arraySp =
                            new ArraySp(variableData.getSizeCurr());
                    arraySp.addPointer(
                            false, Integer.parseInt(arrayDynamic[1]),
                            true, scope.check(arrayDynamic[2], ctx.getStart()).getSizeCurr());
                    result.addDynamicArrayCall(ctx, arraySp);
                    arraySp.setColumnSize(variableData.getColumnCount());
                    mainVariableData = variableData;
                    dynamicArray = true;
                }
            }
        }
        if (dynamicArray){
            setType(ctx,mainVariableData.type);
            result.setGlobal(ctx, mainVariableData.global);

        }
        return dynamicArray;
    }

    @Override public void enterProgram(MyLangParser.ProgramContext ctx){
        active_thread = new ThreadSp(0,0);
        threads.add(active_thread);

    }

    @Override public void exitPrefixFactor(MyLangParser.PrefixFactorContext ctx){
        if (ctx.prefixOp().MINUS() != null && getType(ctx.factor())!= MyType.NUM){

                this.errors.add("Prefix operation has type mismatch, expected int, got bool");
        }
        else if (ctx.prefixOp().NOT() != null && getType(ctx.factor() )!= MyType.BOOLEAN){
            this.errors.add("Prefix operation has type mismatch, expected bool, got int");
        }
        setType(ctx, getType(ctx.factor()));
    }
    @Override public void exitTerm(MyLangParser.TermContext ctx){
        if (ctx.mult()!=null || ctx.AND()!=null)  {
            if (getType(ctx.factor()) != getType(ctx.term())) {
                if (ctx.AND()!=null ){
                    this.errors.add("AND has type mismatch");
                }
                else {
                    this.errors.add("Multiplication has type mismatch");
                }
            }
            else{
                if (getType(ctx.factor()) != MyType.NUM && ctx.mult() != null){
                    this.errors.add("Multiplication or division only takes integers");
                }
                else if (getType(ctx.factor()) != MyType.BOOLEAN && ctx.AND() != null){
                    this.errors.add("AND only takes booleans");
                }
            }
        }
        setType(ctx, getType(ctx.factor()));
    }
    @Override public void exitSuperiorExpr(MyLangParser.SuperiorExprContext ctx){
        if (ctx.superiorExpr() != null) {
            if (getType(ctx.term()) == getType(ctx.superiorExpr())) {

                if (ctx.addOp() != null) {
                    if (ctx.addOp().PLUS() != null && getType(ctx.term()) != MyType.NUM) {
                        this.errors.add("addition has type mismatch");
                    } else if (ctx.addOp().MINUS() != null && getType(ctx.term()) != MyType.NUM) {
                        this.errors.add("subtraction has type mismatch");
                    }
                }
                else if (ctx.OR() != null && getType(ctx.term()) != MyType.BOOLEAN) {
                    this.errors.add("or has type mismatch");
                }
            }
            else{
                this.errors.add("types do not match in one of the following expressions -> addition, subtraction, or" );
            }
        }
        setType(ctx, getType(ctx.term()));
    }

    @Override public void exitExpr(MyLangParser.ExprContext ctx){
        if (ctx.superiorExpr().size() == 1 ){
            setType(ctx,getType(ctx.superiorExpr(0)));
        }
        else  {
            MyType leftType = getType(ctx.superiorExpr(0));
            MyType rightType = getType(ctx.superiorExpr(1));
            if (ctx.comp().EQ() != null || ctx.comp().NE() != null) {
                if (leftType != rightType) {
                    this.errors.add("you cannot compare different types in terms" +
                            "of being equal");
                }
            } else if (leftType != MyType.NUM || rightType != MyType.NUM) {
                this.errors.add("you cannot compare boolean types");
            }
            setType(ctx, MyType.BOOLEAN);
        }

    }

    @Override public void exitParFactor(MyLangParser.ParFactorContext ctx){
        setType(ctx,getType(ctx.expr()));
    }
    @Override public void exitPrimitiveFactor(MyLangParser.PrimitiveFactorContext ctx){
        setType(ctx,getType(ctx.primitive()));
    }


    @Override public void exitIdFactor(MyLangParser.IdFactorContext ctx){
        String iden = ctx.ID().toString();
        VariableData type = null;
        if (this.currFunction !=null){
            if(iden.contains("&")){ //this is a pointer
                setType(ctx,this.currFunction.getVariable(iden.substring(0,iden.length()-1)).type);
                return;
            }
            setType(ctx,this.currFunction.getVariable(iden).type);
            setOffset(ctx,this.currFunction.getVariable(iden).getSizeCurr());
            result.setGlobal(ctx,false);
            return;
        }
        else {
            if (iden.contains("&")) { //this is a pointer
                type = scope.check(iden.substring(0, iden.length() - 1), ctx.getStart());
                if (type != null) {
                    setType(ctx, type.type);
                }
                return;
            }
            if (!checkDynamicArray(iden, ctx)) {
                type = scope.check(iden, ctx.getStart());
                if (type != null) {
                    setType(ctx, type.type);
                    setOffset(ctx, type.getSizeCurr());
                    result.setGlobal(ctx, type.global);
                }
            }
        }
    }
    @Override public void exitPrimitive(MyLangParser.PrimitiveContext ctx) {
        if (ctx.NUM() != null) {
            try{
                Integer.parseInt(ctx.getText());
            }
            catch (NumberFormatException e){
                this.errors.add("This int defined is larger than the limits");
            }
            result.setType(ctx,MyType.NUM);

        }
        else if (ctx.booleanVal() != null) {
            result.setType(ctx,MyType.BOOLEAN);
        }
    }



    @Override public void exitChangeAss(MyLangParser.ChangeAssContext ctx) {
        if(ctx.ID().getText().contains(".")){
            this.errors.add("Enum values are fixed: they cannot be updated");
        }
        String iden = ctx.ID().toString();

        VariableData type = null;
        if (this.currFunction !=null){
            if(iden.contains("&")){ //this is a pointer
                type = this.currFunction.getVariable(iden.substring(0,iden.length()-1));
                if(type.type != getType(ctx.expr())){
                    this.errors.add("You are assigning an invalid type to the pointer-variable");
                }
                return;
            }
            if (!checkDynamicArray(iden,ctx)) {

                type = this.currFunction.getVariable(iden);
                if (type != null) {
                    if (type.type != getType(ctx.expr())) {
                        this.errors.add("you are changing a variable to an unexpected type");
                    }
                    setType(ctx, getType(ctx.expr()));
                    result.setGlobal(ctx, false);
                    setOffset(ctx, this.currFunction.getLocalDataSize());
                    return;
                }
            }
        }
        else {
            if (iden.contains("&")) { //this is a pointer
                type = scope.check(iden.substring(0, iden.length() - 1), ctx.getStart());
                if (type.type != getType(ctx.expr())) {
                    this.errors.add("You are assigning an invalid type to the pointer-variable");
                }
                return;
            }



            }
            if (!checkDynamicArray(iden,ctx)) {
                type = scope.check(ctx.ID().toString(), ctx.getStart());
                if (type != null) {
                    if (type.type != getType(ctx.expr())) {
                        this.errors.add("you are changing a variable to an unexpected type");
                    }
                    setType(ctx, getType(ctx.expr()));
                    setOffset(ctx, type.getSizeCurr());
                    result.setGlobal(ctx, type.global);
                    return;
                }
                this.errors.add("this variable you are changing does not exist");
            }
        }




    @Override public void exitDeclaration(MyLangParser.DeclarationContext ctx) {
            if (ctx.ID().toString().contains("%") || ctx.ID().toString().contains(",") || ctx.ID().toString().contains(".")){
                this.errors.add("Variable cannot contain special character");
            }
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
    public void exitDeclarePointer(MyLangParser.DeclarePointerContext ctx) {
        if(ctx.factor() instanceof MyLangParser.IdFactorContext){
            VariableData check = scope.check(ctx.factor().getText(),ctx.getStart());
            if(check==null){
                this.errors.add("Pointer is pointing to an undefined variable");
            }
            else if (this.currFunction == null){
                scope.declare(ctx.ID().toString(),check.type, ctx.getStart(),ctx.access() != null && ctx.access().SHARED() != null);
                setType(ctx, check.type);
            }
            else{
                this.currFunction.declare(ctx.ID().toString(),check.type);
                setType(ctx, check.type);
            }
        }
        else{
            this.errors.add("Pointer should be assigned to a variable");
        }
    }
    @Override
    public void exitDeclareArray(MyLangParser.DeclareArrayContext ctx) {
        if(ctx.darray().expr().size()!=Integer.parseInt(ctx.NUM().getText())){
            this.errors.add("The size of the array does not match the number of elements you have listed");
        }
        for(int i=0;i<ctx.darray().expr().size();i++) {
            if (ctx.type().BOOLEAN() != null) {
                if (getType(ctx.darray().expr(i)) != MyType.BOOLEAN) {
                    this.errors.add("you are trying to assign an integer to a boolean array");
                }
                setType(ctx.darray().expr(i), MyType.BOOLEAN);
                setOffset(ctx.darray().expr(i), scope.declare(ctx.ID().toString()+"%"+i, MyType.BOOLEAN, ctx.getStart(), ctx.access() != null && ctx.access().SHARED() != null).sizeCurr);
            } else if (ctx.type().INTEGER() != null) {
                if (getType(ctx.darray().expr(i)) != MyType.NUM) {
                    this.errors.add("you are trying to assign an boolean to an integer array");
                }
                setType(ctx.darray().expr(i), MyType.NUM);
                setOffset(ctx.darray().expr(i), scope.declare(ctx.ID().toString()+"%"+i, MyType.NUM, ctx.getStart(), ctx.access() != null && ctx.access().SHARED() != null).sizeCurr);
            } else {
                this.errors.add("Invalid type");
            }
        }
    }
    @Override
    public void exitDeclare2dArray(MyLangParser.Declare2dArrayContext ctx) {
        List<MyLangParser.DarrayContext> rows_list = ctx.darray();
        if(rows_list.size()!=Integer.parseInt(ctx.NUM(0).getText())){
            this.errors.add("The number of rows does not match");
        }
        String array_name = ctx.ID().toString();
        for(int i=0;i< rows_list.size();i++){
            if(rows_list.get(i).expr().size()!=Integer.parseInt(ctx.NUM(1).getText())){
                this.errors.add("The number of columns does not match");
            }
            for(int j=0;j<rows_list.get(i).expr().size();j++){
                if (ctx.type().BOOLEAN() != null) {
                    if (getType(ctx.darray(i).expr(j)) != MyType.BOOLEAN) {
                        this.errors.add("you are trying to assign an integer to a boolean array");
                    }
                    setType(ctx.darray(i).expr(j), MyType.BOOLEAN);
                    VariableData var = scope.declare(array_name+"%"+i+"%"+j, MyType.BOOLEAN, ctx.getStart(), ctx.access() != null && ctx.access().SHARED() != null);
                    setOffset(ctx.darray(i).expr(j), var.getSizeCurr());
                    var.setColumnCount(rows_list.get(i).expr().size());
                } else if (ctx.type().INTEGER() != null) {
                    if (getType(ctx.darray(i).expr(j)) != MyType.NUM) {
                        this.errors.add("you are trying to assign an boolean to an integer array");
                    }
                    setType(ctx.darray(i).expr(j), MyType.NUM);
                    VariableData var = scope.declare(array_name+"%"+i+"%"+j, MyType.NUM, ctx.getStart(), ctx.access() != null && ctx.access().SHARED() != null);
                    setOffset(ctx.darray(i).expr(j),var.getSizeCurr());
                    var.setColumnCount(rows_list.get(i).expr().size());

                } else {
                    this.errors.add("Invalid type");
                }
            }
        }
    }
    @Override
    public void exitDeclareEnum(MyLangParser.DeclareEnumContext ctx) {
        for(int i=0;i<ctx.enumAssign().expr().size();i++) {
            if (ctx.type().BOOLEAN() != null) {
                if (getType(ctx.enumAssign().expr(i)) != MyType.BOOLEAN) {
                    this.errors.add("you are trying to assign an invalid type to a boolean enum");
                }
                setType(ctx.enumAssign().expr(i), MyType.BOOLEAN);
                setOffset(ctx.enumAssign().expr(i), scope.declare(ctx.ID().toString()+"."+ctx.enumAssign().ID(i).getText(), MyType.BOOLEAN, ctx.getStart(), ctx.access() != null && ctx.access().SHARED() != null).sizeCurr);
            } else if (ctx.type().INTEGER() != null) {
                if (getType(ctx.enumAssign().expr(i)) != MyType.NUM) {
                    this.errors.add("you are trying to assign an invalid type to an integer enum");
                }
                setType(ctx.enumAssign().expr(i), MyType.NUM);
                setOffset(ctx.enumAssign().expr(i), scope.declare(ctx.ID().toString()+"."+ctx.enumAssign().ID(i).getText(), MyType.NUM, ctx.getStart(), ctx.access() != null && ctx.access().SHARED() != null).sizeCurr);
            } else {
                this.errors.add("Invalid type");
            }
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
    public void enterFuncCall(MyLangParser.FuncCallContext ctx) {
        if (this.currFunction !=null) return;


        if (!result.functionDataHashMapContains(ctx.ID().toString())) {
            this.errors.add("you are calling a function that doesnt exist yet");
        }
    }
    @Override
    public void exitFuncCall(MyLangParser.FuncCallContext ctx){
        FunctionData functionData = null;
        if (this.currFunction !=null) {
            functionData = this.currFunction;
        }
        else{
            functionData = result.getFunctionData(ctx.ID().toString());
        }
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




