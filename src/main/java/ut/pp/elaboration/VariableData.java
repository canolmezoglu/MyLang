package ut.pp.elaboration;

public class VariableData {
    MyType type;
    int sizeCurr;
    public VariableData(MyType type,int sizeCurr){
        this.sizeCurr = sizeCurr;
        this.type = type;

    }

    public MyType getType() {
        return type;
    }

    public int getSizeCurr() {
        return sizeCurr;
    }
}
