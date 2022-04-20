package ut.pp.compiler.model;

import ut.pp.compiler.model.enums.MemoryAddrs;
import ut.pp.compiler.model.enums.Registers;
import ut.pp.compiler.model.interfaces.InstructionArgs;

public class MemoryAddr implements InstructionArgs {
    private final MemoryAddrs type;
    private String val= "0";
    private Registers reg= Registers.reg0;

    /**
     * @param type The type of MemoryAddress
     * Constructor enumsto initialize a memory address
     */
    public MemoryAddr(MemoryAddrs type) {
        this.type = type;
    }
    /**
     * @param type The type of MemoryAddress
     * @param val Value of this memory address
     * Constructor to initialize a memory address with a memory address type and value
     */
    public MemoryAddr(MemoryAddrs type,double val) {
        this.type = type;
        this.val=String.format("%.0f", val);
    }
    /**
     * @param type The type of MemoryAddress
     * @param reg The register object
     * Constructor to initialize a memory address with a memory address type and register
     */
    public MemoryAddr(MemoryAddrs type, Registers reg){
        this.type= type;
        this.reg=reg;
    }
    /**
     * @return type of memory address
     */
    public MemoryAddrs getType() {
        return this.type;
    }
    /**
     * @return memory address as a string
     * To print the memory address to a haskell file
     */
    @Override
    public String toString() {
        if(type.equals(MemoryAddrs.numberIO)){
            return type+"";
        }else {
            if (!reg.equals(Registers.reg0)) {
                return "(" + type +
                        " " + reg +
                        ')';
            } else {
                return "(" + type +
                        " " + val +
                        ')';
            }
        }
    }

    /**
     * @return this object
     */
    @Override
    public InstructionArgs get() {
        return this;
    }
}
