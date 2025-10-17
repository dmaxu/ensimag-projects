lexer grammar DecaLexer;

options {
   language=Java;
   // Tell ANTLR to make the generated lexer class extend the
   // the named class, which is where any supporting code and
   // variables will be placed.
   superClass = AbstractDecaLexer;
}

@members {
}

// Mots-clés réservés
   ASM : 'asm';
   CLASS : 'class';
   EXTENDS : 'extends';
   ELSE : 'else' ;
   FALSE : 'false';
   IF : 'if' ;
   INSTANCEOF :'instanceof';
   NEW : 'new';
   NULL : 'null';
   READINT : 'readInt';
   READFLOAT : 'readFloat';
   PRINT : 'print' ;
   PRINTLN : 'println' ;
   PRINTLNX : 'printlnx';
   PRINTX : 'printx';
   PROTECTED : 'protected';
   RETURN : 'return';
   THIS : 'this';
   TRUE : 'true';
   WHILE : 'while';


// Fragments pour réutilisation
   fragment LETTER : 'a'..'z'|'A'..'Z' ;
   fragment DIGIT : '0'..'9' ;
   fragment STRING_CAR : ~('"' | '\\' | '\n'); // Caractères autorisés sans échappement
   fragment ESCAPE_SEQ : '\\"' | '\\\\'; // Séquences d'échappement courantes
   fragment EOL : '\n'; // Retour à la ligne
   fragment POSITIVE_DIGIT: '1'..'9';
   fragment NUM : DIGIT+;
   fragment SIGN : ('+'|'-')?;
   fragment EXP : ('E' | 'e') SIGN NUM;
   fragment DEC: NUM '.' NUM;
   fragment FLOATDEC : (DEC | DEC EXP) ('F'|'f')?;
   fragment DIGITHEX : DIGIT | 'A'..'F' | 'a'..'f';
   fragment NUMHEX : DIGITHEX+;
   fragment FLOATHEX : ('0X' | '0x') NUMHEX '.' NUMHEX ('P'|'p') SIGN NUM ('F' | 'f')?;

// Identifiants
   IDENT : (LETTER|'_'|'$')(LETTER|'_'|'$'|DIGIT)* ;

// Chaînes de caractères
   STRING : '"' (STRING_CAR | ESCAPE_SEQ)* '"' ; // Chaîne simple
   MULTI_LINE_STRING : '"' (STRING_CAR | EOL | ESCAPE_SEQ)* '"' ; // Chaîne multiligne

// Entiers
   INT : '0' | POSITIVE_DIGIT DIGIT*;

// Flottants
   FLOAT : FLOATDEC | FLOATHEX;

// Symboles
   AND : '&&';
   CBRACE : '}';
   CBRACKET : ']';
   COMMA : ',';
   CPARENT : ')';
   DOT : '.';
   EQUALS : '=';
   EQEQ : '==';
   EXCLAM : '!';
   GEQ : '>=';
   GT : '>';
   LEQ : '<=';
   LT : '<';
   MINUS : '-';
   NEQ : '!=';
   OBRACE : '{';
   OBRACKET : '[';
   OPARENT : '(';
   OR : '||';
   PERCENT : '%';
   PLUS : '+';
   SEMI : ';';
   SLASH : '/';
   TIMES : '*';

// Commentaires
   SINGLE_LINE_COMMENT : '//' (.*?)('\n') {skip();} ;
   MULTI_LINE_COMMENT : ('/*' .*? '*/') {skip();} ;

// Espaces blancs
   WS : (' ' | '\t' | '\r' | '\n') {skip();}; // Ignore spaces, tabs, newlines et whitespaces

// INCLUDES
   fragment FILENAME : (LETTER | DIGIT |'.'|'-'|'_')+;
   INCLUDE : '#include' (' ')* '"' FILENAME '"' {super.doInclude(getText());};

