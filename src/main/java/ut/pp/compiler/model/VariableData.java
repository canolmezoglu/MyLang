package ut.pp.compiler.model;


import ut.pp.compiler.model.enums.MyType;

public class VariableData {
    MyType type;
    int sizeCurr;
    boolean isParameter = false;
    boolean global = false;
    int columnCount = 0;
    boolean pointer = false;

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public boolean isParameter() {
        return isParameter;
    }


    public boolean isPointer() {
        return pointer;
    }

    public void setPointer(boolean pointer) {
        this.pointer = pointer;
    }


    public VariableData(MyType type,int sizeCurr){
        this.sizeCurr = sizeCurr;
        this.type = type;

    }
    public VariableData(MyType type,int sizeCurr,boolean global){
        this.sizeCurr = sizeCurr;
        this.type = type;
        this.global = global;

    }

    public void makeIntoParameter (){
        this.isParameter = true;
    }
    public void makeIntoPointer (){this.pointer = true;}

    public void setColumnCount (int columnCount){
        this.columnCount = columnCount;
    }

    public int getColumnCount (){
        return this.columnCount;
    }

    public MyType getType() {
        return type;
    }

    public int getSizeCurr() {
        return sizeCurr;
    }
}
