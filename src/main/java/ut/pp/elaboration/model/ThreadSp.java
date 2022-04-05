package ut.pp.elaboration.model;

import java.util.ArrayList;
import java.util.List;

public class ThreadSp {
    int threadnr;
    List<ThreadSp> children;
    int parentnr;
    public ThreadSp(int threadnr,int parentnr){
        this.threadnr = threadnr;
        this.children = new ArrayList<>();
        this.parentnr = parentnr;
    }
    public void addchild(ThreadSp thread){
        children.add(thread);
    }
    public List<ThreadSp> getChildren(){
        return this.children;
    }

    public int getParentnr() {
        return parentnr;
    }

    public int getThreadnr() {
        return threadnr;
    }
}
