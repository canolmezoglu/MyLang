grammar MyLang;

program: stat+
    ;
stat: RPAR stat* LPAR                           #blockStat
    | SHARED? type target '=' expr? END          #assStat
    | target '=' expr END                       #changeStat
    | IF LPAR expr RPAR stat (ELSE stat)?       #ifStat
    | WHILE LPAR expr RPAR LBRACE stat? RBRACE   #whileStat
    | FORK LPAR target RPAR  fstat fstat        #forkStat
    ;

fstat: LBRACE stat* RBRACE #forkBlock
    ;

target
    : ID               #idTarget
    ;

expr: prfOp expr        #prfExpr
    | expr mult expr  #multExpr
    | expr plus expr  #plusExpr
    | expr comp expr  #compExpr
    | expr ( AND | OR ) expr  #boolExpr
    | LPAR expr RPAR    #parExpr
    | ID                #idExpr
    | NUM               #numExpr
    | TRUE              #trueExpr
    | FALSE             #falseExpr
    ;

prfOp: MINUS | NOT;

mult: STAR;

plus: PLUS | MINUS;


comp: LE | LT | GE | GT | EQ | NE;

type: INTEGER  #intType
    | BOOLEAN  #boolType
    ;

AND:    '&&';
BOOLEAN: 'bool' ;
INTEGER: 'int' ;
ELSE:    'else' ;
END:     ';' ;
FALSE:   'false';
SHARED: 'shared';
IF:      'if' ;
FORK:    'fork' ;
NOT:     'not' ;
OR:      '||' ;
TRUE:    'true' ;
WHILE:  'while';

COLON:  ':';
COMMA:  ',';
DOT:    '.';
DQUOTE: '"';
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
SLASH:  '/';
STAR:   '*';


fragment LETTER: [a-zA-Z];
fragment DIGIT: [0-9];

NUM: DIGIT+;
ID: LETTER (LETTER | DIGIT)*;
WS: [ \t\r\n]+ -> skip;