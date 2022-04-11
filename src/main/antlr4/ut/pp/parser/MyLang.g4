grammar MyLang;

program: instruction+
    ;
instruction:
      statement                                 #statementInst
    | ifConstruct                               #ifInst
    | whileConstruct                            #whileInst
    | parallelConstruct                         #parallelInst
    | printConstruct                            #printInst
    | lockConstruct                             #lockInst
    ;

statement: declaration #declStat
           | changeAss #changeStat
           | declareArray #declArray
           | declare2dArray #decl2dArray
           ;

declare2dArray: access? type ID LSQR NUM RSQR LSQR NUM RSQR ASS LBRACE darray (COMMA darray)* RBRACE END;

declareArray: access? type ID LSQR NUM RSQR ASS darray END;

darray: LBRACE expr (COMMA (expr))* RBRACE;

ifConstruct :  IF LPAR expr RPAR block (ELSE block)? ;

whileConstruct :  WHILE LPAR expr RPAR block  ;

parallelConstruct: PARALLEL LBRACE threadConstruct+ RBRACE;

threadConstruct : THREAD LBRACE instruction+ RBRACE;

printConstruct : PRINT LPAR expr RPAR END;

lockConstruct : LOCK instruction* UNLOCK;

block: LBRACE instruction* RBRACE;
// all the first+ is same below, problem?
expr: prefixOp expr        #prfExpr
    | expr mult expr       #multExpr
    | expr addOp expr       #addExpr
    | expr comp expr       #compExpr
    | expr booleanOp expr  #boolExpr
    | LPAR expr RPAR       #parExpr
    | primitive            #primitiveExpr
    | ID                   #idExpr
    ;

declaration: access? type ID ASS expr? END;

changeAss: ID ASS expr END  ;

primitive : NUM
            | booleanVal
            ;

booleanVal :  (TRUE|FALSE);

prefixOp: MINUS | NOT;

mult: STAR;

addOp: PLUS | MINUS;

access: GLOBAL | SHARED ;

booleanOp:  AND | OR ;

comp: LE | LT | GE | GT | EQ | NE;

type: INTEGER
    | BOOLEAN
    ;

AND:    'and';
BOOLEAN: 'bool' ;
INTEGER: 'int' ;
ELSE:    'else' ;
END:     ';' ;
FALSE:   'false';
SHARED: 'shared';
GLOBAL: 'global';
IF:      'if' ;
THREAD:  'thread' ;
NOT:     'not' ;
OR:      'or' ;
TRUE:    'true' ;
WHILE:  'while';
PRINT: 'print';
PARALLEL: 'parallel';
LOCK : 'lock';
UNLOCK : 'unlock';

ASS: '=';
EQ:     '==';
GE:     '>=';
GT:     '>';
LE:     '<=';
LBRACE: '{';
LPAR:   '(';
LSQR:   '[';
LT:     '<';
MINUS:  '-';
NE:     '!=';
PLUS:   '+';
RBRACE: '}';
RPAR:   ')';
RSQR:   ']';
STAR:   '*';
COMMA:   ',';
ARR_INDEX: '%';



fragment LETTER: [a-zA-Z];
fragment DIGIT: [0-9];

NUM: DIGIT+;
ID: LETTER (LETTER | DIGIT |ARR_INDEX |COMMA )*;
WS: [ \t\r\n]+ -> skip;