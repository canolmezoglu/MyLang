package ut.pp.compiler.model;

import ut.pp.compiler.model.enums.Registers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Register {
    /**
     * Class to handle the registers being used in the program
     */
    private List<Registers> registers;
    private Registers reg0;

    public Register(){
        /**
         * Constructor to initialize the registers for the Sprockell program
         */
        this.registers = new ArrayList<>();
        this.registers.addAll(Arrays.asList(Registers.values()));
        this.registers.remove(8);
        this.registers.remove(6);
        this.registers.remove(6);
        // remove regarp
        this.registers.remove(5);
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
     * @param reg the Gegisters object
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
