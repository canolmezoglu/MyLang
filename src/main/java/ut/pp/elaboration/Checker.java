package ut.pp.elaboration;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
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
    /**
     * Class to handle TypeCheck, ScopeCheck,Threading etc
     */
    private List<String> errors;
    private Result result;
    private ScopeTable scope;
    FunctionData currFunction;
    private ThreadSp active_thread;
    private List<ThreadSp> threads;
    private HashMap<String, ArraySp> arrays;

    /**
     * Returns the number of threads defined in the program
     * @param tree Antlr ParseTree
     * @return number of thread occurrences
     */
    public static int getNumberOfThreads(ParseTree tree){
        int occurences = 0;
        String[] keywords = tree.toStringTree().split(" ");
        for (String keyword : keywords){
            if (keyword.equals("thread")) occurences++;
        }

        return occurences;

    }

    /**
     * Walk the ParseTree and Check for correctness
     * @param tree Antlr ParseTree
     * @return Result object
     * @throws Exception if any errors are down and displays those errors
     */
    public Result check (ParseTree tree) throws Exception {
        this.result = new Result();
        this.errors = new ArrayList<String>();
        this.scope = new ScopeTable(getNumberOfThreads(tree));
        this.threads = new ArrayList<>();
        new ParseTreeWalker().walk(this, tree);
        this.errors.addAll((scope.errors));
        String errors = String.join("\n", this.errors);
        if (!this.errors.isEmpty()){
            throw new Exception(errors);
        }
        return this.result;
    }

    /**
     * Checker for a Dynamic Array
     * @param iden name of array
     * @param ctx
     * @return boolean dynamic
     */
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

    /**
     * Enter program and add active threads in the program
     * @param ctx
     */
    @Override public void enterProgram(MyLangParser.ProgramContext ctx){
        active_thread = new ThreadSp(0,0);
        threads.add(active_thread);

    }

    /**
     * Checker for functions
     * @param ctx
     */
    @Override public void exitRunProcedureConstruct(MyLangParser.RunProcedureConstructContext ctx){
        if (ctx.factor() instanceof MyLangParser.FuncCallContext){
           String functionname =  ((MyLangParser.FuncCallContext) ctx.factor()).ID().toString();
           FunctionData functionData =result.getFunctionData(functionname);
           if (functionData.returnType != MyType.VOID){
               this.errors.add("Error:Run does not run any not void functions at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());

           }
        }
        else
        {
            this.errors.add("Error:Run does not run any not void functions at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
        }
    }

    /**
     * Checker for a prefix operator -MINUS,NOT
     * @param ctx
     */
    @Override public void exitPrefixFactor(MyLangParser.PrefixFactorContext ctx){
        if (ctx.prefixOp().MINUS() != null && getType(ctx.factor())!= MyType.NUM){

                this.errors.add("Prefix operation has type mismatch, expected int, got bool at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
        }
        else if (ctx.prefixOp().NOT() != null && getType(ctx.factor() )!= MyType.BOOLEAN){
            this.errors.add("Prefix operation has type mismatch, expected bool, got int at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
        }
        setType(ctx, getType(ctx.factor()));
    }

    /**
     * Checker for a Term - AND,OR
     * @param ctx
     */
    @Override public void exitTerm(MyLangParser.TermContext ctx){
        if (ctx.mult()!=null || ctx.AND()!=null)  {
            if (getType(ctx.factor()) != getType(ctx.term())) {
                if (ctx.AND()!=null ){
                    this.errors.add("AND has type mismatch at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
                }
                else {
                    this.errors.add("Multiplication has type mismatch at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
                }
            }
            else{
                if (getType(ctx.factor()) != MyType.NUM && ctx.mult() != null){
                    this.errors.add("Error: Multiplication or division only takes integers at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
                }
                else if (getType(ctx.factor()) != MyType.BOOLEAN && ctx.AND() != null){
                    this.errors.add("Error: AND only takes booleans at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
                }
            }
        }
        setType(ctx, getType(ctx.factor()));
    }

    /**
     * Checker for a superior expression - PLUS,MINUS,OR
     * @param ctx
     */
    @Override public void exitSuperiorExpr(MyLangParser.SuperiorExprContext ctx){
        if (ctx.superiorExpr() != null) {
            if (getType(ctx.term()) == getType(ctx.superiorExpr())) {

                if (ctx.addOp() != null) {
                    if (ctx.addOp().PLUS() != null && getType(ctx.term()) != MyType.NUM) {
                        this.errors.add("Error: addition has type mismatch at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
                    } else if (ctx.addOp().MINUS() != null && getType(ctx.term()) != MyType.NUM) {
                        this.errors.add("Error: subtraction has type mismatch at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
                    }
                }
                else if (ctx.OR() != null && getType(ctx.term()) != MyType.BOOLEAN) {
                    this.errors.add("Error: or has type mismatch at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
                }
            }
            else{
                this.errors.add("Error: types do not match in one of the following expressions at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine() );
            }
        }
        setType(ctx, getType(ctx.term()));
    }

    /**
     * Checker for a program expression -EQ,NE
     * @param ctx
     */
    @Override public void exitExpr(MyLangParser.ExprContext ctx){
        if (ctx.superiorExpr().size() == 1 ){
            setType(ctx,getType(ctx.superiorExpr(0)));
        }
        else  {
            MyType leftType = getType(ctx.superiorExpr(0));
            MyType rightType = getType(ctx.superiorExpr(1));
            if (ctx.comp().EQ() != null || ctx.comp().NE() != null) {
                if (leftType != rightType) {
                    this.errors.add("Error: you cannot compare different types in terms" +
                            "of being equal at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
                }
            } else if (leftType != MyType.NUM || rightType != MyType.NUM) {
                this.errors.add("Error: you cannot compare boolean types at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
            }
            setType(ctx, MyType.BOOLEAN);
        }

    }

    /**
     * Checker for an expression inside parenthesis
     * @param ctx
     */
    @Override public void exitParFactor(MyLangParser.ParFactorContext ctx){
        setType(ctx,getType(ctx.expr()));
    }

    /**
     * Checker for a Primitiv Factor
     * @param ctx
     */
    @Override public void exitPrimitiveFactor(MyLangParser.PrimitiveFactorContext ctx){
        setType(ctx,getType(ctx.primitive()));
    }

    /**
     * Checker for a factor inside a function
     * @param ctx
     */
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

    /**
     * Checker for a Primitive -NUM,BOOL
     * @param ctx
     */
    @Override public void exitPrimitive(MyLangParser.PrimitiveContext ctx) {
        if (ctx.NUM() != null) {
            try{
                Integer.parseInt(ctx.getText());
            }
            catch (NumberFormatException e){
                this.errors.add("Error: This int defined is larger than the limits at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
            }
            result.setType(ctx,MyType.NUM);

        }
        else if (ctx.booleanVal() != null) {
            result.setType(ctx,MyType.BOOLEAN);
        }
    }

    /**
     * Add a syntax error if the error node is visited
     * @param node
     */
    @Override
    public void visitErrorNode(ErrorNode node) {
        this.errors.add("SyntaxError: at Line: "+node.getSymbol().getLine() + " Character: "+node.getSymbol().getCharPositionInLine());
    }

    /**
     * Checker for a changing a value - variables,arrays,pointers
     * @param ctx
     */
    @Override public void exitChangeAss(MyLangParser.ChangeAssContext ctx) {
        if(ctx.ID().getText().contains(".")){
            this.errors.add("Error: Enum values are fixed: they cannot be updated at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
        }
        String iden = ctx.ID().toString();

        VariableData type = null;
        if (this.currFunction !=null){
            if(iden.contains("&")){ //this is a pointer
                type = this.currFunction.getVariable(iden.substring(0,iden.length()-1));
                if(type.type != getType(ctx.expr())){
                    this.errors.add("Error: You are assigning an invalid type to the pointer-variable at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
                }
                return;
            }
            if (!checkDynamicArray(iden,ctx)) {
                type = this.currFunction.getVariable(iden);
                if (type != null) {
                    if (type.type != getType(ctx.expr())) {
                        this.errors.add("Error: you are changing a variable to an unexpected type at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
                    }
                    setType(ctx, getType(ctx.expr()));
                    result.setGlobal(ctx, false);
                    setOffset(ctx, type.getSizeCurr());
                    return;
                }
            }
        }
        else {
            if (iden.contains("&")) { //this is a pointer
                type = scope.check(iden.substring(0, iden.length() - 1), ctx.getStart());
                if (type.type != getType(ctx.expr())) {
                    this.errors.add("Error: You are assigning an invalid type to the pointer-variable at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
                }
                return;
            }



            }
            if (!checkDynamicArray(iden,ctx)) {
                type = scope.check(ctx.ID().toString(), ctx.getStart());
                if (type != null) {
                    if (type.type != getType(ctx.expr())) {
                        this.errors.add("Error: you are changing a variable to an unexpected type at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
                    }
                    setType(ctx, getType(ctx.expr()));
                    setOffset(ctx, type.getSizeCurr());
                    result.setGlobal(ctx, type.global);
                    return;
                }
                this.errors.add("Error: this variable you are changing does not exist at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
            }
        }


    /**
     * Checker for a declaration of a variable
     * @param ctx
     */
    @Override public void exitDeclaration(MyLangParser.DeclarationContext ctx) {
            if (ctx.ID().toString().contains("%") || ctx.ID().toString().contains(",") || ctx.ID().toString().contains(".")){
                this.errors.add("Error: Variable cannot contain special character at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
            }
            try{
                if (ctx.type().BOOLEAN() != null) {
                    if (getType(ctx.expr()) != MyType.BOOLEAN) {
                        this.errors.add("Error: you are trying to assign an integer to a boolean variable at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
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
                        this.errors.add("Error: you are trying to assign an boolean to an integer variable at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
                    }
                    setType(ctx, MyType.NUM);
                    if (this.currFunction == null){
                        setOffset(ctx, scope.declare(ctx.ID().toString(), MyType.NUM, ctx.getStart(),ctx.access() != null && ctx.access().SHARED() != null).getSizeCurr());
                    }
                    else{
                        setOffset(ctx,this.currFunction.declare(ctx.ID().toString(),MyType.NUM));
                    }
                } else {
                    this.errors.add("Error: Invalid type at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
                }
            }
            catch (Exception e){
                //Do nothing, errors have already been added by ScopeTable
            }

    }

    /**
     * Checker for a Pointer declaration
     * @param ctx
     */
    @Override
    public void exitDeclarePointer(MyLangParser.DeclarePointerContext ctx) {
        try {
            if (ctx.factor() instanceof MyLangParser.IdFactorContext) {
                VariableData check = scope.check(ctx.factor().getText(), ctx.getStart());
                if (check == null) {
                    this.errors.add("Pointer is pointing to an undefined variable");
                } else if (this.currFunction == null) {
                    setOffset(ctx, scope.declare(ctx.ID().toString(), check.type, ctx.getStart(), false).getSizeCurr());
                    setType(ctx, check.type);
                    setOffset(ctx.factor(), check.getSizeCurr());
                } else {
                    this.currFunction.declare(ctx.ID().toString(), check.type);
                    setType(ctx, check.type);
                }
            }
        }
        catch(Exception e){
            //Do nothing, errors have already been added by ScopeTable
        }
    }

    /**
     * Checker for a 1d array declaration
     * @param ctx
     */
    @Override
    public void exitDeclareArray(MyLangParser.DeclareArrayContext ctx) {
        if(ctx.darray().expr().size()!=Integer.parseInt(ctx.NUM().getText())){
            this.errors.add("Error: The size of the array does not match the number of elements you have listed at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
        }
        try {

            for (int i = 0; i < ctx.darray().expr().size(); i++) {
                if (ctx.type().BOOLEAN() != null) {
                    if (getType(ctx.darray().expr(i)) != MyType.BOOLEAN) {
                        this.errors.add("Error: you are trying to assign an integer to a boolean array at Line: " + ctx.getStart().getLine() + " Character: " + ctx.getStart().getCharPositionInLine());
                    }
                    setType(ctx.darray().expr(i), MyType.BOOLEAN);
                    setOffset(ctx.darray().expr(i), scope.declare(ctx.ID().toString() + "%" + i, MyType.BOOLEAN, ctx.getStart(), ctx.access() != null && ctx.access().SHARED() != null).sizeCurr);
                } else if (ctx.type().INTEGER() != null) {
                    if (getType(ctx.darray().expr(i)) != MyType.NUM) {
                        this.errors.add("Error: you are trying to assign an boolean to an integer array at Line: " + ctx.getStart().getLine() + " Character: " + ctx.getStart().getCharPositionInLine());
                    }
                    setType(ctx.darray().expr(i), MyType.NUM);
                    setOffset(ctx.darray().expr(i), scope.declare(ctx.ID().toString() + "%" + i, MyType.NUM, ctx.getStart(), ctx.access() != null && ctx.access().SHARED() != null).sizeCurr);
                } else {
                    this.errors.add("Error: Invalid type at Line: " + ctx.getStart().getLine() + " Character: " + ctx.getStart().getCharPositionInLine());
                }
            }
        }
        catch(Exception e){
            //Do nothing, errors have already been added by ScopeTable
        }
    }

    /**
     * Checker for a 2d array declaration
     * @param ctx
     */
    @Override
    public void exitDeclare2dArray(MyLangParser.Declare2dArrayContext ctx) {
        List<MyLangParser.DarrayContext> rows_list = ctx.darray();
        try {

            if (rows_list.size() != Integer.parseInt(ctx.NUM(0).getText())) {
                this.errors.add("Error: The number of rows does not match at Line: " + ctx.getStart().getLine() + " Character: " + ctx.getStart().getCharPositionInLine());
            }
            String array_name = ctx.ID().toString();
            for (int i = 0; i < rows_list.size(); i++) {
                if (rows_list.get(i).expr().size() != Integer.parseInt(ctx.NUM(1).getText())) {
                    this.errors.add("Error: The number of columns does not match at Line: " + ctx.getStart().getLine() + " Character: " + ctx.getStart().getCharPositionInLine());
                }
                for (int j = 0; j < rows_list.get(i).expr().size(); j++) {
                    if (ctx.type().BOOLEAN() != null) {
                        if (getType(ctx.darray(i).expr(j)) != MyType.BOOLEAN) {
                            this.errors.add("Error: you are trying to assign an integer to a boolean array at Line: " + ctx.getStart().getLine() + " Character: " + ctx.getStart().getCharPositionInLine());
                        }
                        setType(ctx.darray(i).expr(j), MyType.BOOLEAN);
                        VariableData var = scope.declare(array_name + "%" + i + "%" + j, MyType.BOOLEAN, ctx.getStart(), ctx.access() != null && ctx.access().SHARED() != null);
                        setOffset(ctx.darray(i).expr(j), var.getSizeCurr());
                        var.setColumnCount(rows_list.get(i).expr().size());
                    } else if (ctx.type().INTEGER() != null) {
                        if (getType(ctx.darray(i).expr(j)) != MyType.NUM) {
                            this.errors.add("Error: you are trying to assign an boolean to an integer array at Line: " + ctx.getStart().getLine() + " Character: " + ctx.getStart().getCharPositionInLine());
                        }
                        setType(ctx.darray(i).expr(j), MyType.NUM);
                        VariableData var = scope.declare(array_name + "%" + i + "%" + j, MyType.NUM, ctx.getStart(), ctx.access() != null && ctx.access().SHARED() != null);
                        setOffset(ctx.darray(i).expr(j), var.getSizeCurr());
                        var.setColumnCount(rows_list.get(i).expr().size());

                    } else {
                        this.errors.add("Error: Invalid type at Line: " + ctx.getStart().getLine() + " Character: " + ctx.getStart().getCharPositionInLine());
                    }
                }
            }
        }
        catch (Exception e){
            //Do nothing, errors have already been added by ScopeTable
        }
    }

    /**
     * Checker for an enum declaration
     * @param ctx
     */
    @Override
    public void exitDeclareEnum(MyLangParser.DeclareEnumContext ctx) {
        try {
            for (int i = 0; i < ctx.enumAssign().expr().size(); i++) {
                if (ctx.type().BOOLEAN() != null) {
                    if (getType(ctx.enumAssign().expr(i)) != MyType.BOOLEAN) {
                        this.errors.add("Error: you are trying to assign an invalid type to a boolean enum at Line: " + ctx.getStart().getLine() + " Character: " + ctx.getStart().getCharPositionInLine());
                    }
                    setType(ctx.enumAssign().expr(i), MyType.BOOLEAN);
                    setOffset(ctx.enumAssign().expr(i), scope.declare(ctx.ID().toString() + "." + ctx.enumAssign().ID(i).getText(), MyType.BOOLEAN, ctx.getStart(), ctx.access() != null && ctx.access().SHARED() != null).sizeCurr);
                } else if (ctx.type().INTEGER() != null) {
                    if (getType(ctx.enumAssign().expr(i)) != MyType.NUM) {
                        this.errors.add("Error: you are trying to assign an invalid type to an integer enum at Line: " + ctx.getStart().getLine() + " Character: " + ctx.getStart().getCharPositionInLine());
                    }
                    setType(ctx.enumAssign().expr(i), MyType.NUM);
                    setOffset(ctx.enumAssign().expr(i), scope.declare(ctx.ID().toString() + "." + ctx.enumAssign().ID(i).getText(), MyType.NUM, ctx.getStart(), ctx.access() != null && ctx.access().SHARED() != null).sizeCurr);
                } else {
                    this.errors.add("Error: Invalid type at Line: " + ctx.getStart().getLine() + " Character: " + ctx.getStart().getCharPositionInLine());
                }
            }
        }
        catch(Exception e){
            //Do nothing, errors have already been added by ScopeTable
        }
    }

    /**
     * Open a scope when you enter an if block
     * @param ctx
     */
    @Override
    public void enterIfConstruct(MyLangParser.IfConstructContext ctx) {
        scope.openScope();
    }

    /**
     * Checker for if condition and close scope for this if block
     * @param ctx
     */
    @Override public void exitIfConstruct(MyLangParser.IfConstructContext ctx){
        scope.closeScope();
        result.setThread(ctx,active_thread);

        if (getType(ctx.expr()) != MyType.BOOLEAN ){
            this.errors.add("Error: if statement can only check a boolean at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
        }
    }

    /**
     * Open scope when you enter a while block
     * @param ctx
     */
    @Override
    public void enterWhileConstruct(MyLangParser.WhileConstructContext ctx) {
        scope.openScope();
    }

    /**
     * Checker for while condition and close scope for this while loop
     * @param ctx
     */
    @Override public void exitWhileConstruct(MyLangParser.WhileConstructContext ctx){
        scope.closeScope();
        result.setThread(ctx,active_thread);

        if (getType(ctx.expr()) != MyType.BOOLEAN ){
            this.errors.add("Error: while statement can only check a boolean at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
        }
    }

    /**
     * Set this statement to this active thread
     * @param ctx
     */
    @Override public void exitStatementInst(MyLangParser.StatementInstContext ctx){
        result.setThread(ctx.statement(),active_thread);
    }

    /**
     * Add all thread children to thread when a Parallel block is entered
     * @param ctx
     */
    @Override public void exitParallelConstruct(MyLangParser.ParallelConstructContext ctx){
        for (MyLangParser.ThreadConstructContext threadConstructContext : ctx.threadConstruct()){
            result.addChild(ctx,result.getThread(threadConstructContext).getThreadnr());
        }
    }

    /**
     * Add a child to the in the parllel block thread
     * @param ctx
     */
    @Override public void exitParallelInst(MyLangParser.ParallelInstContext ctx){
        result.addChild(ctx,result.getChildren(ctx.parallelConstruct()));
    }

    /**
     * Checker for the Thread Construct and open a new scope for this thread
     * @param ctx
     */
    @Override
    public void enterThreadConstruct(
            MyLangParser.ThreadConstructContext ctx
    ) {
        scope.openScope();

        active_thread = new ThreadSp(threads.size(),active_thread.getThreadnr());
        threads.get(active_thread.getParentnr()).addchild(active_thread);
        threads.add(active_thread);
    }

    /**
     * Close scope and keep track of threads when you exit the thread construct
     * @param ctx
     */
    @Override
    public void exitThreadConstruct(MyLangParser.ThreadConstructContext ctx) {
        result.setThread(ctx,active_thread);
        active_thread = threads.get(active_thread.getParentnr());
        scope.closeScope();

    }

    /**
     * Set the active thread when a print statement is visited
     * @param ctx
     */
    @Override
    public void exitPrintConstruct(MyLangParser.PrintConstructContext ctx){
        result.setThread(ctx,active_thread);
    }

    /**
     * Set active thread when Program ends
     * @param ctx
     */
    @Override
    public void exitProgram(MyLangParser.ProgramContext ctx){
        result.setThread(ctx,active_thread);
    }

    /**
     * Checker for return statement inside a function
     * @param ctx
     */
    @Override
    public void exitReturnConstruct(MyLangParser.ReturnConstructContext ctx){
        if (this.currFunction !=null){
            if (this.currFunction.returnType == MyType.VOID){
                if ( ctx.expr()!= null)
                this.errors.add("A function that is void is trying to return a" + getType(ctx.expr()) + "at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine() );
            }
            else if (this.currFunction.returnType != getType(ctx.expr())){
                this.errors.add("Error: A function claims to return " + this.currFunction.returnType.toString() +
                        " but actually returns " + getType(ctx.expr()) + "at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine() );
            }
        }
        else{
            this.errors.add("Error: a return statement is called outside a function at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
        }
    }

    /**
     * Checker for a function definition
     * @param ctx
     */
    @Override
    public void enterFunctionConstruct(MyLangParser.FunctionConstructContext ctx){
        MyType mytype = null;
        if (ctx.type(0).VOID() !=null) mytype = MyType.VOID;
        if (ctx.type(0).INTEGER() !=null) mytype = MyType.NUM;
        if (ctx.type(0).BOOLEAN() !=null) mytype = MyType.BOOLEAN;

        this.currFunction = new FunctionData(mytype);

        if (ctx.ID() != null){

            for (int i=1; i < ctx.ID().size();i++){
                if (ctx.type(i).VOID() !=null) errors.add("Error : You cannot have void parameters at line: "  + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
                MyType type = ctx.type(i).getText() .equals( "int" ) ? MyType.NUM : MyType.BOOLEAN;
                if (ctx.ID(i).toString().contains("&")){
                    this.currFunction.addParameter(ctx.ID(i).toString().substring(0,ctx.ID(i).toString().length()-1),type,true);
                }
                else{
                    this.currFunction.addParameter(ctx.ID(i).toString(),type,false);

                }
            }

        }
        else{
            this.errors.add("Error: function has not been named at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
        }
    }

    /**
     * Checker for a function call - enter
     * @param ctx
     */
    @Override
    public void enterFuncCall(MyLangParser.FuncCallContext ctx) {
        if (this.currFunction !=null) return;


        if (!result.functionDataHashMapContains(ctx.ID().toString())) {
            this.errors.add("Error: you are calling a function that does not exist yet at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
        }
    }

    /**
     * Checker for a function call - exit
     * @param ctx
     */
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
                this.errors.add("Error: the parameter type not equal to expected type at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
            }

        }

    }

    /**
     * Checker for function construct in the program
     * @param ctx
     */
    @Override
    public void exitFunctionConstruct(MyLangParser.FunctionConstructContext ctx){
        if (this.result.functionDataHashMapContains(ctx.ID(0).toString())){
            this.errors.add("Error: you cannot have two functions with the same name at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
        }
        if (ctx.block().instruction(ctx.block().instruction().size()-1) instanceof MyLangParser.ReturnInstContext) {
            this.currFunction.setLastLineHasReturn(true);

        }
        else if (this.currFunction.returnType == MyType.NUM ||
                this.currFunction.returnType == MyType.BOOLEAN
        ){
            this.errors.add("Error: You cannot have an integer or boolean function that returns nothing at Line: " + ctx.getStart().getLine()+" Character: "+ctx.getStart().getCharPositionInLine());
        }

        this.result.putFunctionDataMap(ctx.ID(0).toString(),this.currFunction);
        this.currFunction = null;
    }

    /**
     * Set the type of the parse tree node to MyType
     * @param node parsetree node
     * @param type MuType object
     */
    public void setType(ParseTree node, MyType type) {
        this.result.setType(node, type);
    }

    /**
     * Get type of this parsetree node
     * @param node parsetree node
     * @return MyType object - type of the parse tree node
     */
    public MyType getType (ParseTree node) {
        return this.result.getType(node);
    }

    /**
     * Set the offset value of a parsetree node
     * @param node parsetree node
     * @param offset int offset value
     */
    public void setOffset(ParseTree node, int offset) {
        this.result.setOffset(node, offset);
    }

    /**
     * Get the offset value of the parsetree node
     * @param node parsetree node
     * @return offset value of this parse tree node
     */
    public int getOffset(ParseTree node){return this.result.getOffset(node);}

    /**
     * Get all errors in the program
     * @return list of error messages
     */
    public List<String> getErrors() {
        return this.errors;
    }

    /**
     * Get all Scope errors in the program
     * @return list of scope check errors
     */
    public Set<String> getScopeErrors(){

        return scope.errors;
    }



}




