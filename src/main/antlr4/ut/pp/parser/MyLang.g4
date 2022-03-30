grammar MyLang;

program: instruction+
    ;
instruction:
      statement                                 #statementInst
    | ifConstruct                               #ifInst
    | whileConstruct                            #whileInst
    | threadConstruct                           #threadInst
    | printConstruct                            #printInst
    ;

statement: declaration #declStat
           | changeAss #changeStat
           ;

ifConstruct :  IF LPAR expr RPAR LBRACE instruction* RBRACE (ELSE LBRACE instruction* RBRACE)? ;

whileConstruct :  WHILE LPAR (declaration+ COMMA)* expr RPAR LBRACE instruction* RBRACE  ;

threadConstruct : THREAD LBRACE instruction+ RBRACE;

printConstruct : PRINT LPAR expr RPAR END;

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

AND:    'and  ';
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

ASS: '=';
EQ:     '==';
GE:     '>=';
GT:     '>';
LE:     '<=';
LBRACE: '{';
LPAR:   '(';
LT:     '<';
MINUS:  '-';
NE:     '<>';
PLUS:   '+';
RBRACE: '}';
RPAR:   ')';
STAR:   '*';
COMMA:   ',';


fragment LETTER: [a-zA-Z];
fragment DIGIT: [0-9];

NUM: DIGIT+;
ID: LETTER (LETTER | DIGIT)*;
WS: [ \t\r\n]+ -> skip;