import Sprockell
prog :: [Instruction]
prog = [Compute  Equal regSprID reg0 regE,Branch  regE (Rel (3)),Load  (ImmValue 1) regE,WriteInstr  regE (IndAddr regSprID),Load  (ImmValue 1) regA,Compute  Sub regA regSprID regA,Branch  regA (Rel (2)),Jump  (Rel (39)),Load  (ImmValue 2) regE,Compute  Sub regE regSprID regE,Branch  regE (Rel (2)),Jump  (Rel (73)),Load  (ImmValue 3) regA,Compute  Sub regA regSprID regA,Branch  regA (Rel (2)),Jump  (Rel (99)),Load  (ImmValue 4) regE,Compute  Sub regE regSprID regE,Branch  regE (Rel (2)),Jump  (Rel (125)),Load  (ImmValue 0) regA,WriteInstr  regA (DirAddr 5),WriteInstr  reg0 (DirAddr 1),ReadInstr  (DirAddr 1),Receive  regB,Load  (ImmValue 2) regC,Compute  Sub regC regB regC,Branch  regB (Rel (-4)),WriteInstr  reg0 (DirAddr 4),ReadInstr  (DirAddr 4),Receive  regD,Load  (ImmValue 2) regE,Compute  Sub regE regD regE,Branch  regD (Rel (-4)),TestAndSet  (DirAddr 1),Receive  regA,Compute  Equal regA reg0 regA,Branch  regA (Rel (-3)),TestAndSet  (DirAddr 4),Receive  regB,Compute  Equal regB reg0 regB,Branch  regB (Rel (-3)),ReadInstr  (DirAddr 5),Receive  regA,WriteInstr  regA numberIO,EndProg,TestAndSet  (DirAddr 1),Receive  regC,Compute  Equal regC reg0 regC,Branch  regC (Rel (-3)),Load  (ImmValue 2) regC,WriteInstr  regC (DirAddr 1),TestAndSet  (DirAddr 0),Receive  regD,Compute  Equal regD reg0 regD,Branch  regD (Rel (-3)),ReadInstr  (DirAddr 5),Receive  regE,Load  (ImmValue 50) regB,Compute  Add regE regB regE,WriteInstr  regE (DirAddr 5),WriteInstr  reg0 (DirAddr 0),WriteInstr  reg0 (DirAddr 2),ReadInstr  (DirAddr 2),Receive  regC,Load  (ImmValue 2) regD,Compute  Sub regD regC regD,Branch  regC (Rel (-4)),WriteInstr  reg0 (DirAddr 3),ReadInstr  (DirAddr 3),Receive  regA,Load  (ImmValue 2) regB,Compute  Sub regB regA regB,Branch  regA (Rel (-4)),TestAndSet  (DirAddr 2),Receive  regE,Compute  Equal regE reg0 regE,Branch  regE (Rel (-3)),TestAndSet  (DirAddr 3),Receive  regC,Compute  Equal regC reg0 regC,Branch  regC (Rel (-3)),WriteInstr  reg0 (DirAddr 1),EndProg,TestAndSet  (DirAddr 2),Receive  regD,Compute  Equal regD reg0 regD,Branch  regD (Rel (-3)),Load  (ImmValue 2) regD,WriteInstr  regD (DirAddr 2),Load  (ImmValue 100) regA,Store  regA (DirAddr 1),Load  (DirAddr 1) regB,Load  (ImmValue 0) regE,Compute  Gt regB regE regB,Branch  regB (Rel (2)),Jump  (Rel (16)),Load  (DirAddr 1) regC,Load  (ImmValue 1) regD,Compute  Sub regC regD regC,Store  regC (DirAddr 1),TestAndSet  (DirAddr 0),Receive  regA,Compute  Equal regA reg0 regA,Branch  regA (Rel (-3)),ReadInstr  (DirAddr 5),Receive  regE,Load  (ImmValue 1) regC,Compute  Add regE regC regE,WriteInstr  regE (DirAddr 5),WriteInstr  reg0 (DirAddr 0),Jump  (Rel (-19)),WriteInstr  reg0 (DirAddr 2),EndProg,TestAndSet  (DirAddr 3),Receive  regA,Compute  Equal regA reg0 regA,Branch  regA (Rel (-3)),Load  (ImmValue 2) regA,WriteInstr  regA (DirAddr 3),Load  (ImmValue 50) regD,Store  regD (DirAddr 1),Load  (DirAddr 1) regC,Load  (ImmValue 0) regE,Compute  Gt regC regE regC,Branch  regC (Rel (2)),Jump  (Rel (16)),Load  (DirAddr 1) regA,Load  (ImmValue 1) regD,Compute  Sub regA regD regA,Store  regA (DirAddr 1),TestAndSet  (DirAddr 0),Receive  regE,Compute  Equal regE reg0 regE,Branch  regE (Rel (-3)),ReadInstr  (DirAddr 5),Receive  regD,Load  (ImmValue 2) regE,Compute  Sub regD regE regD,WriteInstr  regD (DirAddr 5),WriteInstr  reg0 (DirAddr 0),Jump  (Rel (-19)),WriteInstr  reg0 (DirAddr 3),EndProg,TestAndSet  (DirAddr 4),Receive  regA,Compute  Equal regA reg0 regA,Branch  regA (Rel (-3)),Load  (ImmValue 2) regA,WriteInstr  regA (DirAddr 4),Load  (ImmValue 100) regE,Store  regE (DirAddr 1),Load  (DirAddr 1) regD,Load  (ImmValue 0) regA,Compute  Gt regD regA regD,Branch  regD (Rel (2)),Jump  (Rel (16)),Load  (DirAddr 1) regE,Load  (ImmValue 1) regA,Compute  Sub regE regA regE,Store  regE (DirAddr 1),TestAndSet  (DirAddr 0),Receive  regA,Compute  Equal regA reg0 regA,Branch  regA (Rel (-3)),ReadInstr  (DirAddr 5),Receive  regE,Load  (ImmValue 1) regA,Compute  Sub regE regA regE,WriteInstr  regE (DirAddr 5),WriteInstr  reg0 (DirAddr 0),Jump  (Rel (-19)),WriteInstr  reg0 (DirAddr 4),EndProg]

main = run[prog,prog,prog,prog,prog]