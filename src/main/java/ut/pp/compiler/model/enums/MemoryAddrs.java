package ut.pp.compiler.model.enums;

public enum MemoryAddrs {
    /***
     * To represent the different memory addresses
     * ImmValue Int                                  -- ImmValue n:is just the constant value n
     * DirAddr MemAddr                               -- DirAddr a: a is an address in memory (local or shared)
     * IndAddr RegAddr                               -- IndAddr p: p is a register, the content of this register is an address inmemory
     */
    DirAddr,ImmValue,IndAddr,numberIO;
}
