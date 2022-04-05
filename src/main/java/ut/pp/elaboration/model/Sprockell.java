package ut.pp.elaboration.model;

import ut.pp.elaboration.MyType;
import ut.pp.elaboration.Result;
import ut.pp.elaboration.ScopeTable;
import ut.pp.elaboration.model.Instruction;
import ut.pp.elaboration.model.enums.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Sprockell {

    private int pointer;
    private List<Instruction> instructions;
    private  List<List<String>> memory; //list of variables in memory for each scope level

    public Sprockell(){
        pointer=0;
        instructions=new ArrayList<>();
        memory = new ArrayList<>();
    }

    public void setPointer(){
        this.pointer = this.instructions.size();
    }
    public int getPointer() {
        return this.pointer;
    }

    public List<List<String>> getMemory(){return this.memory;}

    public List<Instruction> getInstructions(){
        return this.instructions;
    }

    public void addInstruction (Instruction instr){
        instructions.add(instr);
    }

    public void addInstruction(int index, Instruction instr){
        instructions.add(index,instr);
    }
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
    //TODO when you exit a scope
    public void addToMemory (String val,int scope){
        //TODO shared memory
        while (scope >= memory.size()) {
            memory.add(new ArrayList<>());
        }
        memory.get(scope).add(val);
    }


    //sprockell operations

    public Instruction branch (Registers reg, Target target) {
        return new Instruction(Instructions.Branch, Arrays.asList(reg, target));
    }
//    public void branch (Registers reg,int index, Target target){
//        addInstruction(index, new Instruction(Instructions.Branch, Arrays.asList(reg, target)));
//    }
    public Instruction testAndSet (int addr){
        return new Instruction(Instructions.TestAndSet, Arrays.asList(new MemoryAddr(MemoryAddrs.DirAddr,addr)));
    }
    public Instruction testAndSet (Registers addr){
        return new Instruction(Instructions.TestAndSet, Arrays.asList(new MemoryAddr(MemoryAddrs.IndAddr,addr)));
    }
    public Instruction compute (Operators op, Registers reg1, Registers reg2, Registers reg3){
        return new Instruction(Instructions.Compute, Arrays.asList(op, reg1, reg2, reg3));
    }
    public Instruction receive(Registers reg){
        return new Instruction(Instructions.Receive,Arrays.asList(reg));
    }
    public Instruction endProgram () {
        return new Instruction(Instructions.EndProg);
    }
    public Instruction writeToIO (Registers reg){
        return new Instruction(Instructions.WriteInstr, Arrays.asList(reg, new MemoryAddr(MemoryAddrs.numberIO)));
    }
    public Instruction writeToMemory (Registers reg,int addr){
        return new Instruction(Instructions.WriteInstr, Arrays.asList(reg, new MemoryAddr(MemoryAddrs.DirAddr,addr)));
    }
    public Instruction writeToMemory (Registers reg,Registers addr){
        return new Instruction(Instructions.WriteInstr, Arrays.asList(reg, new MemoryAddr(MemoryAddrs.IndAddr,addr)));
    }
    public Instruction relJump ( int line){
        return new Instruction(Instructions.Jump, Collections.singletonList(new Target(Targets.Rel, line)));
    }
    public Instruction loadToRegister (String val, int scope, Registers register, int res){
        //TODO add instructions for shared memory - readinstr and recieve
        Instruction inst;
        try {
                int num = 0;
                if (val.equals("true")) {
                    num = 1;
                } else if (val.equals("false")) {
                    num = 0;
                } else {
                    num = Integer.parseInt(val);
                }
                 inst = new Instruction(Instructions.Load, Arrays.asList(new MemoryAddr(MemoryAddrs.ImmValue, num), register));
            } catch (NumberFormatException e) {
                inst = new Instruction(Instructions.Load, Arrays.asList(new MemoryAddr(MemoryAddrs.DirAddr, res), register));

        }
        return inst;
    }

    public Instruction storeInMemory (String name, Registers reg,int slot){
        return new Instruction(Instructions.Store, Arrays.asList(reg, new MemoryAddr(MemoryAddrs.DirAddr, slot)));
    }

}
