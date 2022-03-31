package ut.pp.elaboration.model.enums;

import ut.pp.elaboration.model.interfaces.InstructionArgs;

public enum Operators implements InstructionArgs {
    Add,Sub,Mul,Equal,NEq,Gt,Lt,GtE,LtE,And,Or,Incr,Decr;

    @Override
    public InstructionArgs get() {
        return this;
    }
}
