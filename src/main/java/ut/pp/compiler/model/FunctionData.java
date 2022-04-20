package ut.pp.compiler.model;

import ut.pp.compiler.model.enums.MyType;

import java.util.*;

/**
 * FunctionData is a model that stores functions. It
 * is used to send data from the Scanner to the
 * Code Generation.
 */
public class FunctionData {
    Set<String> errors;
    int localDataSize;
    Map<String, VariableData> localScope;
    List<String> parameters;

    public Set<String> getErrors() {
        return errors;
    }

    public void setErrors(Set<String> errors) {
        this.errors = errors;
    }

    public void setLocalDataSize(int localDataSize) {
        this.localDataSize = localDataSize;
    }

    public Map<String, VariableData> getLocalScope() {
        return localScope;
    }

    public void setLocalScope(Map<String, VariableData> localScope) {
        this.localScope = localScope;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public MyType getReturnType() {
        return returnType;
    }

    public void setReturnType(MyType returnType) {
        this.returnType = returnType;
    }

    MyType returnType;

    public boolean isLastLineHasReturn() {
        return lastLineHasReturn;
    }

    public void setLastLineHasReturn(boolean lastLineHasReturn) {
        this.lastLineHasReturn = lastLineHasReturn;
    }

    boolean lastLineHasReturn;
    public FunctionData(MyType returnType){
        this.localDataSize = 0;
        this.localScope = new HashMap<>();
        this.parameters = new ArrayList<>();
        this.returnType = returnType;
        this.errors = new HashSet<>();
    }

    /**
     * Adds a variable data as a parameter,
     * and then changes the pointed
     * object to a parameter.
     * @param id
     * @param type
     * @param pointer
     */
    public void addParameter(String id, MyType type,boolean pointer){
        if (localScope.containsKey(id) ){
            errors.add("You have declared a parameter more than once");
        }
        parameters.add(id);
        VariableData var = new VariableData(type,parameters.size());
        var.makeIntoParameter();
        if (pointer){
            var.makeIntoPointer();
        }
        localScope.put(id,var);

    }

    /**
     * Used to declare a variable inside the function block.
     * Increases the size of the local data so ARP can be created
     * accordingly.
     * @param id
     * @param type
     * @return
     */
    public int declare(String id, MyType type){
        if (localScope.containsKey(id) ){
            errors.add("Scopetable error");
        }
        localScope.put(id,new VariableData(type,++localDataSize));
        return localDataSize;
    }
    public VariableData getVariable(String id) {
        if (localScope.containsKey(id)){
        return localScope.get(id);
    }
        return null;
    }
    public int getLocalDataSize(){
        return this.localDataSize;
    }
}
