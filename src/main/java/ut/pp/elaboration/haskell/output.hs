import Sprockell
prog :: [Instruction]
prog = [Load  (ImmValue 5) regA,Push  regA,Load  (ImmValue 2) regA,Push  regA,Pop  regB,Pop  regA,Compute  Add regA regB regA,Push  regA,Pop  regA,WriteInstr  regA numberIO,Load  (ImmValue 1) regA,Push  regA,Load  (ImmValue 0) regA,Push  regA,Pop  regB,Pop  regA,Compute  Or regA regB regA,Push  regA,Pop  regA,WriteInstr  regA numberIO,Load  (ImmValue 1) regA,Push  regA,Pop  regA,Load  (ImmValue 1) regB,Compute  Xor regA regB regA,Push  regA,Pop  regA,WriteInstr  regA numberIO,Load  (ImmValue 3) regA,Push  regA,Load  (ImmValue 5) regA,Push  regA,Pop  regA,Compute  Sub reg0 regA regA,Push  regA,Pop  regA,Pop  regB,Compute  Mul regA regB regA,Push  regA,Load  (ImmValue 4) regA,Push  regA,Pop  regB,Pop  regA,Compute  Add regA regB regA,Push  regA,Pop  regA,WriteInstr  regA numberIO,Load  (ImmValue 2147483647) regA,Push  regA,Pop  regA,Compute  Sub reg0 regA regA,Push  regA,Pop  regA,WriteInstr  regA numberIO,Load  (ImmValue 2147483647) regA,Push  regA,Pop  regA,WriteInstr  regA numberIO,EndProg]

main = run[prog]