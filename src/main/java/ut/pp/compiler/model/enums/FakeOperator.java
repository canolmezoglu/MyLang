package ut.pp.compiler.model.enums;

import ut.pp.compiler.model.interfaces.InstructionArgs;

public class FakeOperator implements InstructionArgs {
    /***
     * To represent a function definition and usage in Sprockell
     */
    String functionName;

    /***
     * Constructor to initialize this class with the name of the function
     * @param functionName name of the function that is being defined
     */
    public FakeOperator(String functionName){
        this.functionName = functionName;
    }

    /***
     * Get the name of the function
     * @return functionName
     */
    public String getFunctionName() {
        return functionName;
    }

    /***
     * @return this object
     */
    @Override
    public InstructionArgs get() {
        return this;
    }
}
