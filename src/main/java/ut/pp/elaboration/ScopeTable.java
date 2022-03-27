package ut.pp.elaboration;

import org.antlr.v4.runtime.Token;

import java.util.*;

public class ScopeTable {
    List<String> errors;
    List<HashMap<String,MyType>> scopes;
    int scope_num;

    public ScopeTable(){
        this.errors = new ArrayList<>();
        this.scopes = new ArrayList<>();
        this.scope_num=0;
        this.scopes.add(new HashMap<>());
    }
    /*
    Open a new Scope Level
     */
    public void openScope(){
        this.scopes.add(new HashMap<>());
        this.scope_num++;
    }
    /*
    Closes the scope level in the program
     */
    public void closeScope(){
        this.scopes.remove(scope_num);
        this.scope_num--;
    }
    /*
    Adds a variable to the current scope in the program and adds an error if a variable with the
    same name already exists in this scope
     */
    public void declare(String var, MyType type, Token tk){
        if (!this.scopes.get(this.scope_num).containsKey(var)) {
            this.scopes.get(this.scope_num).put(var, type);
        }
        else{
            this.errors.add(var+" already declared in this scope: "+tk.getLine());
        }
    }
    /*
    Checks the current scope if the variable exists and returns the type of the variable if it exists,
    and returns null if the variable does not exist in the current scope
     */
    public MyType check(String var,Token tk){
        if(this.scopes.get(this.scope_num).containsKey(var)){
            return this.scopes.get(this.scope_num).get(var);
        }
        errors.add(var+" not declared in this scope: "+tk.getLine());
        return null;
    }
    /*
    Checks all scopes starting from the deepest level if the variable exists and returns the type of the variable if it exists,
    and returns null if the variable does not exist globally
     */
    public MyType checkGlobal(String var,Token tk){
        ListIterator<HashMap<String,MyType>> iterator= this.scopes.listIterator(this.scopes.size());
        while(iterator.hasPrevious()){
            if(iterator.previous().containsKey(var)){
                return iterator.previous().get(var);
            }
        }
        this.errors.add(var+" has not been declared: "+tk.getLine());
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
                it.remove();
            }
        }
    }

}
