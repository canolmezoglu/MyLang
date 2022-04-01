package ut.pp.elaboration.model;

import ut.pp.elaboration.MyType;
import ut.pp.elaboration.ScopeTable;
import ut.pp.elaboration.model.Instruction;
import ut.pp.elaboration.model.enums.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Sprockell {

    private List<Instruction> instructions;
    private  List<List<String>> memory; //list of variables in memory for each scope level

    public Sprockell(){
        instructions=new ArrayList<>();
        memory = new ArrayList<>();
    }

    public List<List<String>> getMemory(){return this.memory;}

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
    public void addToMemory (String val,int scope, boolean shared){
        //TODO shared memory
        while (scope >= memory.size()) {
            memory.add(new ArrayList<>());
        }
        memory.get(scope).add(val);
    }

    //sprockell operations

    public void branch (Registers reg, Target target){
        addInstruction(new Instruction(Instructions.Branch, Arrays.asList(reg, target)));
    }
    public void branch (Registers reg,int index, Target target){
        addInstruction(index, new Instruction(Instructions.Branch, Arrays.asList(reg, target)));
    }
    public void compute (Operators op, Registers reg1, Registers reg2, Registers reg3){
        addInstruction(new Instruction(Instructions.Compute, Arrays.asList(op, reg1, reg2, reg3)));
    }
    public void endProgram () {
        addInstruction(new Instruction(Instructions.EndProg));
    }
    public void writeToIO (Registers reg){
        addInstruction(new Instruction(Instructions.WriteInstr, Arrays.asList(reg, new MemoryAddr(MemoryAddrs.numberIO))));
    }
    public Instruction relJump ( int line){
        return new Instruction(Instructions.Jump, Collections.singletonList(new Target(Targets.Rel, line)));
    }
    public Registers loadToRegister (String val,int scope, Registers register){
        //TODO add instructions for shared memory - readinstr and recieve
        try {
                int num = 0;
                if (val.equals("true")) {
                    num = 1;
                } else if (val.equals("false")) {
                    num = 0;
                } else {
                    num = Integer.parseInt(val);
                }
                addInstruction(new Instruction(Instructions.Load, Arrays.asList(new MemoryAddr(MemoryAddrs.ImmValue, num), register)));
            } catch (NumberFormatException e) {
                int slot = getAddressFromMemory(val, scope, memory);
                addInstruction(new Instruction(Instructions.Load, Arrays.asList(new MemoryAddr(MemoryAddrs.DirAddr, slot), register)));

            }
        return register;
    }

    public Registers storeInMemory (String name, Registers reg,int scope){
        int slot = getAddressFromMemory(name, scope, memory);
        addInstruction(new Instruction(Instructions.Store, Arrays.asList(reg, new MemoryAddr(MemoryAddrs.DirAddr, slot))));
        return reg;
    }

}
