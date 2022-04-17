package ut.pp.elaboration;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import ut.pp.elaboration.model.ArraySp;
import ut.pp.elaboration.model.ThreadSp;
import ut.pp.parser.MyLangParser;

import java.util.*;
import java.util.function.Function;

/** Class holding the results of the Simple Pascal checker. */
public class Result {
    /** Mapping from statements and expressions to the atomic
     * subtree that is their entry in the control flow graph. */
    private final ParseTreeProperty<ParserRuleContext> entries = new ParseTreeProperty<>();
    /** Mapping from expressions to types. */
    private final ParseTreeProperty<MyType> types = new ParseTreeProperty<>();
    /** Mapping from variables to coordinates. */
    private final ParseTreeProperty<Integer> offsets = new ParseTreeProperty<>();
    private final ParseTreeProperty<ThreadSp> threads = new ParseTreeProperty<>();
    private final ParseTreeProperty<Boolean> globals = new ParseTreeProperty<>();
    private final ParseTreeProperty<Set<Integer>> childrenThreads = new ParseTreeProperty<>();
    private HashMap<String, FunctionData> functionDataHashMap = new HashMap<>();
    private final ParseTreeProperty<ArraySp> dynamicArrayCall = new ParseTreeProperty<>();

    /** Adds an association from parse tree node to the flow graph entry. */
    public void setEntry(ParseTree node, ParserRuleContext entry) {
        this.entries.put(node, entry);
    }

    /** Returns the flow graph entry associated
     * with a given parse tree node. */
    public ParserRuleContext getEntry(ParseTree node) {
        return this.entries.get(node);
    }

    /** Adds an association from a parse tree node containing a
     * variable reference to the offset
     * of that variable. */
    public void setOffset(ParseTree node, int offset) {
        this.offsets.put(node, offset);
    }

    /** Returns the declaration offset of the variable
     * accessed in a given parse tree node. */
    public int getOffset(ParseTree node) {
        return this.offsets.get(node);
    }

    /** Adds an association from a parse tree expression, type,
     * or assignment target node to the corresponding (inferred) type. */
    public void setType(ParseTree node, MyType type) {
        this.types.put(node, type);
    }

    /** Returns the type associated with a given parse tree node. */
    public MyType getType(ParseTree node) {
        return this.types.get(node);
    }

    /** Adds an association from a parse tree expression, type,
     * or assignment target node to the corresponding (inferred) type. */
    public void setThread(ParseTree node, ThreadSp thread) {
        this.threads.put(node, thread);
    }

    /** Returns the type associated with a given parse tree node. */
    public ThreadSp getThread(ParseTree node) {
        return this.threads.get(node);
    }
    /** Adds an association from a parse tree expression, type,
     * or assignment target node to the corresponding (inferred) type. */
    public void setGlobal(ParseTree node, Boolean global) {
        this.globals.put(node, global);
    }

    /** Returns the type associated with a given parse tree node. */
    public Boolean getGlobal(ParseTree node) {
        return this.globals.get(node);
    }

    public void addChild(ParseTree node, int childId){
        if (this.childrenThreads.get(node) == null ){
            this.childrenThreads.put(node,new HashSet<Integer>());
        }
        this.childrenThreads.get(node).add(childId);
    }
    public Set<Integer> getChildren(ParseTree node) {
        return this.childrenThreads.get(node);
    }
    public void addChild(ParseTree node, Set<Integer> childId) {
        if (this.childrenThreads.get(node) == null ){
            this.childrenThreads.put(node,new HashSet<Integer>());
        }
        this.childrenThreads.get(node).addAll(childId);
    }
    public void putFunctionDataMap(String id, FunctionData data){
        this.functionDataHashMap.put(id,data);
    }
    public FunctionData getFunctionData(String id){
        return this.functionDataHashMap.get(id);
    }
    public boolean functionDataHashMapContains(String id){
        return this.functionDataHashMap.containsKey(id);
    }
    public void addDynamicArrayCall(ParseTree node,ArraySp arraySp){
        this.dynamicArrayCall.put(node,arraySp);
    }
    public ArraySp getDynamicArrayCall(ParseTree node){
        return this.dynamicArrayCall.get(node);
    }
}
