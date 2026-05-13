package AnalizadorLexico;

public class Token {

    //TIPOS DE TOKENS
    public enum TipoToken {

        //PALABRAS RESERVADAS
        CONST, VOID, RETURN,
        INT, FLOAT, STRING, BOOL,
        IF, ELSE, FOR, WHILE,
        TRUE, FALSE, READ, WRITE,

        //IDENTIFICADORES Y LITERALES
        ID, INTNUM, FLOATNUM, PERNUM, STRINGWORD,

        //COMENTARIOS
        COMMENT,

        //OPERADORES Y SIMBOLOS
        PARENIZQ, PARENDER, PYC, COMA,
        SUM, REST, MULT, DIV, IGUAL,
        MENOR, MAYOR, MEIGUAL, MAIGUAL, ESIGUAL, NOIGUAL,
        INC, DEC, NOT,

        //CONTROL DE INDENTACION
        NEWLINE, INDENT, DEDENT,

        //FINALIZACION
        EOF, DESCONOCIDO
    }

    //ATRIBUTOS
    private final TipoToken tipo;
    private final String lexema;
    private final int linea;
    private final int columna;

    //CONSTRUCTOR
    public Token(TipoToken tipo, String lexema, int linea, int columna) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linea = linea;
        this.columna = columna;
    }

    //GETTERS
    public TipoToken getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    //METODOS
    //Retorna el token en formato entendible para el reporte
    @Override
    public String toString() {
        return String.format("line %d, col %d: [%s, \"%s\"]", linea, columna, tipo, lexema);
    }
}