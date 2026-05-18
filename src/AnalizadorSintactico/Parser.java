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
    //Parsea la lista de tokens validando sentencias de MiniLang
    public boolean parsear(List<Token> tokensOriginales) {
        erroresSintacticos.clear();

        List<Token> tokens = tokensOriginales == null ? new ArrayList<>() : tokensOriginales;

        for (int i = 0; i < tokens.size(); i++) {
            Token actual = tokens.get(i);

            if (actual.getTipo() == Token.TipoToken.EOF) {
                break;
            }

            if (esIgnorable(actual)) {
                continue;
            }

            if (actual.getTipo() == Token.TipoToken.DESCONOCIDO) {
                registrarError(actual, "No se esperaba el token '" + actual.getLexema() + "'");
                continue;
            }

            if (actual.getTipo() == Token.TipoToken.CONST) {
                i = validarConstante(tokens, i);
                continue;
            }

            if (esTipoDato(actual)) {
                if (esCabeceraFuncion(tokens, i)) {
                    i = validarCabeceraFuncionOMetodo(tokens, i);
                } else {
                    i = validarDeclaracion(tokens, i);
                }

                continue;
            }

            if (actual.getTipo() == Token.TipoToken.VOID) {
                i = validarCabeceraFuncionOMetodo(tokens, i);
                continue;
            }

            if (actual.getTipo() == Token.TipoToken.IF || actual.getTipo() == Token.TipoToken.WHILE) {
                i = validarControl(tokens, i);
                continue;
            }

            if (actual.getTipo() == Token.TipoToken.ELSE) {
                i = validarElse(tokens, i);
                continue;
            }

            if (actual.getTipo() == Token.TipoToken.RETURN) {
                i = validarReturn(tokens, i);
                continue;
            }

            if (actual.getTipo() == Token.TipoToken.READ) {
                i = validarRead(tokens, i);
                continue;
            }

            if (actual.getTipo() == Token.TipoToken.WRITE) {
                i = validarWrite(tokens, i);
                continue;
            }

            if (actual.getTipo() == Token.TipoToken.ID) {
                if (siguienteEs(tokens, i, Token.TipoToken.IGUAL)) {
                    i = validarAsignacion(tokens, i);
                } else if (siguienteEs(tokens, i, Token.TipoToken.PARENIZQ)) {
                    i = validarLlamada(tokens, i);
                } else {
                    registrarError(actual, "Sentencia invalida con identificador '" + actual.getLexema() + "'");
                }

                continue;
            }

            registrarError(actual, "No se esperaba el token '" + actual.getLexema() + "'");
        }

        return erroresSintacticos.isEmpty();
    }

    //Valida una declaracion de constante
    private int validarConstante(List<Token> tokens, int i) {
        Token inicio = tokens.get(i);

        if (!existe(tokens, i + 1) || !esTipoDato(tokens.get(i + 1))) {
            registrarError(inicio, "Constante sin tipo valido");
            return avanzarHastaFinSentencia(tokens, i);
        }

        if (!existe(tokens, i + 2) || tokens.get(i + 2).getTipo() != Token.TipoToken.ID) {
            registrarError(inicio, "Constante sin nombre");
            return avanzarHastaFinSentencia(tokens, i);
        }

        int pos = i + 3;

        if (existe(tokens, pos) && tokens.get(pos).getTipo() == Token.TipoToken.IGUAL) {
            if (esFinSentencia(tokens, pos + 1)) {
                registrarError(tokens.get(pos), "Asignacion incompleta en constante");
                return avanzarHastaFinSentencia(tokens, i);
            }

            pos = avanzarHastaPyc(tokens, pos + 1);

            if (!esPyc(tokens, pos)) {
                registrarError(inicio, "Falta punto y coma en declaracion de constante");
                return pos;
            }

            return pos;
        }

        if (!esPyc(tokens, pos)) {
            registrarError(inicio, "Falta punto y coma en declaracion de constante");
            return avanzarHastaFinSentencia(tokens, i);
        }

        return pos;
    }

    //Valida una declaracion de variable
    private int validarDeclaracion(List<Token> tokens, int i) {
        Token inicio = tokens.get(i);

        if (!existe(tokens, i + 1) || tokens.get(i + 1).getTipo() != Token.TipoToken.ID) {
            registrarError(inicio, "Declaracion de variable sin nombre");
            return avanzarHastaFinSentencia(tokens, i);
        }

        int pos = i + 2;

        if (existe(tokens, pos) && tokens.get(pos).getTipo() == Token.TipoToken.IGUAL) {
            if (esFinSentencia(tokens, pos + 1)) {
                registrarError(tokens.get(pos), "Asignacion incompleta en declaracion");
                return avanzarHastaFinSentencia(tokens, i);
            }

            pos = avanzarHastaPyc(tokens, pos + 1);

            if (!esPyc(tokens, pos)) {
                registrarError(inicio, "Falta punto y coma en declaracion");
                return pos;
            }

            return pos;
        }

        if (!esPyc(tokens, pos)) {
            registrarError(inicio, "Falta punto y coma en declaracion");
            return avanzarHastaFinSentencia(tokens, i);
        }

        return pos;
    }

    //Valida una asignacion
    private int validarAsignacion(List<Token> tokens, int i) {
        Token inicio = tokens.get(i);

        if (!existe(tokens, i + 1) || tokens.get(i + 1).getTipo() != Token.TipoToken.IGUAL) {
            registrarError(inicio, "Asignacion invalida");
            return avanzarHastaFinSentencia(tokens, i);
        }

        if (esFinSentencia(tokens, i + 2)) {
            registrarError(tokens.get(i + 1), "Asignacion incompleta");
            return avanzarHastaFinSentencia(tokens, i);
        }

        int pos = avanzarHastaPyc(tokens, i + 2);

        if (!esPyc(tokens, pos)) {
            registrarError(inicio, "Falta punto y coma en asignacion");
            return pos;
        }

        return pos;
    }

    //Valida una cabecera de funcion o metodo
    private int validarCabeceraFuncionOMetodo(List<Token> tokens, int i) {
        Token inicio = tokens.get(i);

        if (!existe(tokens, i + 1) || tokens.get(i + 1).getTipo() != Token.TipoToken.ID) {
            registrarError(inicio, "Funcion o metodo sin nombre");
            return avanzarHastaFinSentencia(tokens, i);
        }

        if (!existe(tokens, i + 2) || tokens.get(i + 2).getTipo() != Token.TipoToken.PARENIZQ) {
            registrarError(inicio, "Funcion o metodo sin parentesis de apertura");
            return avanzarHastaFinSentencia(tokens, i);
        }

        int cierre = buscarParentesisDerecho(tokens, i + 3);

        if (cierre == -1) {
            registrarError(inicio, "Funcion o metodo sin parentesis de cierre");
            return avanzarHastaFinSentencia(tokens, i);
        }

        validarParametros(tokens, i + 3, cierre);

        if (!tieneBloqueIndentado(tokens, cierre + 1)) {
            registrarError(inicio, "Funcion o metodo sin bloque indentado");
        }

        return cierre;
    }

    //Valida una sentencia if o while
    private int validarControl(List<Token> tokens, int i) {
        Token inicio = tokens.get(i);

        if (!existe(tokens, i + 1) || tokens.get(i + 1).getTipo() != Token.TipoToken.PARENIZQ) {
            registrarError(inicio, "Estructura de control sin parentesis de apertura");
            return avanzarHastaFinSentencia(tokens, i);
        }

        int cierre = buscarParentesisDerecho(tokens, i + 2);

        if (cierre == -1) {
            registrarError(inicio, "Estructura de control sin parentesis de cierre");
            return avanzarHastaFinSentencia(tokens, i);
        }

        if (cierre == i + 2) {
            registrarError(inicio, "Condicion vacia en estructura de control");
        }

        if (!tieneBloqueIndentado(tokens, cierre + 1)) {
            registrarError(inicio, "Estructura de control sin bloque indentado");
        }

        return cierre;
    }

    //Valida una sentencia else
    private int validarElse(List<Token> tokens, int i) {
        Token inicio = tokens.get(i);

        if (!tieneBloqueIndentado(tokens, i + 1)) {
            registrarError(inicio, "Else sin bloque indentado");
        }

        return i;
    }

    //Valida una sentencia return
    private int validarReturn(List<Token> tokens, int i) {
        int pos = avanzarHastaPyc(tokens, i + 1);

        if (esPyc(tokens, pos)) {
            return pos;
        }

        if (esFinSentencia(tokens, pos)) {
            return pos;
        }

        registrarError(tokens.get(i), "Return invalido");
        return pos;
    }

    //Valida una sentencia read
    private int validarRead(List<Token> tokens, int i) {
        Token inicio = tokens.get(i);

        if (!existe(tokens, i + 1) || tokens.get(i + 1).getTipo() != Token.TipoToken.PARENIZQ) {
            registrarError(inicio, "Read sin parentesis de apertura");
            return avanzarHastaFinSentencia(tokens, i);
        }

        int cierre = buscarParentesisDerecho(tokens, i + 2);

        if (cierre == -1) {
            registrarError(inicio, "Read sin parentesis de cierre");
            return avanzarHastaFinSentencia(tokens, i);
        }

        if (cierre == i + 2 || tokens.get(i + 2).getTipo() != Token.TipoToken.ID) {
            registrarError(inicio, "Read espera un identificador");
        }

        int pos = cierre + 1;

        if (!esPyc(tokens, pos)) {
            registrarError(inicio, "Falta punto y coma en read");
            return avanzarHastaFinSentencia(tokens, i);
        }

        return pos;
    }

    //Valida una sentencia write
    private int validarWrite(List<Token> tokens, int i) {
        Token inicio = tokens.get(i);

        if (!existe(tokens, i + 1) || tokens.get(i + 1).getTipo() != Token.TipoToken.PARENIZQ) {
            registrarError(inicio, "Write sin parentesis de apertura");
            return avanzarHastaFinSentencia(tokens, i);
        }

        int cierre = buscarParentesisDerecho(tokens, i + 2);

        if (cierre == -1) {
            registrarError(inicio, "Write sin parentesis de cierre");
            return avanzarHastaFinSentencia(tokens, i);
        }

        if (cierre == i + 2) {
            registrarError(inicio, "Write sin argumentos");
        }

        int pos = cierre + 1;

        if (!esPyc(tokens, pos)) {
            registrarError(inicio, "Falta punto y coma en write");
            return avanzarHastaFinSentencia(tokens, i);
        }

        return pos;
    }

    //Valida una llamada de funcion o metodo como sentencia
    private int validarLlamada(List<Token> tokens, int i) {
        Token inicio = tokens.get(i);

        if (!existe(tokens, i + 1) || tokens.get(i + 1).getTipo() != Token.TipoToken.PARENIZQ) {
            registrarError(inicio, "Llamada sin parentesis de apertura");
            return avanzarHastaFinSentencia(tokens, i);
        }

        int cierre = buscarParentesisDerecho(tokens, i + 2);

        if (cierre == -1) {
            registrarError(inicio, "Llamada sin parentesis de cierre");
            return avanzarHastaFinSentencia(tokens, i);
        }

        int pos = cierre + 1;

        if (!esPyc(tokens, pos)) {
            registrarError(inicio, "Falta punto y coma en llamada");
            return avanzarHastaFinSentencia(tokens, i);
        }

        return pos;
    }

    //Valida parametros separados por coma
    private void validarParametros(List<Token> tokens, int inicio, int fin) {
        if (inicio >= fin) {
            return;
        }

        boolean esperandoTipo = true;
        boolean esperandoNombre = false;
        boolean esperandoComa = false;

        for (int i = inicio; i < fin; i++) {
            Token actual = tokens.get(i);

            if (esperandoTipo) {
                if (!esTipoDato(actual)) {
                    registrarError(actual, "Parametro sin tipo valido");
                    return;
                }

                esperandoTipo = false;
                esperandoNombre = true;
                continue;
            }

            if (esperandoNombre) {
                if (actual.getTipo() != Token.TipoToken.ID) {
                    registrarError(actual, "Parametro sin nombre");
                    return;
                }

                esperandoNombre = false;
                esperandoComa = true;
                continue;
            }

            if (esperandoComa) {
                if (actual.getTipo() != Token.TipoToken.COMA) {
                    registrarError(actual, "Se esperaba coma entre parametros");
                    return;
                }

                esperandoComa = false;
                esperandoTipo = true;
            }
        }

        if (esperandoTipo && inicio < fin) {
            registrarError(tokens.get(fin - 1), "Lista de parametros termina con coma");
        }

        if (esperandoNombre) {
            registrarError(tokens.get(fin - 1), "Parametro incompleto");
        }
    }

    //Busca si despues existe salto de linea e indentacion
    private boolean tieneBloqueIndentado(List<Token> tokens, int desde) {
        boolean vioNewline = false;

        for (int i = desde; i < tokens.size(); i++) {
            Token actual = tokens.get(i);

            if (actual.getTipo() == Token.TipoToken.NEWLINE) {
                vioNewline = true;
                continue;
            }

            if (vioNewline && actual.getTipo() == Token.TipoToken.INDENT) {
                return true;
            }

            if (actual.getTipo() == Token.TipoToken.EOF) {
                return false;
            }

            if (!vioNewline) {
                return false;
            }

            if (actual.getTipo() != Token.TipoToken.NEWLINE) {
                return false;
            }
        }

        return false;
    }

    //Avanza hasta encontrar punto y coma o fin de sentencia
    private int avanzarHastaPyc(List<Token> tokens, int desde) {
        for (int i = desde; i < tokens.size(); i++) {
            Token actual = tokens.get(i);

            if (actual.getTipo() == Token.TipoToken.PYC
                    || actual.getTipo() == Token.TipoToken.NEWLINE
                    || actual.getTipo() == Token.TipoToken.DEDENT
                    || actual.getTipo() == Token.TipoToken.EOF) {
                return i;
            }
        }

        return tokens.size() - 1;
    }

    //Avanza hasta un punto seguro de recuperacion
    private int avanzarHastaFinSentencia(List<Token> tokens, int desde) {
        for (int i = desde; i < tokens.size(); i++) {
            Token actual = tokens.get(i);

            if (actual.getTipo() == Token.TipoToken.PYC
                    || actual.getTipo() == Token.TipoToken.NEWLINE
                    || actual.getTipo() == Token.TipoToken.DEDENT
                    || actual.getTipo() == Token.TipoToken.EOF) {
                return i;
            }
        }

        return tokens.size() - 1;
    }

    //Busca el parentesis derecho correspondiente
    private int buscarParentesisDerecho(List<Token> tokens, int desde) {
        int abiertos = 0;

        for (int i = desde; i < tokens.size(); i++) {
            Token actual = tokens.get(i);

            if (actual.getTipo() == Token.TipoToken.PARENIZQ) {
                abiertos++;
            }

            if (actual.getTipo() == Token.TipoToken.PARENDER) {
                if (abiertos == 0) {
                    return i;
                }

                abiertos--;
            }

            if (actual.getTipo() == Token.TipoToken.NEWLINE
                    || actual.getTipo() == Token.TipoToken.DEDENT
                    || actual.getTipo() == Token.TipoToken.EOF) {
                return -1;
            }
        }

        return -1;
    }

    //Valida si existe una posicion en la lista
    private boolean existe(List<Token> tokens, int i) {
        return i >= 0 && i < tokens.size();
    }

    //Valida si una posicion tiene punto y coma
    private boolean esPyc(List<Token> tokens, int i) {
        return existe(tokens, i) && tokens.get(i).getTipo() == Token.TipoToken.PYC;
    }

    //Valida si una posicion es fin de sentencia
    private boolean esFinSentencia(List<Token> tokens, int i) {
        if (!existe(tokens, i)) {
            return true;
        }

        Token.TipoToken tipo = tokens.get(i).getTipo();

        return tipo == Token.TipoToken.PYC
                || tipo == Token.TipoToken.NEWLINE
                || tipo == Token.TipoToken.DEDENT
                || tipo == Token.TipoToken.EOF;
    }

    //Valida si se puede ignorar el token en el recorrido principal
    private boolean esIgnorable(Token token) {
        return token.getTipo() == Token.TipoToken.NEWLINE
                || token.getTipo() == Token.TipoToken.INDENT
                || token.getTipo() == Token.TipoToken.DEDENT;
    }

    //Valida si el siguiente token tiene un tipo especifico
    private boolean siguienteEs(List<Token> tokens, int i, Token.TipoToken tipo) {
        return existe(tokens, i + 1) && tokens.get(i + 1).getTipo() == tipo;
    }

    //Valida si un token es cabecera de funcion
    private boolean esCabeceraFuncion(List<Token> tokens, int i) {
        return existe(tokens, i + 2)
                && tokens.get(i + 1).getTipo() == Token.TipoToken.ID
                && tokens.get(i + 2).getTipo() == Token.TipoToken.PARENIZQ;
    }

    //Valida si el token es tipo de dato
    private boolean esTipoDato(Token token) {
        return token.getTipo() == Token.TipoToken.INT
                || token.getTipo() == Token.TipoToken.FLOAT
                || token.getTipo() == Token.TipoToken.STRING
                || token.getTipo() == Token.TipoToken.BOOL;
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