package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Util {

    public static final int TAM_BUFFER = 1500;

    public static void eliminarDirectorio(File dir) {
        if (dir.exists()) {
            if (dir.isDirectory()) {
                File[] elementos = dir.listFiles();
                for (File elemento : elementos) {
                    if (elemento.isDirectory()) {
                        eliminarDirectorio(elemento);
                    } else {
                        elemento.delete();
                    }
                }
            }

            dir.delete();
        }
    }

    public static void copiarDirectorio(File origen, File destino) throws IOException {
        if (origen.isDirectory()) {
            if (!destino.exists()) {
                destino.mkdir();
            }

            String[] archivos = origen.list();
            for (String archivo : archivos) {
                File origenArchivo = new File(origen, archivo);
                File destinoArchivo = new File(destino, archivo);

                copiarDirectorio(origenArchivo, destinoArchivo);
            }
        } else {
            Files.copy(origen.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void comprimirDirectorio(File directorio, String archivoZip) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(archivoZip); ZipOutputStream zos = new ZipOutputStream(fos)) {
            agregarDirectorioAlZip("", directorio, zos);
        }
    }

    public static void agregarDirectorioAlZip(String parentPath, File directorio, ZipOutputStream zos) throws IOException {
        for (File archivo : directorio.listFiles()) {
            if (archivo.isDirectory()) {
                agregarDirectorioAlZip(parentPath + archivo.getName() + "/", archivo, zos);
            } else {
                ZipEntry zipEntry = new ZipEntry(parentPath + archivo.getName());
                zos.putNextEntry(zipEntry);

                try (FileInputStream fis = new FileInputStream(archivo)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                }
            }
        }
    }

    public static String obtenerCaminoDeRuta(String ruta) {
        String[] componentesDeRuta = ruta.split("/");
        String camino = "";
        for (int i = 0; i < componentesDeRuta.length - 1; i++) {
            camino += componentesDeRuta[i] + "/";
        }
        
        return camino;
    }
    
    public static void extraerArchivo(ZipInputStream zis, String rutaArchivo) throws IOException {
        FileOutputStream fos = new FileOutputStream(rutaArchivo);

        byte[] b = new byte[TAM_BUFFER];
        int leido = 0;
        while ((leido = zis.read(b)) != -1) {
            fos.write(b, 0, leido);
        }

        fos.close();
    }

    public static void descomprimirArchivoZIP(String rutaArchivoZIP, String rutaDestino) throws IOException {
        File carpetaDestino = new File(rutaDestino);

        if (!carpetaDestino.exists()) {
            carpetaDestino.mkdir();
        }

        ZipInputStream zis = new ZipInputStream(new FileInputStream(rutaArchivoZIP));
        ZipEntry entrada = zis.getNextEntry();

        while (entrada != null) {
            //Creando la trayectoria de carpetas en la carpeta remota.
            File carpeta = new File(rutaDestino + obtenerCaminoDeRuta(entrada.getName()));
            carpeta.mkdirs();

            extraerArchivo(zis, rutaDestino + entrada.getName());

            zis.closeEntry();
            entrada = zis.getNextEntry();
        }

        zis.close();
    }
}
