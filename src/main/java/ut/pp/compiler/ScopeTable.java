package ut.pp.compiler;

import org.antlr.v4.runtime.Token;
import ut.pp.compiler.model.enums.MyType;
import ut.pp.compiler.model.VariableData;

import java.util.*;

public class ScopeTable {
    /**
     * Class to handle Scope Checking and keeping track identifiers in the program
     */
    Set<String> errors;
    List<HashMap<String, VariableData>> scopes;
    List<Integer> sizes;
    List<Integer> globalSizes;
    int scope_num;

    /**
     * Constructor for the ScopeTable
     */
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

    /**
     * Constructor for scope table + check for shared memory locations
     * Global sizes are initialized with the number of threads
     * @param globalSizes number of shared variables
     */
    public ScopeTable(int globalSizes){
        this.errors = new HashSet<>();
        this.scopes = new ArrayList<>();
        this.sizes = new ArrayList<>();
        this.globalSizes = new ArrayList<>();
        this.scope_num=0;
        this.scopes.add(new HashMap<>());
        this.sizes.add(0);
        if (globalSizes > 7){
            this.errors.add(" Error: More than 7 threads are defined, more shared memory locations are used than capacity");
        }
        this.globalSizes.add(globalSizes);

    }

    /**
     * Get total size of a list of integers
     * @param x
     * @return
     */
    public int getPrevSizes(List<Integer> x){
        int total = 0;
        for (int i : x){
            total +=i;
        }
        return total;
    }

    /**
     * Open a new Scope Level
     */
    public void openScope(){
        this.sizes.add(getPrevSizes( this.sizes));
        this.globalSizes.add(getPrevSizes( this.globalSizes));
        this.scopes.add(new HashMap<>());
        this.scope_num++;
    }

    /**
     * Closes a scope level in the program
     */
    public void closeScope(){
        this.scopes.remove(scope_num);
        this.sizes.remove(scope_num);
        this.globalSizes.remove(scope_num);
        this.scope_num--;
    }

    /**
     * Adds a variable to the current scope in the program and adds an error if a variable with the
     *     same name already exists in this scope
     * @param var name of variable
     * @param type type of variable
     * @param tk Token object
     * @param shared is the variable shared or not
     * @return VariableData object
     */
    public VariableData declare(String var, MyType type, Token tk, boolean shared){
        if (shared && !this.scopes.get(this.scope_num).containsKey(var)){
            if (type!= MyType.ARRAY) {
                this.globalSizes.set(this.scope_num, this.globalSizes.get(this.scope_num) + 1);
            }

            this.scopes.get(this.scope_num).put(var, new VariableData(type,this.globalSizes.get(this.scope_num),shared));
            if (globalSizes.get(this.scope_num) > 7){
                this.errors.add("Error: More than 7 shared memory locations are used at line: "+tk.getLine());
            }
            return this.scopes.get(this.scope_num).get(var);


        }
        else if (!this.scopes.get(this.scope_num).containsKey(var)) {
            if (type!=MyType.ARRAY) {
                this.sizes.set(this.scope_num, this.sizes.get(this.scope_num) + 1);
            }
            this.scopes.get(this.scope_num).put(var, new VariableData(type,this.sizes.get(this.scope_num)));
            return this.scopes.get(this.scope_num).get(var);
        }
        else{
            if(var.contains("%")){
                this.errors.add("Error:"+var+"array or array index already declared in this scope at Line "+tk.getLine()+" Character:" + tk.getCharPositionInLine());
            }
            else if(var.contains(".")){
                this.errors.add("Error:"+var+" enum or enum type already declared in this scope at Line "+tk.getLine()+" Character:" + tk.getCharPositionInLine());
            }
            else{
                this.errors.add("Error:"+var+" already declared in this scope at Line "+tk.getLine()+" Character:" + tk.getCharPositionInLine());
            }
            return null;
        }
    }

    /**
     * Check whether the variable has been defined for this scope
     * @param var name of variable
     * @param tk Token object
     * @return
     */
    public VariableData check(String var,Token tk){
        VariableData check = checkLocal(var,tk);
        if (check!=null){
            return check;
        }
        check = checkGlobal(var,tk);
        if(check==null){
            if(var.contains("%")){
                errors.add("Error:"+ var+" array or array index not declared in this scope at Line: "+tk.getLine()+ " Character: "+tk.getCharPositionInLine());}
            else if(var.contains(".")){
                errors.add("Error: "+var+" enum or enum type not declared in this scope at Line: "+tk.getLine()+ " Character: "+tk.getCharPositionInLine());}
            else{
                errors.add("Error:"+var+" not declared in this scope at Line: "+tk.getLine()+ " Character: "+tk.getCharPositionInLine());
            }
            return null;
        }
        return check;
    }


    /**
     * Checks the current scope if the variable exists and returns the type of the variable if it exists,
     *     and returns null if the variable does not exist in the current scope
     * @param var name of variable
     * @param tk Token object
     * @return VariableData object
     */
    public VariableData checkLocal(String var,Token tk){
        if(this.scopes.get(this.scope_num).containsKey(var)){
            return this.scopes.get(this.scope_num).get(var);
        }
        return null;
    }


    /**
     * Checks all scopes starting from the deepest level if the variable exists and returns the type of the variable if it exists,
     *     and returns null if the variable does not exist globally
     * @param var name of variable
     * @param tk Token object
     * @return VariableData object
     */
    public VariableData checkGlobal(String var,Token tk){
        for (int i = this.scopes.size()-1; i >= 0; i--){
            if (this.scopes.get(i).containsKey(var)){
                return this.scopes.get(i).get(var);
            }

        }
        return null;
    }

    /**
     * Return the current scope number in the program
     * @return scope number value
     */
    public int getCurrentScope(){
        return this.scope_num;
    }

    /**
     * Print the Scope Table
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