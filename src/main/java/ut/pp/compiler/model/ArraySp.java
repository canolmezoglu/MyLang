package ut.pp.compiler.model;

import ut.pp.compiler.model.enums.Operators;
import ut.pp.compiler.model.enums.Registers;

import java.util.ArrayList;
import java.util.List;

public class ArraySp {
    /***
     * Class to handle dynamic arrays
     */
    class ArrayPointer{
        /***
         * Class to keep track of the array
         */
        boolean dynamic;
        int offset;

        /***
         * Constructor for the ArrayPointer class with the following instance variables
         * @param dynamic is the array dynamic or not
         * @param offset address where the array val is stored
         */
        public ArrayPointer(boolean dynamic, int offset) {
            this.dynamic = dynamic;
            this.offset = offset;
        }
    }
    private int columnSize;
    private int beginning_offset;
    private ArrayPointer[] pointers;

    /**
     * Get the column size in a 2d array
     * @return columnSize
     */
    public int getColumnSize() {
        return columnSize;
    }

    /***
     * Set the column size in a 2d array
     * @param columnSize column size of the array
     */
    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;
    }

    /***
     * Constructor for the ArraySp class
     * @param beginning_offset offset values - address where the array is saved
     */
    public ArraySp(int beginning_offset) {
        this.beginning_offset = beginning_offset;
    }

    /**
     * Get the initial offset value of the array where it is saved
     * @return beginning_offset
     */
    public int getBeginning_offset() {
        return beginning_offset;
    }

    /**
     * Add a pointer to point to the array
     * @param dynamic1 is the array a dynamic array
     * @param offset1 offset value
     */
    public void addPointer(boolean dynamic1, int offset1){
        pointers = new ArrayPointer[1];
        pointers[0] = new ArrayPointer(dynamic1,offset1);
    }

    /**
     * Add a pointer to point to the array
     * @param dynamic1 is the array a dynamic array
     * @param offset1 offset value 1
     * @param dynamic2 is the array a dynamic array
     * @param offset2 offset value 2
     */
    public void addPointer(boolean dynamic1, int offset1,boolean dynamic2, int offset2){
        pointers = new ArrayPointer[2];

        pointers[0] = new ArrayPointer(dynamic1,offset1);
        pointers[1] = new ArrayPointer(dynamic2,offset2);

    }

    /***
     * Check the first pointer to the array is dynamic
     * @return this.pointers[0].dynamic
     */
    public boolean getFirstPointerDynamic(){
        return this.pointers[0].dynamic;
    }

    /***
     * Get offset value of the first pointer
     * @return this.pointers[0].offset
     */
    public int getFirstPointerOffset(){
        return this.pointers[0].offset;
    }

    /***
     * Check the second pointer to the array is dynamic
     * @return this.pointers[1].offset
     */
    public boolean getSecondPointerDynamic(){
        return this.pointers[1].dynamic;
    }

    /***
     * Get offset value of the first pointer
     * @return this.pointers[1].offset
     */
    public int getSecondPointerOffset(){
        return this.pointers[1].offset;
    }

    /***
     * Get the size of the pointer
     * @return this.pointers.length
     */
    public int getPointerSize(){
        return this.pointers.length;
    }

    /***
     * Generate sprockell code to get the array value
     * dynamically
     * @return list of sprockell instructions
     */
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

    /**
     * Generate sprockell code when an array value is changed.
     * Generates different instructions based on the parameter shared.
     * @param shared is the array shared or not
     * @return list of sprockell instructions
     */
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

    /**
     * Generate sprockell code when an array is called by its identifier
     * @param shared is the array shared or not
     * @return list of sprockell instructions
     */
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
