package AnalizadorSemantico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TablaDeSimbolos {

    //CLASE INTERNA
    public static class Simbolo {

        //ATRIBUTOS
        private final int id;
        private final String nombre;
        private final String categoria;
        private final String tipo;
        private final String ambito;
        private String valorInfo;
        private final int linea;
        private final int columna;

        //CONSTRUCTOR
        public Simbolo(int id, String nombre, String categoria, String tipo, String ambito,
                       String valorInfo, int linea, int columna) {
            this.id = id;
            this.nombre = nombre;
            this.categoria = categoria;
            this.tipo = tipo;
            this.ambito = ambito;
            this.valorInfo = valorInfo;
            this.linea = linea;
            this.columna = columna;
        }

        //GETTERS
        public int getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        public String getCategoria() {
            return categoria;
        }

        public String getTipo() {
            return tipo;
        }

        public String getAmbito() {
            return ambito;
        }

        public String getValorInfo() {
            return valorInfo;
        }

        public int getLinea() {
            return linea;
        }

        public int getColumna() {
            return columna;
        }

        //SETTERS
        public void setValorInfo(String valorInfo) {
            this.valorInfo = valorInfo;
        }
    }

    //ATRIBUTOS
    private final List<Simbolo> simbolos;
    private int contadorId;

    //CONSTRUCTOR
    public TablaDeSimbolos() {
        this.simbolos = new ArrayList<>();
        this.contadorId = 1;
    }

    //METODOS
    //Agrega un simbolo nuevo a la tabla
    public Simbolo agregarSimbolo(String nombre, String categoria, String tipo, String ambito,
                                  String valorInfo, int linea, int columna) {
        Simbolo simbolo = new Simbolo(contadorId, nombre, categoria, tipo, ambito, valorInfo, linea, columna);
        simbolos.add(simbolo);
        contadorId++;
        return simbolo;
    }

    //Valida si ya existe un simbolo en el mismo ambito
    public boolean existeEnAmbito(String nombre, String ambito) {
        return buscarEnAmbito(nombre, ambito) != null;
    }

    //Busca un simbolo exactamente en un ambito
    public Simbolo buscarEnAmbito(String nombre, String ambito) {
        for (Simbolo simbolo : simbolos) {
            if (simbolo.getNombre().equals(nombre) && simbolo.getAmbito().equals(ambito)) {
                return simbolo;
            }
        }

        return null;
    }

    //Busca primero en el ambito actual y luego en global
    public Simbolo buscarVisible(String nombre, String ambitoActual) {
        Simbolo local = buscarEnAmbito(nombre, ambitoActual);

        if (local != null) {
            return local;
        }

        return buscarEnAmbito(nombre, "global");
    }

    //Busca una funcion o metodo global por nombre
    public Simbolo buscarFuncionOMetodo(String nombre) {
        for (Simbolo simbolo : simbolos) {
            boolean esFuncion = simbolo.getCategoria().equals("funcion");
            boolean esMetodo = simbolo.getCategoria().equals("metodo");

            if (simbolo.getNombre().equals(nombre) && simbolo.getAmbito().equals("global")
                    && (esFuncion || esMetodo)) {
                return simbolo;
            }
        }

        return null;
    }

    //Retorna los parametros de una funcion o metodo
    public List<Simbolo> getParametros(String ambitoFuncion) {
        List<Simbolo> parametros = new ArrayList<>();

        for (Simbolo simbolo : simbolos) {
            if (simbolo.getCategoria().equals("parametro") && simbolo.getAmbito().equals(ambitoFuncion)) {
                parametros.add(simbolo);
            }
        }

        return parametros;
    }

    //Actualiza el valor de un simbolo visible
    public void actualizarValor(String nombre, String ambitoActual, String nuevoValor) {
        Simbolo simbolo = buscarVisible(nombre, ambitoActual);

        if (simbolo != null) {
            simbolo.setValorInfo(nuevoValor);
        }
    }

    //Retorna todos los simbolos registrados
    public List<Simbolo> getSimbolos() {
        return Collections.unmodifiableList(simbolos);
    }

    //Genera el texto de la tabla para consola o archivo
    public String generarTablaTexto() {
        StringBuilder sb = new StringBuilder();

        sb.append("TABLA DE SIMBOLOS MINILANG\n\n");
        sb.append(String.format("%-5s | %-18s | %-12s | %-10s | %-15s | %-25s\n",
                "ID", "Nombre", "Categoria", "Tipo", "Ambito", "Valor/info"));
        sb.append("-------------------------------------------------------------------------------\n");

        for (Simbolo simbolo : simbolos) {
            sb.append(String.format("%-5d | %-18s | %-12s | %-10s | %-15s | %-25s\n",
                    simbolo.getId(),
                    simbolo.getNombre(),
                    simbolo.getCategoria(),
                    simbolo.getTipo(),
                    simbolo.getAmbito(),
                    simbolo.getValorInfo()));
        }

        return sb.toString();
    }
}