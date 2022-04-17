package ut.pp.elaboration;

import java.util.*;

public class FunctionData {
    Set<String> errors;
    int localDataSize;
     Map<String,VariableData> localScope;
     List<String> parameters;
     MyType returnType;
    public FunctionData(MyType returnType){
        this.localDataSize = 0;
        this.localScope = new HashMap<>();
        this.parameters = new ArrayList<>();
        this.returnType = returnType;
    }
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
