package ut.pp.compiler.model;

import java.util.ArrayList;
import java.util.List;

public class ThreadSp {
    /***
     * Class to handle the different threads being used the program
     */
    int threadnr;
    List<ThreadSp> children;
    int parentnr;

    /***
     * Constructor to initialize this class a thread number and the value of the parent thread
     * @param threadnr this thread number
     * @param parentnr parent thread number
     */
    public ThreadSp(int threadnr,int parentnr){
        this.threadnr = threadnr;
        this.children = new ArrayList<>();
        this.parentnr = parentnr;
    }

    /**
     * Add a child to this thread
     * @param thread a thread object
     */
    public void addchild(ThreadSp thread){
        children.add(thread);
    }

    /***
     * Get all the children of this thread
     * @return this.children
     */
    public List<ThreadSp> getChildren(){
        return this.children;
    }

    /***
     * Get the parent number of this thread
     * @return parentnr
     */
    public int getParentnr() {
        return parentnr;
    }

    /***
     * Get the thread number
     * @return threadnr
     */
    public int getThreadnr() {
        return threadnr;
    }
}
