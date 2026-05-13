package Archivo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ArchivoMiniLang {

    //ATRIBUTOS
    private final String extensionPermitida;

    //CONSTRUCTOR
    public ArchivoMiniLang() {
        this.extensionPermitida = ".mlng";
    }

    //METODOS
    //Valida que la ruta exista y que el archivo tenga extension permitida
    public Path validarArchivo(String rutaArchivo) throws IOException {
        Path ruta = Paths.get(rutaArchivo);

        if (!Files.exists(ruta)) {
            throw new IOException("Error: El archivo no existe en la ruta especificada: " + rutaArchivo);
        }

        if (!esExtensionValida(rutaArchivo)) {
            throw new IOException("Error: Extension invalida. El archivo debe ser de tipo " + extensionPermitida);
        }

        return ruta;
    }

    //Verifica si el nombre del archivo termina con la extension permitida
    public boolean esExtensionValida(String rutaArchivo) {
        return rutaArchivo != null && rutaArchivo.toLowerCase().endsWith(extensionPermitida);
    }

    //Genera la ruta para el archivo de salida out
    public Path obtenerRutaSalida(String rutaEntrada) {
        return obtenerRutaConExtension(rutaEntrada, ".out");
    }

    //Genera la ruta para el archivo de tabla
    public Path obtenerRutaTabla(String rutaEntrada) {
        return obtenerRutaConExtension(rutaEntrada, ".tabla");
    }

    //Genera una ruta de salida con la extension indicada
    private Path obtenerRutaConExtension(String rutaEntrada, String extension) {
        Path entrada = Paths.get(rutaEntrada);
        String nombreArchivo = entrada.getFileName().toString();

        int indicePunto = nombreArchivo.lastIndexOf('.');
        String nombreBase = (indicePunto >= 0) ? nombreArchivo.substring(0, indicePunto) : nombreArchivo;
        String nombreSalida = nombreBase + extension;

        if (entrada.getParent() == null) {
            return Paths.get(nombreSalida);
        }

        return entrada.getParent().resolve(nombreSalida);
    }

    //Lee todo el contenido de un archivo de texto
    public String leerContenido(String ruta) throws IOException {
        Path path = validarArchivo(ruta);
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}