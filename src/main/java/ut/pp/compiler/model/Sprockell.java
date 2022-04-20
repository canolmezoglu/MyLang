package ut.pp.compiler.model;

import ut.pp.compiler.model.enums.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Sprockell {
    /***
     * Class to generate Sprockell instructions
     */
    private int pointer;
    private List<Instruction> instructions;
    private  List<List<String>> memory; //list of variables in memory for each scope level

    /***
     * Constructor to set the code pointer to 0 initially
     * and initialize the program memory and instructions
     */
    public Sprockell(){
        pointer=0;
        instructions=new ArrayList<>();
        memory = new ArrayList<>();
    }

    /**
     * Set the pointer to the number of lines in the code
     */
    public void setPointer(){
        this.pointer = this.instructions.size();
    }

    /**
     * Get the number current pointer value
     * @return this.pointer
     */
    public int getPointer() {
        return this.pointer;
    }

    /**
     * Get the program memory list
     * @return this.memory
     */
    public List<List<String>> getMemory(){return this.memory;}

    /**
     * Get the list of instructions in the program
     * @return this.instructions
     */
    public List<Instruction> getInstructions(){
        return this.instructions;
    }

    /**
     * Add an instruction to the program
     * @param instr sprockell instruction
     */
    public void addInstruction (Instruction instr){
        instructions.add(instr);
    }

    /**
     * Add an instruction to a certain line in the program
     * @param index line number at which instruction should be added
     * @param instr sprockell instruction
     */
    public void addInstruction(int index, Instruction instr){
        instructions.add(index,instr);
    }

    /**
     * Get the address of a variable from the program memory
     * @param val value whose address is needed
     * @param scope current scope number in the program
     * @param list scope list
     * @return
     */
    public int getAddressFromMemory (String val,int scope, List<List<String>>list){
        //TODO add check for shared memory
        int address = 0;
        for (int i = scope; i >= 0; i--) {
            List<String> scopeList = list.get(i);
            if (scopeList.contains(val)) {
                address += scopeList.indexOf(val);
                for (int j = 0; j < i; j++) {
                    address += list.get(j).size();
                }
                break;
            }
        }
        return address;
    }

    /**
     * Add a value to memory
     * @param val value to be added
     * @param scope current scope number
     */
    //TODO when you exit a scope
    public void addToMemory (String val,int scope){
        //TODO shared memory
        while (scope >= memory.size()) {
            memory.add(new ArrayList<>());
        }
        memory.get(scope).add(val);
    }


    //sprockell operations

    /**
     * Sprockell branch instruction
     * @param reg Register
     * @param target TargetType
     * @return
     */
    public Instruction branch (Registers reg, Target target) {
        return new Instruction(Instructions.Branch, Arrays.asList(reg, target));
    }
//    public void branch (Registers reg,int index, Target target){
//        addInstruction(index, new Instruction(Instructions.Branch, Arrays.asList(reg, target)));
//    }

    /**
     * TestAndSet sprockell instruction
     * @param addr Value of direct memory address
     * @return sprockell testandset instruction
     */
    public Instruction testAndSet (int addr){
        return new Instruction(Instructions.TestAndSet, Arrays.asList(new MemoryAddr(MemoryAddrs.DirAddr,addr)));
    }

    /**
     * TestAndSet sprockell instruction
     * @param addr Registers object
     * @return sprockell testandset instruction
     */
    public Instruction testAndSet (Registers addr){
        return new Instruction(Instructions.TestAndSet, Arrays.asList(new MemoryAddr(MemoryAddrs.IndAddr,addr)));
    }

    /**
     * Sprockell Compute Instruction
     * @param op Operator type
     * @param reg1 Register 1
     * @param reg2 Register 2
     * @param reg3 Register 3 where result is stores
     * @return
     */
    public Instruction compute (Operators op, Registers reg1, Registers reg2, Registers reg3){
        return new Instruction(Instructions.Compute, Arrays.asList(op, reg1, reg2, reg3));
    }

    /**
     * Sprockell Receive instruction to get the value stored in a register
     * @param reg Register object
     * @return sprockell instruction
     */
    public Instruction receive(Registers reg){
        return new Instruction(Instructions.Receive,Arrays.asList(reg));
    }

    /**
     * Sprockell End program instruction
     * @return sprockell instruction
     */
    public Instruction endProgram () {
        return new Instruction(Instructions.EndProg);
    }

    /**
     * Sprockell print instruction
     * @param reg The register whose value needs to be printed
     * @return
     */
    public Instruction writeToIO (Registers reg){
        return new Instruction(Instructions.WriteInstr, Arrays.asList(reg, new MemoryAddr(MemoryAddrs.numberIO)));
    }

    /**
     * Write the register value to a direct memory address
     * @param reg Register object
     * @param addr Memory address value
     * @return sprockell instruction
     */
    public Instruction writeToMemory (Registers reg,int addr){
        return new Instruction(Instructions.WriteInstr, Arrays.asList(reg, new MemoryAddr(MemoryAddrs.DirAddr,addr)));
    }

    /**
     * Write the value of a register to an indirect memory address
     * @param reg Register 1
     * @param addr Register 2
     * @return sprockell instruction
     */
    public Instruction writeToMemory (Registers reg,Registers addr){
        return new Instruction(Instructions.WriteInstr, Arrays.asList(reg, new MemoryAddr(MemoryAddrs.IndAddr,addr)));
    }

    /**
     * Relative jump instruction
     * @param line line number to jump to
     * @return
     */
    public Instruction relJump ( int line){
        return new Instruction(Instructions.Jump, Collections.singletonList(new Target(Targets.Rel, line)));
    }

    /**
     * Absolute jump sprockell instruction
     * @param line line number to jump to
     * @return sprockell instruction
     */
    public Instruction absJump ( int line){
        return new Instruction(Instructions.Jump, Collections.singletonList(new Target(Targets.Abs, line)));
    }

    /**
     * Sprockell indirect jump instruction
     * @param reg Register object
     * @return sprockell instruction
     */
    public Instruction IndJump ( Registers reg){
        return new Instruction(Instructions.Jump, Collections.singletonList(new Target(Targets.Ind, reg)));
    }

    /**
     * Sprockell instruction to Load a value to the memory
     * @param val value to be loaded
     * @param register Register object
     * @return sprockell instruction
     */
    public Instruction loadToMemory (String val, Registers register){
        Instruction inst;
        double num = 0;
        if (val.equals("true")) {
            num = 1;
        } else if (val.equals("false")) {
            num = 0;
        } else {
            num = Double.parseDouble(val);
        }
        inst = new Instruction(Instructions.Load, Arrays.asList(new MemoryAddr(MemoryAddrs.ImmValue, num), register));
        return inst;
    }

    /**
     * Sprockell instruction to load a value to the sprockell memory
     * @param reg1 Register object
     * @param slot value of slot to store the value
     * @return sprockell instruction
     */
    public Instruction loadToMemory(Registers reg1, int slot){
        return new Instruction(Instructions.Load, Arrays.asList(new MemoryAddr(MemoryAddrs.DirAddr, slot), reg1));
    }

    /**
     * Get value from IndAddr
     * @param reg1 Regsiter to load from
     * @param reg2 Register to load into
     * @return
     */
    public Instruction getFromIndAddr(Registers reg1,Registers reg2){
        return new Instruction(Instructions.Load, Arrays.asList(new MemoryAddr(MemoryAddrs.IndAddr, reg2), reg1));
    }

    /**
     * Store register value in DirAddr
     * @param name name of varibale
     * @param reg register object
     * @param slot slot number to store value at
     * @return sprockell instruction
     */
    public Instruction storeInMemory (String name, Registers reg,int slot){
        return new Instruction(Instructions.Store, Arrays.asList(reg, new MemoryAddr(MemoryAddrs.DirAddr, slot)));
    }

    /**
     * Store value of a register in DirAddr
     * @param reg register object
     * @param slot slot number to store value at
     * @return sprockell instruction
     */
    public Instruction storeInMemory (Registers reg,int slot){
        return new Instruction(Instructions.Store, Arrays.asList(reg, new MemoryAddr(MemoryAddrs.DirAddr, slot)));
    }

    /**
     * Store register value in IndAddr
     * @param reg register object
     * @param slot slot number to store the value at
     * @return
     */
    public Instruction storeInMemory (Registers reg,Registers slot){
        return new Instruction(Instructions.Store, Arrays.asList(reg, new MemoryAddr(MemoryAddrs.IndAddr, slot)));
    }

    /**
     * Read instruction from DirAddr at a certain slot
     * @param slot slot number of DirAddr
     * @return
     */
    public Instruction readInst (int slot){
        return new Instruction(Instructions.ReadInstr, Arrays.asList(new MemoryAddr(MemoryAddrs.DirAddr, slot)));
    }

    /**
     * Read Instruction from IndAddr of certain register
     * @param slot register object
     * @return sprockell instruction
     */
    public Instruction readInst (Registers slot){
        return new Instruction(Instructions.ReadInstr, Arrays.asList(new MemoryAddr(MemoryAddrs.IndAddr, slot)));
    }

    /**
     * TODO
     * @param functionName
     * @return
     */
    public Instruction fakeInst (String functionName){
        return new Instruction(Instructions.Fake,Arrays.asList(new FakeOperator(functionName)));
    }

    /**
     * put the top of the stack in a register
     * @param registers Registers object
     * @return sprockell instruction
     */
    public Instruction pop (Registers registers){
        return new Instruction(Instructions.Pop,Arrays.asList(registers));
    }

    /**
     * put the value from register on the stack
     * @param registers Registers object
     * @return sprockell instruction
     */
    public Instruction push (Registers registers){
        return new Instruction(Instructions.Push,Arrays.asList(registers));
    }



}
