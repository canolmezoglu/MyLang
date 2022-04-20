package ut.pp.compiler.model.enums;

import ut.pp.compiler.model.interfaces.InstructionArgs;

public enum Operators implements InstructionArgs {
    /***
     * To represent the Operators
     */
    Add,Sub,Mul,Equal,NEq,Gt,Lt,GtE,LtE,And,Or,Incr,Decr,Xor;

    /***
     * @return this object
     */
    @Override
    public InstructionArgs get() {
        return this;
    }
}
