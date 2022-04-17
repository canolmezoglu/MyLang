package ut.pp.elaboration.model;

import ut.pp.elaboration.model.enums.Operators;
import ut.pp.elaboration.model.enums.Registers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ArraySp {
    class ArrayPointer{
        boolean dynamic;
        int offset;

        public ArrayPointer(boolean dynamic, int offset) {
            this.dynamic = dynamic;
            this.offset = offset;
        }
    }
    private int columnSize;
    private int beginning_offset;
    private ArrayPointer[] pointers;

    public int getColumnSize() {
        return columnSize;
    }

    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;
    }

    public ArraySp(int beginning_offset) {
        this.beginning_offset = beginning_offset;
    }

    public int getBeginning_offset() {
        return beginning_offset;
    }
    public void addPointer(boolean dynamic1, int offset1){
        pointers = new ArrayPointer[1];
        pointers[0] = new ArrayPointer(dynamic1,offset1);
    }

    public void addPointer(boolean dynamic1, int offset1,boolean dynamic2, int offset2){
        pointers = new ArrayPointer[2];

        pointers[0] = new ArrayPointer(dynamic1,offset1);
        pointers[1] = new ArrayPointer(dynamic2,offset2);

    }
    public boolean getFirstPointerDynamic(){
        return this.pointers[0].dynamic;
    }
    public int getFirstPointerOffset(){
        return this.pointers[0].offset;
    }
    public boolean getSecondPointerDynamic(){
        return this.pointers[1].dynamic;
    }
    public int getSecondPointerOffset(){
        return this.pointers[1].offset;
    }
    public int getPointerSize(){
        return this.pointers.length;
    }

    public List<Instruction> getArrPointer(){
        List<Instruction> InstructionList = new ArrayList<>();
        Sprockell sp = new Sprockell();
        if ( this.getPointerSize() > 1){
            if (this.getFirstPointerDynamic() && this.getSecondPointerDynamic()){
                InstructionList.add(sp.loadToMemory(Integer.toString(this.getFirstPointerOffset()), Registers.regB));
                InstructionList.add(sp.getFromIndAddr(Registers.regB, Registers.regB));
                InstructionList.add(sp.loadToMemory(Integer.toString(this.columnSize),Registers.regC));
                InstructionList.add(sp.compute(Operators.Mul,Registers.regB,Registers.regC,Registers.regB));
                InstructionList.add(sp.loadToMemory(Integer.toString(this.getBeginning_offset()), Registers.regC));
                InstructionList.add(sp.compute(Operators.Add, Registers.regB, Registers.regC, Registers.regB));
                InstructionList.add(sp.loadToMemory(Integer.toString(this.getSecondPointerOffset()), Registers.regC));
                InstructionList.add(sp.getFromIndAddr(Registers.regC, Registers.regC));
                InstructionList.add(sp.compute(Operators.Add, Registers.regB, Registers.regC, Registers.regB));
            }
            else if(this.getFirstPointerDynamic()){
                InstructionList.add(sp.loadToMemory(Integer.toString(this.getFirstPointerOffset()), Registers.regB));
                InstructionList.add(sp.getFromIndAddr(Registers.regB, Registers.regB));
                InstructionList.add(sp.loadToMemory(Integer.toString(this.columnSize),Registers.regC));
                InstructionList.add(sp.compute(Operators.Mul,Registers.regB,Registers.regC,Registers.regB));
                InstructionList.add(sp.loadToMemory(Integer.toString(this.getBeginning_offset()), Registers.regC));
                InstructionList.add(sp.compute(Operators.Add, Registers.regB, Registers.regC, Registers.regB));
                InstructionList.add(sp.loadToMemory(Integer.toString(this.getSecondPointerOffset()),Registers.regC));
                InstructionList.add(sp.compute(Operators.Add, Registers.regB, Registers.regC, Registers.regB));
            }
            else if(this.getSecondPointerDynamic()){
                InstructionList.add(sp.loadToMemory(Integer.toString(this.getFirstPointerOffset()*this.columnSize+this.getBeginning_offset()),Registers.regB));
                InstructionList.add(sp.loadToMemory(Integer.toString(this.getSecondPointerOffset()), Registers.regC));
                InstructionList.add(sp.getFromIndAddr(Registers.regC, Registers.regC));
                InstructionList.add(sp.compute(Operators.Add, Registers.regB, Registers.regC, Registers.regB));
            }

        }
        else {

            InstructionList.add(sp.loadToMemory(Integer.toString(this.getFirstPointerOffset()), Registers.regB));
            InstructionList.add(sp.getFromIndAddr(Registers.regB, Registers.regB));
            InstructionList.add(sp.loadToMemory(Integer.toString(this.getBeginning_offset()), Registers.regC));
            InstructionList.add(sp.compute(Operators.Add, Registers.regB, Registers.regC, Registers.regB));
        }
        return InstructionList;
    }
    public List<Instruction> getChangeInstructions(boolean shared){
        List<Instruction> InstructionList = new ArrayList<>();
        Sprockell sp = new Sprockell();
        InstructionList.addAll(this.getArrPointer());
        if (shared){
            InstructionList.add(sp.writeToMemory(Registers.regA,Registers.regB));
        }
        else {
            InstructionList.add(sp.storeInMemory(Registers.regA, Registers.regB));
        }
        return  InstructionList;
    }
    public List<Instruction> getIDCallInstructions(boolean shared){
        List<Instruction> InstructionList = new ArrayList<>();
        Sprockell sp = new Sprockell();
        InstructionList.addAll(this.getArrPointer());
        if (shared){
            InstructionList.add(sp.readInst(Registers.regB));
            InstructionList.add(sp.receive(Registers.regA));
        }
        else{
            InstructionList.add(sp.getFromIndAddr(Registers.regA,Registers.regB));
        }
        InstructionList.add(sp.push(Registers.regA));
        return  InstructionList;
    }
}
