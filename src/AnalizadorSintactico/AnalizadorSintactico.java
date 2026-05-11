package AnalizadorSintactico;

import AnalizadorLexico.Token;
import java.util.List;
import java.util.Set;

public class AnalizadorSintactico {

    //ATRIBUTOS
    private final Grafo grafo;
    private final Parser parser;

    //CONSTRUCTOR
    public AnalizadorSintactico() {
        this.grafo = new Grafo();
        construirGramaticaMiniLang();
        this.parser = new Parser(grafo);
    }

    //METODOS
    //Construye la gramatica del lenguaje MiniLang
    private void construirGramaticaMiniLang() {
        Set<String> FIN_EXPR = Set.of(
                "PARENDER", "PYC", "COMA", "ESIGUAL", "NOIGUAL", "MEIGUAL",
                "MAIGUAL", "MENOR", "MAYOR", "DEDENT", "EOF"
        );

        Set<String> FIN_COND = Set.of("PARENDER");

        Set<String> FIN_STMT = Set.of(
                "CONST", "INT", "FLOAT", "STRING", "BOOL", "IF", "ELSE", "FOR", "WHILE",
                "READ", "WRITE", "COMMENT", "DEDENT", "EOF"
        );

        grafo.agregarRegla(new Reglas(1, "Status", List.of("TRUE"), Set.of("PYC")));
        grafo.agregarRegla(new Reglas(2, "Status", List.of("FALSE"), Set.of("PYC")));

        grafo.agregarRegla(new Reglas(3, "Symb", List.of("INC"), Set.of("PARENDER")));
        grafo.agregarRegla(new Reglas(4, "Symb", List.of("DEC"), Set.of("PARENDER")));

        grafo.agregarRegla(new Reglas(5, "C", List.of("INTNUM"), FIN_EXPR));
        grafo.agregarRegla(new Reglas(6, "C", List.of("FLOATNUM"), FIN_EXPR));
        grafo.agregarRegla(new Reglas(7, "C", List.of("PERNUM"), FIN_EXPR));
        grafo.agregarRegla(new Reglas(8, "C", List.of("ID"), FIN_EXPR));
        grafo.agregarRegla(new Reglas(9, "C", List.of("PARENIZQ", "E", "PARENDER"), FIN_EXPR));

        grafo.agregarRegla(new Reglas(10, "A", List.of("A", "MULT", "C"), FIN_EXPR));
        grafo.agregarRegla(new Reglas(11, "A", List.of("A", "DIV", "C"), FIN_EXPR));
        grafo.agregarRegla(new Reglas(12, "A", List.of("C"), FIN_EXPR));

        grafo.agregarRegla(new Reglas(13, "E", List.of("E", "SUM", "A"), FIN_EXPR));
        grafo.agregarRegla(new Reglas(14, "E", List.of("E", "REST", "A"), FIN_EXPR));
        grafo.agregarRegla(new Reglas(15, "E", List.of("A"), FIN_EXPR));

        grafo.agregarRegla(new Reglas(16, "Num_Condition", List.of("E", "ESIGUAL", "E"), FIN_COND));
        grafo.agregarRegla(new Reglas(17, "Num_Condition", List.of("E", "MEIGUAL", "E"), FIN_COND));
        grafo.agregarRegla(new Reglas(18, "Num_Condition", List.of("E", "MAIGUAL", "E"), FIN_COND));
        grafo.agregarRegla(new Reglas(19, "Num_Condition", List.of("E", "MENOR", "E"), FIN_COND));
        grafo.agregarRegla(new Reglas(20, "Num_Condition", List.of("E", "MAYOR", "E"), FIN_COND));
        grafo.agregarRegla(new Reglas(21, "Num_Condition", List.of("E", "NOIGUAL", "E"), FIN_COND));

        grafo.agregarRegla(new Reglas(22, "Condition", List.of("NOT", "ID"), Set.of("PARENDER")));
        grafo.agregarRegla(new Reglas(23, "Condition", List.of("Num_Condition"), Set.of("PARENDER")));
        grafo.agregarRegla(new Reglas(24, "Condition", List.of("ID"), Set.of("PARENDER")));

        grafo.agregarRegla(new Reglas(25, "Int_Dec", List.of("INT", "ID", "IGUAL", "INTNUM", "PYC"), FIN_STMT));
        grafo.agregarRegla(new Reglas(26, "Float_Dec", List.of("FLOAT", "ID", "IGUAL", "FLOATNUM", "PYC"), FIN_STMT));
        grafo.agregarRegla(new Reglas(27, "Float_Dec", List.of("FLOAT", "ID", "IGUAL", "PERNUM", "PYC"), FIN_STMT));
        grafo.agregarRegla(new Reglas(28, "Float_Dec", List.of("FLOAT", "ID", "IGUAL", "INTNUM", "PYC"), FIN_STMT));
        grafo.agregarRegla(new Reglas(29, "String_Dec", List.of("STRING", "ID", "IGUAL", "STRINGWORD", "PYC"), FIN_STMT));
        grafo.agregarRegla(new Reglas(30, "Bool_Dec", List.of("BOOL", "ID", "IGUAL", "Status", "PYC"), FIN_STMT));

        grafo.agregarRegla(new Reglas(31, "Int_Dec", List.of("INT", "ID", "PYC"), FIN_STMT));
        grafo.agregarRegla(new Reglas(32, "Float_Dec", List.of("FLOAT", "ID", "PYC"), FIN_STMT));
        grafo.agregarRegla(new Reglas(33, "String_Dec", List.of("STRING", "ID", "PYC"), FIN_STMT));
        grafo.agregarRegla(new Reglas(34, "Bool_Dec", List.of("BOOL", "ID", "PYC"), FIN_STMT));

        grafo.agregarRegla(new Reglas(35, "Const_Int_Dec", List.of("CONST", "INT", "ID", "IGUAL", "INTNUM", "PYC"), FIN_STMT));
        grafo.agregarRegla(new Reglas(36, "Const_Int_Dec", List.of("CONST", "INT", "ID", "PYC"), FIN_STMT));

        grafo.agregarRegla(new Reglas(37, "Const_Float_Dec", List.of("CONST", "FLOAT", "ID", "IGUAL", "FLOATNUM", "PYC"), FIN_STMT));
        grafo.agregarRegla(new Reglas(38, "Const_Float_Dec", List.of("CONST", "FLOAT", "ID", "IGUAL", "PERNUM", "PYC"), FIN_STMT));
        grafo.agregarRegla(new Reglas(39, "Const_Float_Dec", List.of("CONST", "FLOAT", "ID", "IGUAL", "INTNUM", "PYC"), FIN_STMT));
        grafo.agregarRegla(new Reglas(40, "Const_Float_Dec", List.of("CONST", "FLOAT", "ID", "PYC"), FIN_STMT));

        grafo.agregarRegla(new Reglas(41, "Const_String_Dec", List.of("CONST", "STRING", "ID", "IGUAL", "STRINGWORD", "PYC"), FIN_STMT));
        grafo.agregarRegla(new Reglas(42, "Const_String_Dec", List.of("CONST", "STRING", "ID", "PYC"), FIN_STMT));

        grafo.agregarRegla(new Reglas(43, "Const_Bool_Dec", List.of("CONST", "BOOL", "ID", "IGUAL", "Status", "PYC"), FIN_STMT));
        grafo.agregarRegla(new Reglas(44, "Const_Bool_Dec", List.of("CONST", "BOOL", "ID", "PYC"), FIN_STMT));

        grafo.agregarRegla(new Reglas(45, "Const_Dec", List.of("Const_Int_Dec"), FIN_STMT));
        grafo.agregarRegla(new Reglas(46, "Const_Dec", List.of("Const_Float_Dec"), FIN_STMT));
        grafo.agregarRegla(new Reglas(47, "Const_Dec", List.of("Const_String_Dec"), FIN_STMT));
        grafo.agregarRegla(new Reglas(48, "Const_Dec", List.of("Const_Bool_Dec"), FIN_STMT));

        grafo.agregarRegla(new Reglas(49, "Dec_Stmt", List.of("Int_Dec"), FIN_STMT));
        grafo.agregarRegla(new Reglas(50, "Dec_Stmt", List.of("Float_Dec"), FIN_STMT));
        grafo.agregarRegla(new Reglas(51, "Dec_Stmt", List.of("String_Dec"), FIN_STMT));
        grafo.agregarRegla(new Reglas(52, "Dec_Stmt", List.of("Bool_Dec"), FIN_STMT));
        grafo.agregarRegla(new Reglas(53, "Dec_Stmt", List.of("Const_Dec"), FIN_STMT));

        grafo.agregarRegla(new Reglas(54, "Write_Item", List.of("STRINGWORD"), Set.of("COMA", "PARENDER")));
        grafo.agregarRegla(new Reglas(55, "Write_Item", List.of("E"), Set.of("COMA", "PARENDER")));

        grafo.agregarRegla(new Reglas(56, "Write_List", List.of("Write_List", "COMA", "Write_Item"), Set.of("PARENDER")));
        grafo.agregarRegla(new Reglas(57, "Write_List", List.of("Write_Item"), Set.of("PARENDER")));

        grafo.agregarRegla(new Reglas(58, "Write_Stmt", List.of("WRITE", "PARENIZQ", "Write_List", "PARENDER", "PYC"), FIN_STMT));
        grafo.agregarRegla(new Reglas(59, "Write_Stmt", List.of("WRITE", "PARENIZQ", "Write_List", "PARENDER"), FIN_STMT));

        grafo.agregarRegla(new Reglas(60, "Read_Stmt", List.of("READ", "PARENIZQ", "ID", "PARENDER", "PYC"), FIN_STMT));
        grafo.agregarRegla(new Reglas(61, "Read_Stmt", List.of("READ", "PARENIZQ", "ID", "PARENDER"), FIN_STMT));

        grafo.agregarRegla(new Reglas(62, "Comment_Stmt", List.of("COMMENT"), FIN_STMT));

        grafo.agregarRegla(new Reglas(63, "Stmts", List.of(), Set.of("DEDENT", "EOF")));
        grafo.agregarRegla(new Reglas(64, "Stmts", List.of("Stmt"), Set.of("DEDENT", "EOF")));
        grafo.agregarRegla(new Reglas(65, "Stmts", List.of("Stmts", "Stmt"), Set.of("DEDENT", "EOF")));

        grafo.agregarRegla(new Reglas(66, "Else_Stmt", List.of("ELSE", "NEWLINE", "INDENT", "Stmts", "DEDENT"), FIN_STMT));
        grafo.agregarRegla(new Reglas(67, "Else_Stmt", List.of("ELSE", "NEWLINE", "INDENT", "DEDENT"), FIN_STMT));

        grafo.agregarRegla(new Reglas(68, "If_Stmt", List.of("IF", "PARENIZQ", "Condition", "PARENDER", "NEWLINE", "INDENT", "Stmts", "DEDENT", "Else_Stmt"), FIN_STMT));
        grafo.agregarRegla(new Reglas(69, "If_Stmt", List.of("IF", "PARENIZQ", "Condition", "PARENDER", "NEWLINE", "INDENT", "Stmts", "DEDENT"), FIN_STMT));
        grafo.agregarRegla(new Reglas(70, "If_Stmt", List.of("IF", "PARENIZQ", "Condition", "PARENDER", "NEWLINE", "INDENT", "DEDENT"), FIN_STMT));

        grafo.agregarRegla(new Reglas(71, "While_Stmt", List.of("WHILE", "PARENIZQ", "Condition", "PARENDER", "NEWLINE", "INDENT", "Stmts", "DEDENT"), FIN_STMT));
        grafo.agregarRegla(new Reglas(72, "While_Stmt", List.of("WHILE", "PARENIZQ", "Condition", "PARENDER", "NEWLINE", "INDENT", "DEDENT"), FIN_STMT));

        grafo.agregarRegla(new Reglas(73, "For_Stmt", List.of("FOR", "PARENIZQ", "Int_Dec", "Num_Condition", "PYC", "ID", "Symb", "PARENDER", "NEWLINE", "INDENT", "Stmts", "DEDENT"), FIN_STMT));
        grafo.agregarRegla(new Reglas(74, "For_Stmt", List.of("FOR", "PARENIZQ", "Int_Dec", "Num_Condition", "PYC", "ID", "Symb", "PARENDER", "NEWLINE", "INDENT", "DEDENT"), FIN_STMT));

        grafo.agregarRegla(new Reglas(75, "Stmt", List.of("Dec_Stmt"), FIN_STMT));
        grafo.agregarRegla(new Reglas(76, "Stmt", List.of("If_Stmt"), FIN_STMT));
        grafo.agregarRegla(new Reglas(77, "Stmt", List.of("For_Stmt"), FIN_STMT));
        grafo.agregarRegla(new Reglas(78, "Stmt", List.of("While_Stmt"), FIN_STMT));
        grafo.agregarRegla(new Reglas(79, "Stmt", List.of("Comment_Stmt"), FIN_STMT));
        grafo.agregarRegla(new Reglas(80, "Stmt", List.of("Write_Stmt"), FIN_STMT));
        grafo.agregarRegla(new Reglas(81, "Stmt", List.of("Read_Stmt"), FIN_STMT));

        grafo.agregarRegla(new Reglas(82, "Program", List.of("Stmts"), Set.of("EOF")));
    }

    //Ejecuta el analisis sintactico usando la lista de tokens
    public boolean analizar(List<Token> tokens) {
        return parser.parsear(tokens);
    }

    //Retorna el parser para poder consultar errores
    public Parser getParser() {
        return parser;
    }
}