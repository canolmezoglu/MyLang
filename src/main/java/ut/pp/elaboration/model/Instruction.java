package ut.pp.elaboration.model;

import ut.pp.elaboration.model.enums.Instructions;
import ut.pp.elaboration.model.interfaces.InstructionArgs;

import java.util.List;

public class Instruction {
    private final Instructions instr;
    List<InstructionArgs> args;

    /**
     * @param instr
     * @param args
     * Constructor for Instructions which can have an instruction and a list of arguments
     */
    public Instruction(Instructions instr,List<InstructionArgs> args){
        this.instr=instr;
        this.args= args;
    }

    /**
     * @param instr
     * Constructor for Instruction with no args
     */
    public Instruction(Instructions instr){
        this.instr=instr;
    }

    public Instructions getInstr() {
        return this.instr;
    }

    /**
     * @return instructions as a string
     * To print the instruction to the haskell file
     */
    @Override
    public String toString() {
        StringBuilder arg = new StringBuilder();
        if(args!=null) {
            for (var i : args) {
                arg.append(i.toString()).append(" ");
            }
            return this.instr + " " + arg+"\n";
        }
        return this.instr+"\n";
    }

}
