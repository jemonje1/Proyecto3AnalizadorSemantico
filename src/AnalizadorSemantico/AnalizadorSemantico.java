package AnalizadorSemantico;

import AnalizadorLexico.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class AnalizadorSemantico {

    //ATRIBUTOS
    private final TablaDeSimbolos tabla;
    private final List<String> errores;
    private final Stack<String> pilaAmbitos;
    private final Stack<Integer> nivelesFuncion;
    private int nivelIndentacion;
    private String funcionPendiente;
    private String funcionActual;
    private int contadorMain;

    //CONSTRUCTOR
    public AnalizadorSemantico() {
        this.tabla = new TablaDeSimbolos();
        this.errores = new ArrayList<>();
        this.pilaAmbitos = new Stack<>();
        this.nivelesFuncion = new Stack<>();
        this.nivelIndentacion = 0;
        this.funcionPendiente = null;
        this.funcionActual = null;
        this.contadorMain = 0;
        this.pilaAmbitos.push("global");
    }

    //METODOS
    //Ejecuta el analisis semantico completo
    public boolean analizar(List<Token> tokens) {
        errores.clear();

        for (int i = 0; i < tokens.size(); i++) {
            Token actual = tokens.get(i);

            if (actual.getTipo() == Token.TipoToken.INDENT) {
                nivelIndentacion++;

                if (funcionPendiente != null) {
                    pilaAmbitos.push(funcionPendiente);
                    nivelesFuncion.push(nivelIndentacion);
                    funcionActual = funcionPendiente;
                    funcionPendiente = null;
                }

                continue;
            }

            if (actual.getTipo() == Token.TipoToken.DEDENT) {
                if (!nivelesFuncion.isEmpty() && nivelesFuncion.peek() == nivelIndentacion) {
                    nivelesFuncion.pop();

                    if (pilaAmbitos.size() > 1) {
                        pilaAmbitos.pop();
                    }

                    funcionActual = pilaAmbitos.peek();
                }

                if (nivelIndentacion > 0) {
                    nivelIndentacion--;
                }

                continue;
            }

            if (actual.getTipo() == Token.TipoToken.CONST) {
                i = procesarConstante(tokens, i);
                continue;
            }

            if (esTipoDato(actual) || actual.getTipo() == Token.TipoToken.VOID) {
                if (esCabeceraFuncion(tokens, i)) {
                    i = procesarFuncionOMetodo(tokens, i);
                } else {
                    i = procesarVariable(tokens, i);
                }

                continue;
            }

            if (actual.getTipo() == Token.TipoToken.ID) {
                if (esAsignacion(tokens, i)) {
                    i = procesarAsignacion(tokens, i);
                } else if (esLlamadaFuncion(tokens, i)) {
                    i = procesarLlamadaFuncion(tokens, i);
                }
            }

            if (actual.getTipo() == Token.TipoToken.RETURN) {
                i = procesarReturn(tokens, i);
            }
        }

        validarNoInicializados();

        if (contadorMain == 0) {
            errores.add("line 1, col 1: ERROR Semantico. No existe metodo main");
        }

        return errores.isEmpty();
    }

    //Procesa una constante
    private int procesarConstante(List<Token> tokens, int i) {
        Token constToken = tokens.get(i);

        if (i + 2 >= tokens.size() || !esTipoDato(tokens.get(i + 1)) || tokens.get(i + 2).getTipo() != Token.TipoToken.ID) {
            registrarError(constToken, "Declaracion de constante invalida");
            return avanzarHastaPyc(tokens, i);
        }

        Token tipoToken = tokens.get(i + 1);
        Token nombreToken = tokens.get(i + 2);

        String tipo = tipoComoTexto(tipoToken);
        String nombre = nombreToken.getLexema();
        String ambito = ambitoActual();

        if (tabla.existeEnAmbito(nombre, ambito)) {
            registrarError(nombreToken, "El simbolo '" + nombre + "' ya existe en el ambito " + ambito);
        } else {
            String valor = "no inicializado";

            if (i + 3 < tokens.size() && tokens.get(i + 3).getTipo() == Token.TipoToken.IGUAL) {
                int fin = buscarPyc(tokens, i + 4);
                valor = expresionComoTexto(tokens, i + 4, fin);

                String tipoExpr = inferirTipoExpresion(tokens, i + 4, fin, ambito, nombreToken);

                if (!tiposCompatibles(tipo, tipoExpr)) {
                    registrarError(nombreToken, "No se puede asignar tipo '" + tipoExpr + "' a constante de tipo '" + tipo + "'");
                }

                tabla.agregarSimbolo(nombre, "constante", tipo, ambito, valor, nombreToken.getLinea(), nombreToken.getColumna());
                return fin;
            }

            tabla.agregarSimbolo(nombre, "constante", tipo, ambito, valor, nombreToken.getLinea(), nombreToken.getColumna());
        }

        return avanzarHastaPyc(tokens, i);
    }

    //Procesa una variable normal
    private int procesarVariable(List<Token> tokens, int i) {
        Token tipoToken = tokens.get(i);

        if (i + 1 >= tokens.size() || tokens.get(i + 1).getTipo() != Token.TipoToken.ID) {
            registrarError(tipoToken, "Declaracion de variable invalida");
            return avanzarHastaPyc(tokens, i);
        }

        Token nombreToken = tokens.get(i + 1);
        String tipo = tipoComoTexto(tipoToken);
        String nombre = nombreToken.getLexema();
        String ambito = ambitoActual();

        if (tabla.existeEnAmbito(nombre, ambito)) {
            registrarError(nombreToken, "El simbolo '" + nombre + "' ya existe en el ambito " + ambito);
            return avanzarHastaPyc(tokens, i);
        }

        String valor = "no inicializado";

        if (i + 2 < tokens.size() && tokens.get(i + 2).getTipo() == Token.TipoToken.IGUAL) {
            int fin = buscarPyc(tokens, i + 3);
            valor = expresionComoTexto(tokens, i + 3, fin);

            String tipoExpr = inferirTipoExpresion(tokens, i + 3, fin, ambito, nombreToken);

            if (!tiposCompatibles(tipo, tipoExpr)) {
                registrarError(nombreToken, "No se puede asignar tipo '" + tipoExpr + "' a variable de tipo '" + tipo + "'");
            }

            tabla.agregarSimbolo(nombre, "variable", tipo, ambito, valor, nombreToken.getLinea(), nombreToken.getColumna());
            return fin;
        }

        tabla.agregarSimbolo(nombre, "variable", tipo, ambito, valor, nombreToken.getLinea(), nombreToken.getColumna());
        return avanzarHastaPyc(tokens, i);
    }

    //Procesa una funcion o metodo
    private int procesarFuncionOMetodo(List<Token> tokens, int i) {
        Token tipoToken = tokens.get(i);
        Token nombreToken = tokens.get(i + 1);

        String tipo = tipoComoTexto(tipoToken);
        String nombre = nombreToken.getLexema();
        String categoria = tipoToken.getTipo() == Token.TipoToken.VOID ? "metodo" : "funcion";

        if (tabla.existeEnAmbito(nombre, "global")) {
            registrarError(nombreToken, "La funcion o metodo '" + nombre + "' ya existe");
        }

        if (categoria.equals("metodo") && nombre.equals("main")) {
            contadorMain++;

            if (contadorMain > 1) {
                registrarError(nombreToken, "Solo puede existir un metodo main");
            }
        }

        int inicioParams = i + 3;
        int finParams = buscarParentesisDerecho(tokens, inicioParams);

        String infoParametros = obtenerInfoParametros(tokens, inicioParams, finParams);

        tabla.agregarSimbolo(nombre, categoria, tipo, "global", infoParametros,
                nombreToken.getLinea(), nombreToken.getColumna());

        registrarParametros(tokens, inicioParams, finParams, nombre);

        funcionPendiente = nombre;
        return finParams;
    }

    //Procesa una asignacion
    private int procesarAsignacion(List<Token> tokens, int i) {
        Token nombreToken = tokens.get(i);
        String nombre = nombreToken.getLexema();
        String ambito = ambitoActual();

        TablaDeSimbolos.Simbolo simbolo = tabla.buscarVisible(nombre, ambito);

        if (simbolo == null) {
            registrarError(nombreToken, "La variable '" + nombre + "' no ha sido declarada");
            return avanzarHastaPyc(tokens, i);
        }

        int fin = buscarPyc(tokens, i + 2);
        String valor = expresionComoTexto(tokens, i + 2, fin);
        String tipoExpr = inferirTipoExpresion(tokens, i + 2, fin, ambito, nombreToken);

        if (simbolo.getCategoria().equals("constante") && !simbolo.getValorInfo().equals("no inicializado")) {
            registrarError(nombreToken, "No se puede cambiar el valor de la constante '" + nombre + "'");
        } else if (!tiposCompatibles(simbolo.getTipo(), tipoExpr)) {
            registrarError(nombreToken, "No se puede asignar tipo '" + tipoExpr + "' a simbolo de tipo '" + simbolo.getTipo() + "'");
        } else {
            tabla.actualizarValor(nombre, ambito, valor);
        }

        return fin;
    }

    //Procesa una llamada de funcion o metodo
    private int procesarLlamadaFuncion(List<Token> tokens, int i) {
        Token nombreToken = tokens.get(i);
        String nombre = nombreToken.getLexema();
        String ambito = ambitoActual();

        TablaDeSimbolos.Simbolo funcion = tabla.buscarFuncionOMetodo(nombre);

        int inicioArgs = i + 2;
        int finArgs = buscarParentesisDerecho(tokens, inicioArgs);

        if (funcion == null) {
            registrarError(nombreToken, "La funcion o metodo '" + nombre + "' no ha sido declarado");
            return finArgs;
        }

        List<String> argumentos = obtenerTiposArgumentos(tokens, inicioArgs, finArgs, ambito, nombreToken);
        List<TablaDeSimbolos.Simbolo> parametros = tabla.getParametros(nombre);

        if (argumentos.size() != parametros.size()) {
            registrarError(nombreToken, "Cantidad incorrecta de argumentos para '" + nombre + "'");
            return finArgs;
        }

        for (int j = 0; j < argumentos.size(); j++) {
            String tipoArg = argumentos.get(j);
            String tipoParam = parametros.get(j).getTipo();

            if (!tiposCompatibles(tipoParam, tipoArg)) {
                registrarError(nombreToken, "Argumento " + (j + 1) + " invalido para '" + nombre
                        + "'. Se esperaba '" + tipoParam + "' y se recibio '" + tipoArg + "'");
            }
        }

        return finArgs;
    }

    //Procesa un return
    private int procesarReturn(List<Token> tokens, int i) {
        Token returnToken = tokens.get(i);

        if (funcionActual == null || funcionActual.equals("global")) {
            registrarError(returnToken, "Return fuera de una funcion o metodo");
            return avanzarHastaPyc(tokens, i);
        }

        TablaDeSimbolos.Simbolo funcion = tabla.buscarFuncionOMetodo(funcionActual);

        if (funcion == null) {
            return avanzarHastaPyc(tokens, i);
        }

        int fin = buscarPyc(tokens, i + 1);

        if (funcion.getCategoria().equals("metodo")) {
            if (i + 1 < fin) {
                registrarError(returnToken, "Un metodo void no debe retornar valor");
            }

            return fin;
        }

        if (i + 1 >= fin) {
            registrarError(returnToken, "La funcion '" + funcionActual + "' debe retornar un valor");
            return fin;
        }

        String tipoRetorno = inferirTipoExpresion(tokens, i + 1, fin, ambitoActual(), returnToken);

        if (!tiposCompatibles(funcion.getTipo(), tipoRetorno)) {
            registrarError(returnToken, "La funcion '" + funcionActual + "' retorna tipo '" + tipoRetorno
                    + "' pero se esperaba '" + funcion.getTipo() + "'");
        }

        return fin;
    }

    //Registra los parametros de una funcion o metodo
    private void registrarParametros(List<Token> tokens, int inicio, int fin, String ambitoFuncion) {
        int i = inicio;

        while (i < fin) {
            if (esTipoDato(tokens.get(i)) && i + 1 < fin && tokens.get(i + 1).getTipo() == Token.TipoToken.ID) {
                Token tipoToken = tokens.get(i);
                Token nombreToken = tokens.get(i + 1);

                String tipo = tipoComoTexto(tipoToken);
                String nombre = nombreToken.getLexema();

                if (tabla.existeEnAmbito(nombre, ambitoFuncion)) {
                    registrarError(nombreToken, "El parametro '" + nombre + "' ya existe en el ambito " + ambitoFuncion);
                } else {
                    tabla.agregarSimbolo(nombre, "parametro", tipo, ambitoFuncion, "parametro",
                            nombreToken.getLinea(), nombreToken.getColumna());
                }

                i += 2;
            } else {
                i++;
            }
        }
    }

    //Valida simbolos que quedaron sin inicializar
    private void validarNoInicializados() {
        for (TablaDeSimbolos.Simbolo simbolo : tabla.getSimbolos()) {
            boolean revisable = simbolo.getCategoria().equals("variable") || simbolo.getCategoria().equals("constante");

            if (revisable && simbolo.getValorInfo().equals("no inicializado")) {
                errores.add("line " + simbolo.getLinea() + ", col " + simbolo.getColumna()
                        + ": ERROR Semantico. El simbolo '" + simbolo.getNombre()
                        + "' no fue inicializado");
            }
        }
    }

    //Infiere el tipo de una expresion simple
    private String inferirTipoExpresion(List<Token> tokens, int inicio, int fin, String ambito, Token referencia) {
        boolean tieneString = false;
        boolean tieneBool = false;
        boolean tieneFloat = false;
        boolean tieneComparacion = false;
        boolean tieneAritmetica = false;
        boolean tieneValor = false;

        for (int i = inicio; i < fin && i < tokens.size(); i++) {
            Token token = tokens.get(i);

            if (esOperadorComparacion(token)) {
                tieneComparacion = true;
                continue;
            }

            if (esOperadorAritmetico(token)) {
                tieneAritmetica = true;
                continue;
            }

            if (token.getTipo() == Token.TipoToken.STRINGWORD) {
                tieneString = true;
                tieneValor = true;
                continue;
            }

            if (token.getTipo() == Token.TipoToken.TRUE || token.getTipo() == Token.TipoToken.FALSE) {
                tieneBool = true;
                tieneValor = true;
                continue;
            }

            if (token.getTipo() == Token.TipoToken.FLOATNUM || token.getTipo() == Token.TipoToken.PERNUM) {
                tieneFloat = true;
                tieneValor = true;
                continue;
            }

            if (token.getTipo() == Token.TipoToken.INTNUM) {
                tieneValor = true;
                continue;
            }

            if (token.getTipo() == Token.TipoToken.ID) {
                TablaDeSimbolos.Simbolo simbolo = tabla.buscarVisible(token.getLexema(), ambito);

                if (simbolo == null) {
                    registrarError(token, "La variable '" + token.getLexema() + "' no ha sido declarada");
                    continue;
                }

                tieneValor = true;

                if (simbolo.getTipo().equals("string")) {
                    tieneString = true;
                } else if (simbolo.getTipo().equals("bool")) {
                    tieneBool = true;
                } else if (simbolo.getTipo().equals("float")) {
                    tieneFloat = true;
                }
            }
        }

        if (!tieneValor) {
            return "desconocido";
        }

        if (tieneComparacion) {
            if (tieneString && tieneAritmetica) {
                registrarError(referencia, "No se puede operar string en expresiones aritmeticas");
            }

            if (tieneBool && tieneAritmetica) {
                registrarError(referencia, "No se puede operar bool en expresiones aritmeticas");
            }

            return "bool";
        }

        if (tieneString && tieneAritmetica) {
            registrarError(referencia, "No se puede operar tipo string con operadores aritmeticos");
            return "error";
        }

        if (tieneBool && tieneAritmetica) {
            registrarError(referencia, "No se puede operar tipo bool con operadores aritmeticos");
            return "error";
        }

        if (tieneString) {
            return "string";
        }

        if (tieneBool) {
            return "bool";
        }

        if (tieneFloat) {
            return "float";
        }

        return "int";
    }

    //Obtiene los tipos de los argumentos de una llamada
    private List<String> obtenerTiposArgumentos(List<Token> tokens, int inicio, int fin, String ambito, Token referencia) {
        List<String> tipos = new ArrayList<>();
        int inicioArg = inicio;

        for (int i = inicio; i <= fin; i++) {
            boolean corte = i == fin || tokens.get(i).getTipo() == Token.TipoToken.COMA;

            if (corte) {
                if (inicioArg < i) {
                    tipos.add(inferirTipoExpresion(tokens, inicioArg, i, ambito, referencia));
                }

                inicioArg = i + 1;
            }
        }

        return tipos;
    }

    //Obtiene informacion textual de parametros
    private String obtenerInfoParametros(List<Token> tokens, int inicio, int fin) {
        List<String> nombres = new ArrayList<>();

        for (int i = inicio; i < fin; i++) {
            if (tokens.get(i).getTipo() == Token.TipoToken.ID) {
                nombres.add(tokens.get(i).getLexema());
            }
        }

        if (nombres.isEmpty()) {
            return "sin parametros";
        }

        return "parametros " + String.join(", ", nombres);
    }

    //Convierte una expresion a texto
    private String expresionComoTexto(List<Token> tokens, int inicio, int fin) {
        StringBuilder sb = new StringBuilder();

        for (int i = inicio; i < fin && i < tokens.size(); i++) {
            if (sb.length() > 0) {
                sb.append(" ");
            }

            sb.append(tokens.get(i).getLexema());
        }

        if (sb.length() == 0) {
            return "no inicializado";
        }

        return sb.toString();
    }

    //Valida si un token es cabecera de funcion o metodo
    private boolean esCabeceraFuncion(List<Token> tokens, int i) {
        return i + 2 < tokens.size()
                && tokens.get(i + 1).getTipo() == Token.TipoToken.ID
                && tokens.get(i + 2).getTipo() == Token.TipoToken.PARENIZQ;
    }

    //Valida si un token inicia asignacion
    private boolean esAsignacion(List<Token> tokens, int i) {
        return i + 1 < tokens.size() && tokens.get(i + 1).getTipo() == Token.TipoToken.IGUAL;
    }

    //Valida si un token inicia llamada de funcion
    private boolean esLlamadaFuncion(List<Token> tokens, int i) {
        return i + 1 < tokens.size() && tokens.get(i + 1).getTipo() == Token.TipoToken.PARENIZQ;
    }

    //Valida si el token es un tipo de dato
    private boolean esTipoDato(Token token) {
        return token.getTipo() == Token.TipoToken.INT
                || token.getTipo() == Token.TipoToken.FLOAT
                || token.getTipo() == Token.TipoToken.STRING
                || token.getTipo() == Token.TipoToken.BOOL;
    }

    //Convierte token de tipo a texto
    private String tipoComoTexto(Token token) {
        if (token.getTipo() == Token.TipoToken.INT) {
            return "int";
        }

        if (token.getTipo() == Token.TipoToken.FLOAT) {
            return "float";
        }

        if (token.getTipo() == Token.TipoToken.STRING) {
            return "string";
        }

        if (token.getTipo() == Token.TipoToken.BOOL) {
            return "bool";
        }

        if (token.getTipo() == Token.TipoToken.VOID) {
            return "void";
        }

        return "desconocido";
    }

    //Valida compatibilidad de tipos
    private boolean tiposCompatibles(String destino, String origen) {
        if (origen.equals("desconocido")) {
            return true;
        }

        if (origen.equals("error")) {
            return false;
        }

        if (destino.equals(origen)) {
            return true;
        }

        return destino.equals("float") && origen.equals("int");
    }

    //Valida si es operador aritmetico
    private boolean esOperadorAritmetico(Token token) {
        return token.getTipo() == Token.TipoToken.SUM
                || token.getTipo() == Token.TipoToken.REST
                || token.getTipo() == Token.TipoToken.MULT
                || token.getTipo() == Token.TipoToken.DIV;
    }

    //Valida si es operador de comparacion
    private boolean esOperadorComparacion(Token token) {
        return token.getTipo() == Token.TipoToken.ESIGUAL
                || token.getTipo() == Token.TipoToken.NOIGUAL
                || token.getTipo() == Token.TipoToken.MEIGUAL
                || token.getTipo() == Token.TipoToken.MAIGUAL
                || token.getTipo() == Token.TipoToken.MENOR
                || token.getTipo() == Token.TipoToken.MAYOR;
    }

    //Busca el punto y coma mas cercano
    private int buscarPyc(List<Token> tokens, int desde) {
        for (int i = desde; i < tokens.size(); i++) {
            if (tokens.get(i).getTipo() == Token.TipoToken.PYC) {
                return i;
            }

            if (tokens.get(i).getTipo() == Token.TipoToken.NEWLINE
                    || tokens.get(i).getTipo() == Token.TipoToken.DEDENT
                    || tokens.get(i).getTipo() == Token.TipoToken.EOF) {
                return i;
            }
        }

        return tokens.size() - 1;
    }

    //Avanza hasta punto y coma
    private int avanzarHastaPyc(List<Token> tokens, int desde) {
        return buscarPyc(tokens, desde);
    }

    //Busca parentesis derecho
    private int buscarParentesisDerecho(List<Token> tokens, int desde) {
        for (int i = desde; i < tokens.size(); i++) {
            if (tokens.get(i).getTipo() == Token.TipoToken.PARENDER) {
                return i;
            }
        }

        return desde;
    }

    //Retorna el ambito actual
    private String ambitoActual() {
        if (pilaAmbitos.isEmpty()) {
            return "global";
        }

        return pilaAmbitos.peek();
    }

    //Registra un error semantico
    private void registrarError(Token token, String mensaje) {
        errores.add(String.format("line %d, col %d: ERROR Semantico. %s",
                token.getLinea(), token.getColumna(), mensaje));
    }

    //GETTERS
    public TablaDeSimbolos getTabla() {
        return tabla;
    }

    public List<String> getErrores() {
        return errores;
    }
}