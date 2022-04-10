package ut.pp.elaboration.model.enums;

import ut.pp.elaboration.model.interfaces.InstructionArgs;

public class FakeOperator implements InstructionArgs {
    String functionName;
    public FakeOperator(String functionName){
        this.functionName = functionName;
    }

    public String getFunctionName() {
        return functionName;
    }

    @Override
    public InstructionArgs get() {
        return this;
    }
}
