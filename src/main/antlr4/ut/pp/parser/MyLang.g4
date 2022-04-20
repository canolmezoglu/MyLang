grammar MyLang;

/** Outermost nonterminal **/
program: instruction*;

/** Program instructions **/
instruction:
      statement                                 #statementInst // used for declarations and changing
    | ifConstruct                               #ifInst        // standard if statement
    | whileConstruct                            #whileInst     // standard while statement
    | parallelConstruct                         #parallelInst  // creates parallel block
    | printConstruct                            #printInst     // print basic types
    | lockConstruct                             #lockInst      // locks and unlock
    | returnConstruct                           #returnInst    // returns from functions
    | functionConstruct                         #functionInst  // defines functions
    | runProcedureConstruct                     #runProInst    // runs void functions
    ;

/** Statement instructions **/
statement: declaration                          #declStat       // declares a variable of basic type
           | changeAss                          #changeStat     // changes a variable of basic type / array value
           | declareArray                       #declArray      // declares a one dimensional array
           | declare2dArray                     #decl2dArray    // declares a two dimensional array
           | declareEnum                        #declEnum       // declares an enum
           | declarePointer                     #declPointer    // declares a pointer
           ;


/** If statement **/
ifConstruct :  IF LPAR expr RPAR block (ELSE block)? ;

/** While statement **/
whileConstruct :  WHILE LPAR expr RPAR block  ;

/** Parallel block **/
parallelConstruct: PARALLEL LBRACE threadConstruct+ RBRACE;

/** Prints values **/
printConstruct : PRINT LPAR expr RPAR END;

/** locking and unlocking **/
lockConstruct : LOCK block UNLOCK;

/** Returning from functions **/
returnConstruct : RETURN expr? END;

/** Defining functions **/
functionConstruct: FUNCTION type ID LPAR ((type ID) (COMMA type ID )* )? RPAR block;

/** Void function caller **/
runProcedureConstruct: RUN LPAR factor RPAR END;

/** Thread definition **/
threadConstruct : THREAD block;

/** Block of instructions **/
block: LBRACE instruction* RBRACE;

/** Declaring a pointer   **/
declarePointer: POINTER ID ASS factor END;

/** Declaring an enum   **/
declareEnum: access? type ENUM ID enumAssign END;

/** Assigning values inside an enum **/
enumAssign: LBRACE ID (ASS (expr))? (COMMA ID (ASS (expr))?)* RBRACE;

/** Declaring a 2D array   **/
declare2dArray: access? type ID LSQR NUM RSQR LSQR NUM RSQR ASS LBRACE darray (COMMA darray)* RBRACE END;

/** Declaring a 1D array   **/
declareArray: access? type ID LSQR NUM RSQR ASS darray END;

/** The expression on the right hand side of an array declaration   **/
darray: LBRACE expr (COMMA (expr))* RBRACE;


/** Expressions for comparing - lowest precedence **/
expr: superiorExpr
    | superiorExpr comp superiorExpr
    ;

/** Expressions for addition, subtraction, OR operation - second lowest precedence **/
superiorExpr:  term
            |  superiorExpr addOp term
            |  superiorExpr OR term
            ;

/** Expressions for multiplication, division, AND operation - second highest precedence **/
term: factor
    | term mult  factor
    | term AND  factor
    ;

/** Expressions for prefixing, parenthesis , variable call, primitive declaration
 and function calls - maximum precedence **/
factor : prefixOp factor                     #prefixFactor
       |  LPAR expr RPAR                     #parFactor
       |  ID                                 #idFactor
       |  primitive                          #primitiveFactor
       | ID LPAR (expr (COMMA expr)* )? RPAR #funcCall
        ;


/** Declare basic types **/
declaration: access? type ID ASS expr END;

/** Used to change variables of basic types and array values **/
changeAss: ID ASS expr END  ;

/** Used to write primitive types**/
primitive : NUM
            | booleanVal
            ;

/** Used to write boolean values**/
booleanVal :  (TRUE|FALSE);

/** Used to declare prefix operations**/
prefixOp: MINUS | NOT;

/** Multiplication operators **/
mult: STAR | DIV;

/** Addition operators **/
addOp: PLUS | MINUS;

/** Shared access **/
access: SHARED ;

/** Comparation operators **/
comp: LE | LT | GE | GT | EQ | NE;

/** Variable types **/
type: INTEGER
    | BOOLEAN
    | VOID
    ;

/** The keywords **/
DIV: '/';
AND:    'and';
BOOLEAN: 'bool' ;
INTEGER: 'int' ;
ELSE:    'else' ;
END:     ';' ;
FALSE:   'false';
SHARED: 'shared';
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
RETURN : 'return';
FUNCTION: 'function';
ENUM: 'enum';
POINTER: 'pointer';
VOID: 'void';
RUN: 'run';

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
DOT: '.';
POINT: '&';


fragment LETTER: [a-zA-Z];
fragment DIGIT: [0-9];

NUM: DIGIT+;
ID: LETTER (LETTER | DIGIT | ARR_INDEX | DOT | POINT )*;
WS: [ \t\r\n]+ -> skip;
