package ut.pp.elaboration.model.enums;

import ut.pp.elaboration.model.interfaces.InstructionArgs;

public enum Registers implements InstructionArgs {
    regA,regB,regC,regD,regE,regF,reg0,regSprID;

    @Override
    public InstructionArgs get() {
        return this;
    }
}
