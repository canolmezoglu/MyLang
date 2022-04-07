package ut.pp.elaboration;

public class VariableData {
    MyType type;
    int sizeCurr;
    boolean global;
    public VariableData(MyType type,int sizeCurr){
        this.sizeCurr = sizeCurr;
        this.type = type;

    }
    public VariableData(MyType type,int sizeCurr,boolean global){
        this.sizeCurr = sizeCurr;
        this.type = type;
        this.global = global;

    }


    public MyType getType() {
        return type;
    }

    public int getSizeCurr() {
        return sizeCurr;
    }
}
