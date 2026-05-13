import AnalizadorLexico.AnalizadorLexico;
import AnalizadorLexico.Token;
import AnalizadorSemantico.AnalizadorSemantico;
import AnalizadorSemantico.TablaDeSimbolos;
import AnalizadorSintactico.AnalizadorSintactico;
import Archivo.ArchivoMiniLang;
import Stack.PilaIdentacion;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Inicio {

    //ATRIBUTOS
    private final Scanner scanner;
    private final ArchivoMiniLang gestorArchivo;

    //CONSTRUCTOR
    public Inicio() {
        this.scanner = new Scanner(System.in);
        this.gestorArchivo = new ArchivoMiniLang();
    }

    //METODOS
    //Inicia el menu principal del programa
    public void iniciar() {
        boolean salir = false;

        while (!salir) {
            System.out.println("\n----- Menu -----");
            System.out.println("1. Cargar Archivo");
            System.out.println("2. Salir");
            System.out.println("----------------");
            System.out.print("Seleccione una opcion: ");

            String opcion = scanner.nextLine().trim();

            switch (opcion) {
                case "1":
                    menuArchivo();
                    break;
                case "2":
                    salir = true;
                    System.out.println("Programa finalizado.");
                    break;
                default:
                    System.out.println("Opcion invalida.");
            }
        }

        scanner.close();
    }

    //Muestra el menu para analizar un archivo
    private void menuArchivo() {
        System.out.print("\nIngrese la ruta del archivo (.mlng): ");
        String rutaEntrada = scanner.nextLine().trim();

        String contenido;

        try {
            contenido = gestorArchivo.leerContenido(rutaEntrada);
            System.out.println("Archivo .mlng leido correctamente.");
        } catch (Exception e) {
            System.out.println("No se pudo leer el archivo: " + e.getMessage());
            return;
        }

        boolean volver = false;

        while (!volver) {
            System.out.println("\n------ Menu de archivo ------");
            System.out.println("1. Analizar (Generar .out y .tabla)");
            System.out.println("2. Mostrar Tokens y analisis completo");
            System.out.println("3. Volver");
            System.out.println("-----------------------------");
            System.out.print("Seleccione una opcion: ");

            String opcion = scanner.nextLine().trim();

            switch (opcion) {
                case "1":
                    ejecutarAnalisis(rutaEntrada, contenido, true, false);
                    break;
                case "2":
                    ejecutarAnalisis(rutaEntrada, contenido, false, true);
                    break;
                case "3":
                    volver = true;
                    break;
                default:
                    System.out.println("Opcion invalida.");
            }
        }
    }

    //Ejecuta el analisis lexico, indentacion, sintactico y semantico
    private void ejecutarAnalisis(String rutaEntrada, String contenido, boolean generarOut, boolean mostrarConsola) {
        try {
            AnalizadorLexico lexer = new AnalizadorLexico(contenido);
            List<Token> tokens = lexer.analizar();

            PilaIdentacion pila = lexer.getPila();
            List<String> erroresIndentacion = new ArrayList<>(pila.getErrores());

            AnalizadorSintactico sintactico = new AnalizadorSintactico();
            boolean sintaxisCorrecta = sintactico.analizar(tokens);

            AnalizadorSemantico semantico = new AnalizadorSemantico();
            boolean semanticaCorrecta = semantico.analizar(tokens);

            List<String> erroresLex = lexer.getErrores();
            List<String> erroresSin = sintactico.getParser().getErrores();
            List<String> erroresSem = semantico.getErrores();
            TablaDeSimbolos tabla = semantico.getTabla();

            boolean compilacionExitosa = erroresLex.isEmpty()
                    && erroresIndentacion.isEmpty()
                    && erroresSin.isEmpty()
                    && erroresSem.isEmpty()
                    && sintaxisCorrecta
                    && semanticaCorrecta;

            if (mostrarConsola) {
                mostrarResultadoConsola(tokens, erroresLex, erroresIndentacion, erroresSin, erroresSem,
                        tabla, compilacionExitosa);
            }

            if (generarOut) {
                generarReporte(rutaEntrada, tokens, erroresLex, erroresIndentacion, erroresSin,
                        erroresSem, compilacionExitosa);
                generarTabla(rutaEntrada, tabla);
            }

        } catch (Exception e) {
            System.out.println("ERROR CRITICO: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Muestra el resultado completo en consola
    private void mostrarResultadoConsola(List<Token> tokens, List<String> erroresLex,
                                         List<String> erroresIndentacion, List<String> erroresSin,
                                         List<String> erroresSem, TablaDeSimbolos tabla,
                                         boolean compilacionExitosa) {
        System.out.println("\n--- TOKENS ---");

        for (Token token : tokens) {
            System.out.println(token);
        }

        System.out.println("\n--- TABLA DE SIMBOLOS ---");
        System.out.println(tabla.generarTablaTexto());

        System.out.println("\n--- RESUMEN DE ERRORES ---");

        int total = erroresLex.size() + erroresIndentacion.size() + erroresSin.size() + erroresSem.size();

        System.out.println("Total de errores: " + total);

        for (String err : erroresLex) {
            System.out.println("[LEXICO] " + err);
        }

        for (String err : erroresIndentacion) {
            System.out.println("[INDENTACION] " + err);
        }

        for (String err : erroresSin) {
            System.out.println("[SINTACTICO] " + err);
        }

        for (String err : erroresSem) {
            System.out.println("[SEMANTICO] " + err);
        }

        if (total == 0) {
            System.out.println("OK");
            System.out.println("Sin errores.");
        }

        System.out.println("\nResultado: " + (compilacionExitosa ? "CADENA ACEPTADA" : "CADENA RECHAZADA"));
    }

    //Genera el archivo out con tokens y errores encontrados
    private void generarReporte(String ruta, List<Token> tokens, List<String> erroresLex,
                                List<String> erroresInd, List<String> erroresSin,
                                List<String> erroresSem, boolean exitosa) {
        StringBuilder reporte = new StringBuilder();

        reporte.append("REPORTE DE COMPILACION MINILANG\n");
        reporte.append("Archivo: ").append(ruta).append("\n\n");

        reporte.append("--- LISTADO DE TOKENS ---\n");

        for (Token t : tokens) {
            reporte.append(t.toString()).append("\n");
        }

        reporte.append("\n");
        reporte.append("--- RESUMEN DE ERRORES ---\n");

        List<String> todosLosErrores = new ArrayList<>();
        todosLosErrores.addAll(erroresLex);
        todosLosErrores.addAll(erroresInd);
        todosLosErrores.addAll(erroresSin);
        todosLosErrores.addAll(erroresSem);

        if (todosLosErrores.isEmpty()) {
            reporte.append("OK\n");
            reporte.append("No se detectaron errores durante la compilacion.\n");
        } else {
            reporte.append("Total de errores: ").append(todosLosErrores.size()).append("\n\n");

            for (String err : erroresLex) {
                reporte.append("[LEXICO] ").append(err).append("\n");
            }

            for (String err : erroresInd) {
                reporte.append("[INDENTACION] ").append(err).append("\n");
            }

            for (String err : erroresSin) {
                reporte.append("[SINTACTICO] ").append(err).append("\n");
            }

            for (String err : erroresSem) {
                reporte.append("[SEMANTICO] ").append(err).append("\n");
            }
        }

        reporte.append("\n--- RESULTADO FINAL ---\n");
        reporte.append(exitosa
                ? "COMPILACION EXITOSA - CADENA ACEPTADA\n"
                : "COMPILACION FALLIDA - CADENA RECHAZADA\n");

        try {
            Path rutaSalida = gestorArchivo.obtenerRutaSalida(ruta);
            Files.writeString(rutaSalida, reporte.toString());
            System.out.println("Reporte generado en: " + rutaSalida.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("Error al escribir el archivo .out: " + e.getMessage());
        }
    }

    //Genera el archivo tabla con la tabla de simbolos
    private void generarTabla(String ruta, TablaDeSimbolos tabla) {
        try {
            Path rutaTabla = gestorArchivo.obtenerRutaTabla(ruta);
            Files.writeString(rutaTabla, tabla.generarTablaTexto());
            System.out.println("Tabla generada en: " + rutaTabla.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("Error al escribir el archivo .tabla: " + e.getMessage());
        }
    }
}