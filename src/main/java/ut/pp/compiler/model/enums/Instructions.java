package ut.pp.compiler.model.enums;

public enum Instructions {
    /***
     * To represent the Sprockell Instructions
     */
    Compute,
    Jump,
    Branch,
    Load,
    Store,
    Push,
    Pop,
    ReadInstr,
    Receive,
    WriteInstr,
    TestAndSet,
    EndProg,
    Nop,
    Fake;
}
