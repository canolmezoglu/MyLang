import Sprockell
prog :: [Instruction]
prog = [Compute  Equal regSprID reg0 regA,Branch  regA (Rel (3)),Load  (ImmValue 1) regA,WriteInstr  regA (IndAddr regSprID),Load  (ImmValue 1) regA,Compute  Sub regA regSprID regA,Branch  regA (Rel (2)),Jump  (Rel (54)),Load  (ImmValue 2) regA,Compute  Sub regA regSprID regA,Branch  regA (Rel (2)),Jump  (Rel (88)),Load  (ImmValue 3) regA,Compute  Sub regA regSprID regA,Branch  regA (Rel (2)),Jump  (Rel (122)),Load  (ImmValue 4) regA,Compute  Sub regA regSprID regA,Branch  regA (Rel (2)),Jump  (Rel (156)),WriteInstr  reg0 (DirAddr 1),ReadInstr  (DirAddr 1),Receive  regA,Load  (ImmValue 2) regB,Compute  Sub regB regA regB,Branch  regA (Rel (-4)),WriteInstr  reg0 (DirAddr 2),ReadInstr  (DirAddr 2),Receive  regA,Load  (ImmValue 2) regB,Compute  Sub regB regA regB,Branch  regA (Rel (-4)),WriteInstr  reg0 (DirAddr 3),ReadInstr  (DirAddr 3),Receive  regA,Load  (ImmValue 2) regB,Compute  Sub regB regA regB,Branch  regA (Rel (-4)),WriteInstr  reg0 (DirAddr 4),ReadInstr  (DirAddr 4),Receive  regA,Load  (ImmValue 2) regB,Compute  Sub regB regA regB,Branch  regA (Rel (-4)),TestAndSet  (DirAddr 1),Receive  regA,Compute  Equal regA reg0 regA,Branch  regA (Rel (-3)),TestAndSet  (DirAddr 2),Receive  regA,Compute  Equal regA reg0 regA,Branch  regA (Rel (-3)),TestAndSet  (DirAddr 3),Receive  regA,Compute  Equal regA reg0 regA,Branch  regA (Rel (-3)),TestAndSet  (DirAddr 4),Receive  regA,Compute  Equal regA reg0 regA,Branch  regA (Rel (-3)),EndProg,TestAndSet  (DirAddr 1),Receive  regA,Compute  Equal regA reg0 regA,Branch  regA (Rel (-3)),Load  (ImmValue 2) regA,WriteInstr  regA (DirAddr 1),Load  (ImmValue 20) regA,Push  regA,Pop  regA,Store  regA (DirAddr 1),Load  (DirAddr 1) regA,Push  regA,Load  (ImmValue 0) regA,Push  regA,Pop  regB,Pop  regA,Compute  Gt regA regB regA,Push  regA,Pop  regA,Branch  regA (Rel (2)),Jump  (Rel (16)),Load  (DirAddr 1) regA,Push  regA,Load  (ImmValue 1) regA,Push  regA,Pop  regB,Pop  regA,Compute  Sub regA regB regA,Push  regA,Pop  regA,Store  regA (DirAddr 1),Load  (ImmValue 1) regA,Push  regA,Pop  regA,WriteInstr  regA numberIO,Jump  (Rel (-25)),WriteInstr  reg0 (DirAddr 1),EndProg,TestAndSet  (DirAddr 2),Receive  regA,Compute  Equal regA reg0 regA,Branch  regA (Rel (-3)),Load  (ImmValue 2) regA,WriteInstr  regA (DirAddr 2),Load  (ImmValue 20) regA,Push  regA,Pop  regA,Store  regA (DirAddr 1),Load  (DirAddr 1) regA,Push  regA,Load  (ImmValue 0) regA,Push  regA,Pop  regB,Pop  regA,Compute  Gt regA regB regA,Push  regA,Pop  regA,Branch  regA (Rel (2)),Jump  (Rel (16)),Load  (DirAddr 1) regA,Push  regA,Load  (ImmValue 1) regA,Push  regA,Pop  regB,Pop  regA,Compute  Sub regA regB regA,Push  regA,Pop  regA,Store  regA (DirAddr 1),Load  (ImmValue 2) regA,Push  regA,Pop  regA,WriteInstr  regA numberIO,Jump  (Rel (-25)),WriteInstr  reg0 (DirAddr 2),EndProg,TestAndSet  (DirAddr 3),Receive  regA,Compute  Equal regA reg0 regA,Branch  regA (Rel (-3)),Load  (ImmValue 2) regA,WriteInstr  regA (DirAddr 3),Load  (ImmValue 20) regA,Push  regA,Pop  regA,Store  regA (DirAddr 1),Load  (DirAddr 1) regA,Push  regA,Load  (ImmValue 0) regA,Push  regA,Pop  regB,Pop  regA,Compute  Gt regA regB regA,Push  regA,Pop  regA,Branch  regA (Rel (2)),Jump  (Rel (16)),Load  (DirAddr 1) regA,Push  regA,Load  (ImmValue 1) regA,Push  regA,Pop  regB,Pop  regA,Compute  Sub regA regB regA,Push  regA,Pop  regA,Store  regA (DirAddr 1),Load  (ImmValue 3) regA,Push  regA,Pop  regA,WriteInstr  regA numberIO,Jump  (Rel (-25)),WriteInstr  reg0 (DirAddr 3),EndProg,TestAndSet  (DirAddr 4),Receive  regA,Compute  Equal regA reg0 regA,Branch  regA (Rel (-3)),Load  (ImmValue 2) regA,WriteInstr  regA (DirAddr 4),Load  (ImmValue 20) regA,Push  regA,Pop  regA,Store  regA (DirAddr 1),Load  (DirAddr 1) regA,Push  regA,Load  (ImmValue 0) regA,Push  regA,Pop  regB,Pop  regA,Compute  Gt regA regB regA,Push  regA,Pop  regA,Branch  regA (Rel (2)),Jump  (Rel (16)),Load  (DirAddr 1) regA,Push  regA,Load  (ImmValue 1) regA,Push  regA,Pop  regB,Pop  regA,Compute  Sub regA regB regA,Push  regA,Pop  regA,Store  regA (DirAddr 1),Load  (ImmValue 4) regA,Push  regA,Pop  regA,WriteInstr  regA numberIO,Jump  (Rel (-25)),WriteInstr  reg0 (DirAddr 4),EndProg]

main = run[prog,prog,prog,prog,prog]