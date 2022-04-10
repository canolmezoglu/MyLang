import Sprockell
prog :: [Instruction]
prog = [Load  (ImmValue 0) regA,WriteInstr  regA numberIO,EndProg]

main = run[prog]