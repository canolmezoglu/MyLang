package ut.pp.elaboration;

public class VariableData {
    MyType type;
    int sizeCurr;
    boolean isParameter = false;
    boolean global = false;
    int columnCount = 0;
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
