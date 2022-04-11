package ut.pp.elaboration;

import org.antlr.v4.runtime.Token;

import java.util.*;

public class ScopeTable {
    Set<String> errors;
    List<HashMap<String,VariableData>> scopes;
    List<Integer> sizes;
    List<Integer> globalSizes;
    int scope_num;

    public ScopeTable(){
        this.errors = new HashSet<>();
        this.scopes = new ArrayList<>();
        this.sizes = new ArrayList<>();
        this.globalSizes = new ArrayList<>();
        this.scope_num=0;
        this.scopes.add(new HashMap<>());
        this.sizes.add(0);
        this.globalSizes.add(0);

    }

    public ScopeTable(int globalSizes){
        this.errors = new HashSet<>();
        this.scopes = new ArrayList<>();
        this.sizes = new ArrayList<>();
        this.globalSizes = new ArrayList<>();
        this.scope_num=0;
        this.scopes.add(new HashMap<>());
        this.sizes.add(0);
        this.globalSizes.add(globalSizes);

    }
    public int getPrevSizes(List<Integer> x){
        int total = 0;
        for (int i : x){
            total +=i;
        }
        return total;
    }
    /*
    Open a new Scope Level
     */
    public void openScope(){
        this.sizes.add(getPrevSizes( this.sizes));
        this.globalSizes.add(getPrevSizes( this.globalSizes));
        this.scopes.add(new HashMap<>());
        this.scope_num++;
    }
    /*
    Closes the scope level in the program
     */
    public void closeScope(){
        this.scopes.remove(scope_num);
        this.sizes.remove(scope_num);
        this.globalSizes.remove(scope_num);
        this.scope_num--;
    }
    /*
    Adds a variable to the current scope in the program and adds an error if a variable with the
    same name already exists in this scope
     */
    public VariableData declare(String var, MyType type, Token tk,boolean shared){
        if (shared){
            this.globalSizes.set(this.scope_num,this.globalSizes.get(this.scope_num)+1);

            this.scopes.get(this.scope_num).put(var, new VariableData(type,this.globalSizes.get(this.scope_num),shared));
            return this.scopes.get(this.scope_num).get(var);


        }
        if (!this.scopes.get(this.scope_num).containsKey(var)) {
            this.sizes.set(this.scope_num,this.sizes.get(this.scope_num)+1);
            this.scopes.get(this.scope_num).put(var, new VariableData(type,this.sizes.get(this.scope_num)));
            return this.scopes.get(this.scope_num).get(var);
        }
        else{
            this.errors.add(var+" already declared in this scope: "+tk.getLine());
        }
        return null;
    }

    public VariableData check(String var,Token tk){
        VariableData check = checkLocal(var,tk);
        if (check!=null){
            return check;
        }
        check = checkGlobal(var,tk);
        if(check==null){
            errors.add(var+" not declared in this scope: "+tk.getLine());
            return null;
        }
        return check;
    }
    /*
    Checks the current scope if the variable exists and returns the type of the variable if it exists,
    and returns null if the variable does not exist in the current scope
     */
    public VariableData checkLocal(String var,Token tk){
        if(this.scopes.get(this.scope_num).containsKey(var)){
            return this.scopes.get(this.scope_num).get(var);
        }
        return null;
    }
    /*
    Checks all scopes starting from the deepest level if the variable exists and returns the type of the variable if it exists,
    and returns null if the variable does not exist globally
     */

    public VariableData checkGlobal(String var,Token tk){
        for (int i = this.scopes.size()-1; i >= 0; i--){
            if (this.scopes.get(i).containsKey(var)){
                return this.scopes.get(i).get(var);
            }

        }
        return null;
    }
    /*
    Returns the current scope number in the program
     */
    public int getCurrentScope(){
        return this.scope_num;
    }
    /*
    Print the Scope Table
     */
    public void print(){
        for(int i=0;i<this.scopes.size();i++){
            System.out.println("Scope Level: "+i);
            Iterator it = this.scopes.get(i).entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                System.out.println(pair.getKey() + " = " + pair.getValue());
            }
        }
    }
    public void addOffset(int offset){
        this.sizes.add(getPrevSizes(this.sizes) + offset);
    }

}
