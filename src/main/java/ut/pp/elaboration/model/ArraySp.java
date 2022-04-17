package ut.pp.elaboration.model;

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

    private int beginning_offset;
     private ArrayPointer[] pointers;

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

}
