package ut.pp.compiler.model;

import ut.pp.compiler.model.enums.Instructions;
import ut.pp.compiler.model.interfaces.InstructionArgs;

import java.util.List;

public class Instruction {
    private final Instructions instr;
    List<InstructionArgs> args;

    /**
     * @param instr an instance of the Instructions class
     * @param args arguments of this instruction
     * Constructor for Instructions which can have an instruction and a list of arguments
     */
    public Instruction(Instructions instr,List<InstructionArgs> args){
        this.instr=instr;
        this.args= args;
    }

    /**
     * @param instr an instance of the Instructions class
     * Constructor for Instruction with no args
     */
    public Instruction(Instructions instr){
        this.instr=instr;
    }

    /***
     * Get this instruction
     * @return this.instr
     */
    public Instructions getInstr() {
        return this.instr;
    }

    /***
     * Get the arguments of this instruction
     * @return this.args
     */
    public  List<InstructionArgs> getArgs() {
        return this.args;
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
                arg.append(" ");
                arg.append(i.toString());
            }
            return this.instr + " " + arg;
           // return this.instr + " " + arg+"\n";

        }
        //return this.instr+"\n";
        return this.instr+"";

    }

}
