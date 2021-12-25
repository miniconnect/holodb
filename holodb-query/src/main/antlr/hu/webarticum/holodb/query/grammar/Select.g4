grammar Select;

@header {
    package hu.webarticum.holodb.query.grammar;
}


selectQuery:
    selectPart
    fromPart
    joinPart?
    wherePart?
    groupByPart?
    orderPart?
    havingPart?
    limitPart?
    offsetPart?
;


selectPart: SELECT (countSelection | normalSelection);
normalSelection: selectableItem (COMMA selectableItem)*;
countSelection: beforeSelection aliasableCount afterSelection;
beforeSelection: (selectableItem COMMA)*;
afterSelection: (COMMA selectableItem)*;
selectableItem: aliasableExpression | wildcarded;
aliasableExpression: expression alias?;
aliasableCount: count alias?;
count: COUNT PAR_LEFT MUL PAR_RIGHT;
wildcarded: (parentName=name DOT)? MUL;
fromPart: FROM aliasableName;
joinPart: joinItem+;
joinItem: (innerJoin | leftJoin) aliasableName ON compoundName REL_EQ compoundName;
innerJoin: INNER? JOIN;
leftJoin: LEFT JOIN;
wherePart: WHERE atomicCondition (whereAndConnections | whereOrConnections)?;
whereAndConnections: (AND atomicCondition)*;
whereOrConnections: (OR atomicCondition)*;
groupByPart: GROUP BY compoundableName (COMMA compoundableName)*;
havingPart: HAVING expression;
orderPart: ORDER BY orderItem (COMMA orderItem)*;
orderItem: compoundableName (ASC | DESC)?;
limitPart: LIMIT integerLiteral;
offsetPart: OFFSET integerLiteral;

atomicCondition:
    simpleAtomicCondition |
    PAR_LEFT simpleAtomicCondition PAR_RIGHT |
    extendedBetweenCondition;
simpleAtomicCondition:
	binaryCondition |
	isNullCondition |
	isNotNullCondition |
	betweenCondition;
binaryCondition: compoundableName relationOperator literal;
isNullCondition: compoundableName IS NULL;
isNotNullCondition: compoundableName IS NOT NULL;
betweenCondition: compoundableName BETWEEN literal AND literal;
extendedBetweenCondition: PAR_LEFT lowerBoundCondition AND upperBoundCondition PAR_RIGHT;
lowerBoundCondition: compoundableName lowerBoundOperator literal;
upperBoundCondition: compoundableName upperBoundOperator literal;
expression:
    expression arithmeticOperator1 expression |
    expression arithmeticOperator2 expression |
    expression relationOperator expression |
    isNullExpression |
    isNotNullExpression |
    betweenExpression |
    atomicExpression;
function: simpleName PAR_LEFT (expression (COMMA expression)*)? PAR_RIGHT;
isNullExpression: atomicExpression IS NULL;
isNotNullExpression: atomicExpression IS NOT NULL;
betweenExpression: atomicExpression BETWEEN atomicExpression AND atomicExpression;
atomicExpression: literal | compoundableName | function | PAR_LEFT expression PAR_RIGHT;

aliasableName: name alias?;
alias: (AS name | simpleName);
literal: decimalLiteral | integerLiteral | stringLiteral;
decimalLiteral: LIT_DECIMAL;
integerLiteral: LIT_INTEGER;
stringLiteral: LIT_STRING;
compoundableName: (parentName=name DOT)? selfName=name; 
compoundName: parentName=name DOT selfName=name;
name: quotedName | simpleName;
quotedName: QUOTEDNAME;
simpleName: SIMPLENAME;
arithmeticOperator1: MUL | DIV;
arithmeticOperator2: PLUS | MINUS;
relationOperator: REL_EQ | REL_NEQ | lowerBoundOperator | upperBoundOperator;
lowerBoundOperator: REL_LT | REL_LTEQ;
upperBoundOperator: REL_GT | REL_GTEQ;


SELECT: S E L E C T;
COUNT: C O U N T;
AS: A S;
FROM: F R O M;
INNER: I N N E R;
LEFT: L E F T;
JOIN: J O I N;
ON: O N;
WHERE: W H E R E;
NOT: N O T;
IS: I S;
NULL: N U L L;
BETWEEN: B E T W E E N;
AND: A N D;
OR: O R;
GROUP: G R O U P;
BY: B Y;
HAVING: H A V I N G;
ORDER: O R D E R;
ASC: A S C;
DESC: D E S C;
LIMIT: L I M I T;
OFFSET: O F F S E T;

QUOTEDNAME: '"' ('\\' . | '""' | ~[\\"] )* '"';
SIMPLENAME: [a-zA-Z_] [a-zA-Z_0-9]+;

LIT_STRING: '\'' ('\\' . | '\'\'' | ~[\\'] )* '\'';
LIT_DECIMAL: '-'? [0-9]+ '.' [0-9]+;
LIT_INTEGER: '-'? [0-9]+;

REL_EQ: '=';
REL_NEQ: '!=' | '<>';
REL_GT: '>';
REL_GTEQ: '>=';
REL_LT: '<';
REL_LTEQ: '<=';
PAR_LEFT: '(';
PAR_RIGHT: ')';
DOT: '.';
COMMA: ',';
PLUS: '+';
MINUS: '-';
MUL: '*';
DIV: '/';

WHITESPACE: [ \n\t\r] -> skip;


fragment A: [Aa];
fragment B: [Bb];
fragment C: [Cc];
fragment D: [Dd];
fragment E: [Ee];
fragment F: [Ff];
fragment G: [Gg];
fragment H: [Hh];
fragment I: [Ii];
fragment J: [Jj];
fragment K: [Kk];
fragment L: [Ll];
fragment M: [Mm];
fragment N: [Nn];
fragment O: [Oo];
fragment P: [Pp];
fragment Q: [Qq];
fragment R: [Rr];
fragment S: [Ss];
fragment T: [Tt];
fragment U: [Uu];
fragment V: [Vv];
fragment W: [Ww];
fragment X: [Xx];
fragment Y: [Yy];
fragment Z: [Zz];
