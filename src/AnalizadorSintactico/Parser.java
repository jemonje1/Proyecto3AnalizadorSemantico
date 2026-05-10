package AnalizadorSintactico;

import AnalizadorLexico.Token;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    //ATRIBUTOS
    private final Grafo grafo;
    private final List<String> erroresSintacticos;

    //CONSTRUCTOR
    public Parser(Grafo grafo) {
        this.grafo = grafo;
        this.erroresSintacticos = new ArrayList<>();
    }

    //METODOS
    //Parsea la lista de tokens usando reducciones por reglas
    public boolean parsear(List<Token> tokensOriginales) {
        erroresSintacticos.clear();

        List<Token> tokens = normalizarTokens(tokensOriginales);
        List<String> pilaSimbolos = new ArrayList<>();
        List<Token> pilaTokens = new ArrayList<>();

        int indice = 0;

        while (indice < tokens.size()) {
            Token actual = tokens.get(indice);
            String simbolo = actual.getTipo().name();

            if (actual.getTipo() == Token.TipoToken.DESCONOCIDO) {
                registrarError(actual, "No se esperaba el token '" + actual.getLexema() + "'");
                indice++;
                continue;
            }

            if (actual.getTipo() == Token.TipoToken.EOF) {
                boolean cambio;

                do {
                    cambio = reducir(pilaSimbolos, pilaTokens, "EOF");
                } while (cambio);

                if (pilaSimbolos.size() == 1 && "Program".equals(pilaSimbolos.get(0))) {
                    return erroresSintacticos.isEmpty();
                }

                if (pilaSimbolos.isEmpty()) {
                    return erroresSintacticos.isEmpty();
                }

                Token t = pilaTokens.isEmpty() ? actual : pilaTokens.get(pilaTokens.size() - 1);
                registrarError(t, "Cadena incompleta o estructura no reducida. Pila final: " + pilaSimbolos);
                return false;
            }

            pilaSimbolos.add(simbolo);
            pilaTokens.add(actual);

            boolean cambio;

            do {
                String lookahead = (indice + 1 < tokens.size()) ? tokens.get(indice + 1).getTipo().name() : "EOF";
                cambio = reducir(pilaSimbolos, pilaTokens, lookahead);
            } while (cambio);

            indice++;
        }

        return erroresSintacticos.isEmpty();
    }

    //Intenta reducir el final de la pila usando las reglas cargadas
    private boolean reducir(List<String> pilaSimbolos, List<Token> pilaTokens, String lookahead) {
        boolean redujoAlgo = false;

        if (debeInsertarStmtsVacio(pilaSimbolos, lookahead)) {
            pilaSimbolos.add("Stmts");
            pilaTokens.add(tokenVirtual(pilaTokens));
            return true;
        }

        for (Reglas regla : grafo.getReglas()) {
            List<String> cuerpo = regla.getCuerpo();

            if (cuerpo.isEmpty()) {
                continue;
            }

            if (!regla.aceptaLookahead(lookahead)) {
                continue;
            }

            if (terminaCon(pilaSimbolos, cuerpo)) {
                int eliminar = cuerpo.size();
                Token referencia = pilaTokens.get(pilaTokens.size() - 1);

                for (int i = 0; i < eliminar; i++) {
                    pilaSimbolos.remove(pilaSimbolos.size() - 1);
                    pilaTokens.remove(pilaTokens.size() - 1);
                }

                pilaSimbolos.add(regla.getCabeza());
                pilaTokens.add(referencia);
                redujoAlgo = true;
                break;
            }
        }

        if (redujoAlgo) {
            return true;
        }

        if (debeRecuperar(pilaSimbolos, lookahead)) {
            int idx = pilaSimbolos.size() - 1;
            Token t = pilaTokens.get(idx);

            registrarError(t, "No se esperaba el token '" + t.getLexema() + "'");
            pilaSimbolos.remove(idx);
            pilaTokens.remove(idx);
            return true;
        }

        return false;
    }

    //Inserta Stmts vacio cuando se cierra un bloque de indentacion
    private boolean debeInsertarStmtsVacio(List<String> pilaSimbolos, String lookahead) {
        if (!("DEDENT".equals(lookahead) || "EOF".equals(lookahead))) {
            return false;
        }

        if (pilaSimbolos.isEmpty()) {
            return true;
        }

        String tope = pilaSimbolos.get(pilaSimbolos.size() - 1);

        return "INDENT".equals(tope)
                || "NEWLINE".equals(tope)
                || "ELSE".equals(tope);
    }

    //Detecta algunos errores claros y permite continuar el parseo
    private boolean debeRecuperar(List<String> pilaSimbolos, String lookahead) {
        if (pilaSimbolos.isEmpty()) {
            return false;
        }

        String tope = pilaSimbolos.get(pilaSimbolos.size() - 1);

        if ("NEWLINE".equals(tope) && !"INDENT".equals(lookahead)) {
            return true;
        }

        if ("COMA".equals(tope) && !"ID".equals(lookahead) && !"STRINGWORD".equals(lookahead)
                && !"INTNUM".equals(lookahead) && !"FLOATNUM".equals(lookahead)
                && !"PERNUM".equals(lookahead) && !"PARENIZQ".equals(lookahead)) {
            return true;
        }

        return false;
    }

    //Valida si la pila termina con el cuerpo de una regla
    private boolean terminaCon(List<String> pila, List<String> sufijo) {
        if (pila.size() < sufijo.size()) {
            return false;
        }

        int inicio = pila.size() - sufijo.size();

        for (int i = 0; i < sufijo.size(); i++) {
            if (!pila.get(inicio + i).equals(sufijo.get(i))) {
                return false;
            }
        }

        return true;
    }

    //Crea un token virtual para representar producciones vacias
    private Token tokenVirtual(List<Token> pilaTokens) {
        if (pilaTokens.isEmpty()) {
            return new Token(Token.TipoToken.NEWLINE, "", 1, 1);
        }

        Token t = pilaTokens.get(pilaTokens.size() - 1);
        return new Token(Token.TipoToken.NEWLINE, "", t.getLinea(), t.getColumna());
    }

    //Normaliza saltos de linea para conservar solo los que son estructurales
    private List<Token> normalizarTokens(List<Token> originales) {
        List<Token> resultado = new ArrayList<>();

        for (int i = 0; i < originales.size(); i++) {
            Token actual = originales.get(i);

            if (actual.getTipo() != Token.TipoToken.NEWLINE) {
                resultado.add(actual);
                continue;
            }

            Token prev = ultimoNoNewline(resultado);
            Token next = siguienteNoNewline(originales, i + 1);

            if (prev == null || next == null) {
                continue;
            }

            boolean estructural =
                    (prev.getTipo() == Token.TipoToken.PARENDER && next.getTipo() == Token.TipoToken.INDENT)
                            || (prev.getTipo() == Token.TipoToken.ELSE && next.getTipo() == Token.TipoToken.INDENT);

            if (estructural) {
                resultado.add(actual);
            }
        }

        if (resultado.isEmpty() || resultado.get(resultado.size() - 1).getTipo() != Token.TipoToken.EOF) {
            resultado.add(new Token(Token.TipoToken.EOF, "$", 1, 1));
        }

        return resultado;
    }

    //Obtiene el ultimo token que no sea salto de linea
    private Token ultimoNoNewline(List<Token> tokens) {
        for (int i = tokens.size() - 1; i >= 0; i--) {
            if (tokens.get(i).getTipo() != Token.TipoToken.NEWLINE) {
                return tokens.get(i);
            }
        }

        return null;
    }

    //Obtiene el siguiente token que no sea salto de linea
    private Token siguienteNoNewline(List<Token> tokens, int desde) {
        for (int i = desde; i < tokens.size(); i++) {
            if (tokens.get(i).getTipo() != Token.TipoToken.NEWLINE) {
                return tokens.get(i);
            }
        }

        return null;
    }

    //Registra un error sintactico con linea y columna
    private void registrarError(Token t, String mensaje) {
        erroresSintacticos.add(String.format("line %d, col %d: ERROR Sintactico. %s",
                t.getLinea(), t.getColumna(), mensaje));
    }

    //GETTERS
    public List<String> getErrores() {
        return erroresSintacticos;
    }
}