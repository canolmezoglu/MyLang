package ut.pp.elaboration.model.enums;

import ut.pp.elaboration.model.interfaces.InstructionArgs;

public enum Registers implements InstructionArgs {
    /***
     * To represent the different registers that are used in Sprockell
     */
    regA,regB,regC,regD,regE,regF,reg0,regSprID,regPC;

    /***
     * @return this object
     */
    @Override
    public InstructionArgs get() {
        return this;
    }
}
