package ut.pp.compiler.model;

import ut.pp.compiler.model.enums.Registers;
import ut.pp.compiler.model.enums.Targets;
import ut.pp.compiler.model.interfaces.InstructionArgs;

public class Target implements InstructionArgs{

    private Targets target;
    private int val = 0;
    private Registers reg = Registers.reg0;

    /**
     * @param target
     * @param val
     * To initialize the target with a Target and a value
     */
    public Target(Targets target, int val){
        this.target = target;
        this.val = val;
    }
    /**
     * @param target
     * @param reg
     * To initialize the target with a Target and a register
     */
    public Target(Targets target, Registers reg) {
        this.target = target;
        this.reg = reg;
    }

    /**
     * @return target instruction as string
     * To print the target to a haskell file
     */
    @Override
    public String toString () {
        String temp = "";
        if (reg.equals(Registers.reg0)) {
            temp = " (" + this.val + "))";
        } else {
            temp = " (" + this.reg + "))";
        }
        return "(" + this.target + temp;
    }

    /**
     * @return this object
     */
    @Override
    public InstructionArgs get() {
        return this;
    }
}
