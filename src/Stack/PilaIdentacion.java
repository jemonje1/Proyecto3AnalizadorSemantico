package Stack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class PilaIdentacion {

    //TIPOS DE ACCIONES
    public enum AccionIdentacion {
        INDENT,
        DEDENT
    }

    //ATRIBUTOS
    private final Deque<Integer> niveles;
    private final int maxNiveles;
    private final List<String> errores;
    private int ultimoNivel;

    //CONSTRUCTOR
    public PilaIdentacion(int maxNiveles) {
        this.niveles = new ArrayDeque<>();
        this.maxNiveles = maxNiveles;
        this.errores = new ArrayList<>();
        this.ultimoNivel = 0;
        this.niveles.push(0);
    }

    //METODOS
    //Procesa el nivel de indentacion detectado en una linea
    public List<AccionIdentacion> procesarNivel(int nivelActual, int numeroLinea) {
        List<AccionIdentacion> acciones = new ArrayList<>();

        ultimoNivel = nivelActual;

        int cima = niveles.peek();

        if (nivelActual > maxNiveles) {
            errores.add("line " + numeroLinea
                    + ", col 1: ERROR Se sobrepaso el limite de indentacion. Nivel detectado: "
                    + nivelActual + ", maximo permitido: " + maxNiveles);
        }

        if (nivelActual == cima) {
            return acciones;
        }

        if (nivelActual > cima) {
            if (nivelActual == cima + 1) {
                niveles.push(nivelActual);
                acciones.add(AccionIdentacion.INDENT);
                return acciones;
            }

            errores.add("line " + numeroLinea
                    + ", col 1: ERROR Salto invalido de indentacion. De nivel "
                    + cima + " a nivel " + nivelActual);

            while (!niveles.isEmpty() && niveles.peek() < nivelActual) {
                niveles.push(niveles.peek() + 1);
                acciones.add(AccionIdentacion.INDENT);
            }

            return acciones;
        }

        while (!niveles.isEmpty() && niveles.peek() > nivelActual) {
            niveles.pop();
            acciones.add(AccionIdentacion.DEDENT);
        }

        if (niveles.isEmpty() || niveles.peek() != nivelActual) {
            errores.add("line " + numeroLinea
                    + ", col 1: ERROR Cierre incorrecto de indentacion. Nivel detectado: "
                    + nivelActual);

            int nivelBase = niveles.isEmpty() ? 0 : niveles.peek();

            niveles.clear();
            niveles.push(0);

            for (int i = 1; i <= nivelActual; i++) {
                niveles.push(i);
            }

            for (int i = nivelBase + 1; i <= nivelActual; i++) {
                acciones.add(AccionIdentacion.INDENT);
            }
        }

        return acciones;
    }

    //Finaliza la pila al terminar el archivo
    public List<AccionIdentacion> finalizarArchivo() {
        List<AccionIdentacion> acciones = new ArrayList<>();

        while (!estaEnBase()) {
            desapilarNivel();
            acciones.add(AccionIdentacion.DEDENT);
        }

        ultimoNivel = 0;
        return acciones;
    }

    //Calcula el nivel usando tabs y grupos de 4 espacios
    public int calcularNivel(int espacios, int tabs) {
        return tabs + (espacios / 4);
    }

    //Retorna el nivel actual
    public int getNivelActual() {
        return niveles.peek();
    }

    //Valida si la pila contiene un nivel especifico
    public boolean contieneNivel(int nivel) {
        return niveles.contains(nivel);
    }

    //Agrega un nivel a la pila
    public void apilarNivel(int nivel) {
        niveles.push(nivel);
    }

    //Quita un nivel de la pila
    public int desapilarNivel() {
        if (niveles.size() <= 1) {
            return 0;
        }

        return niveles.pop();
    }

    //Valida si todavia se puede agregar otro nivel
    public boolean nivelValido(int nivel) {
        return (niveles.size() - 1) < maxNiveles;
    }

    //Valida si la pila volvio al nivel base
    public boolean estaEnBase() {
        return niveles.size() == 1 && niveles.peek() == 0;
    }

    //Retorna la profundidad actual sin contar el nivel base
    public int getProfundidad() {
        return niveles.size() - 1;
    }

    //Limpia la pila y reinicia los errores
    public void reset() {
        niveles.clear();
        niveles.push(0);
        errores.clear();
        ultimoNivel = 0;
    }

    //GETTERS
    public List<String> getErrores() {
        return errores;
    }

    public int getUltimoNivel() {
        return ultimoNivel;
    }
}