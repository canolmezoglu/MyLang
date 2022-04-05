package ut.pp.elaboration.model;

import ut.pp.elaboration.model.enums.Registers;
import ut.pp.elaboration.model.interfaces.InstructionArgs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Register {
    private List<Registers> registers;
    private Registers reg0;

    public Register(){
        this.registers = new ArrayList<>();
        this.registers.addAll(Arrays.asList(Registers.values()));
        this.registers.remove(6);
        this.registers.remove(6);
        this.reg0= Registers.reg0;
    }

    /**
     * @return a register
     * @throws IndexOutOfBoundsException
     * Acquire an empty register from the registers list
     */
    public Registers acquire() throws IndexOutOfBoundsException{
        return this.registers.remove(0);
    }
    /**
     * @param reg
     * Release a register by adding it back to the list of registers
     */
    public void release(Registers reg){
        this.registers.add(reg);
    }

    /**
     * @return
     * Get all the registers
     */
    public List<Registers> getAll() {
        return this.registers;
    }

}
