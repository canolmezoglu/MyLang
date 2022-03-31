import Sprockell
prog :: [Instruction]
prog=[ Load (ImmValue 0) regA 
     ,Store regA (DirAddr 0) 
     ,Load (ImmValue 67) regB 
     ,Store regB (DirAddr 0) 
     ,EndProg
     ]

main = run[prog ]