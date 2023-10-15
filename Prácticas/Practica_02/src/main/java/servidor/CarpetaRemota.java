package servidor;

import directorio.ArbolCarpetas;
import java.io.File;
import java.net.Socket;
import util.Flags;
import util.Util;

public class CarpetaRemota {

    public final static String CARPETA_REMOTA = "C:\\Users\\diego\\Documents\\Clases\\Aplicaciones para comunicaciones en red\\Aplicaciones-para-comunicaciones-en-red\\servidor\\archivos\\";
    
    public static String obtenerExtensionArchivo(String rutaArchivo) {
        int i = rutaArchivo.lastIndexOf(".");
        if (i > 0 && i < rutaArchivo.length() - 1) {
            return rutaArchivo.substring(i + 1);
        }

        return null;
    }

    public static void main(String[] args) {
        //Instanciando la clase Servidor.
        Servidor servidor = new Servidor(8000);

        //Referencias para gestionar el servidor.
        Socket socketCliente;
        ArbolCarpetas arbolCarpetas;

        try {
            servidor.establecerConexion();
            while (true) {

                boolean clienteCerrado = false;

                //Preparando la carpeta remota.
                File carpetaRemota = new File(CARPETA_REMOTA);

                //Llenando el arbol con la informacion de la carpeta remota.
                arbolCarpetas = new ArbolCarpetas(new File(CARPETA_REMOTA));

                //Aceptando la conexion con el cliente.
                socketCliente = servidor.obtenerSocket().accept();

                //Enviando la carpeta al cliente.
                servidor.enviarArbolCarpetas(socketCliente, arbolCarpetas);

                //Cerrando cliente.
                socketCliente.close();

                for (;;) {
                    //Recibiendo el comando del cliente.
                    socketCliente = servidor.obtenerSocket().accept();
                    int comando = servidor.recibirComando(socketCliente);
                    socketCliente.close();

                    switch (comando) {
                        case Flags.ELIMINAR -> {

                            //Recibe las rutas de los archivos que se van a eliminar de la carpeta.
                            String[] rutasArchivos = servidor.recibirNombresDeArchivo();

                            //Iteramos las rutas recibidas para eliminar el archivo correspondiente.
                            for (String rutaArchivo : rutasArchivos) {
                                //Apuntamos al archivo.
                                File archivoAEliminar = new File(carpetaRemota + rutaArchivo);

                                //Eliminando el archivo.
                                Util.eliminarDirectorio(archivoAEliminar);
                            }

                            //Enviando JTree actualizado.
                            socketCliente = servidor.obtenerSocket().accept();
                            servidor.enviarArbolCarpetas(socketCliente, new ArbolCarpetas(new File(CARPETA_REMOTA)));
                            socketCliente.close();
                        }

                        case Flags.SUBIR -> {

                            //Recibindo la ruta destino.
                            String rutaDestino = servidor.recibirCadena();

                            //Recibiendo archivos subidos del cliente.
                            servidor.recibirArchivoZipDatagramas(rutaDestino + "\\");

                            //Enviando JTree actualizado.
                            socketCliente = servidor.obtenerSocket().accept();
                            servidor.enviarArbolCarpetas(socketCliente, new ArbolCarpetas(new File(CARPETA_REMOTA)));
                            socketCliente.close();
                        }

                        case Flags.DESCARGAR -> {
                            // Recibe las rutas de los archivos que se van a descargar.
                            String[] rutasArchivos = servidor.recibirNombresDeArchivo();

                            //Creando directorio temporal.
                            File drectorioTemporal = new File("directorio_temporal_servidor");
                            drectorioTemporal.mkdirs();

                            //Copiando archivos al directorio temporal.
                            for (String ruta : rutasArchivos) {
                                File archivo = new File(CARPETA_REMOTA + ruta);
                                File destino = new File(drectorioTemporal, archivo.getName());

                                Util.copiarDirectorio(archivo, destino);
                            }

                            //Comprimiendo directorio temporal.
                            Util.comprimirDirectorio(drectorioTemporal, "descarga.zip");

                            //Apuntando al archivo zip.
                            File descargaZip = new File("descarga.zip");

                            //Enviando descarga al cliente.
                            socketCliente = servidor.obtenerSocket().accept();
                            servidor.enviarArchivo(socketCliente, descargaZip);
                            socketCliente.close();

                            //Borrando archivo zip.
                            descargaZip.delete();

                            //Borrando el directorio temporal.
                            Util.eliminarDirectorio(drectorioTemporal);
                        }

                        case Flags.RENOMBRAR -> {
                            //Recibe ruta del archivo a renombrar.
                            String rutaRecibida = servidor.recibirCadena();
                            String nuevoNombre = servidor.recibirCadena();

                            //Apuntando al archivo a renombrar.
                            File archivo = new File(CARPETA_REMOTA + rutaRecibida);
                            File nuevoArchivo;
                            if (archivo.isDirectory()) {
                                nuevoArchivo = new File(archivo.getParent(), nuevoNombre);
                            } else {
                                nuevoArchivo = new File(archivo.getParent(), nuevoNombre + "." + obtenerExtensionArchivo(archivo.getAbsolutePath()));
                            }

                            //Renombrando archivo.
                            archivo.renameTo(nuevoArchivo);

                            //Enviando JTree actualizado.
                            socketCliente = servidor.obtenerSocket().accept();
                            servidor.enviarArbolCarpetas(socketCliente, new ArbolCarpetas(new File(CARPETA_REMOTA)));
                            socketCliente.close();
                        }

                        case Flags.CREAR_CARPETA -> {
                            //Recibir ruta de la nueva carpeta.
                            String rutaRecibida = servidor.recibirCadena();

                            //Recibir nombre de la carpeta nueva.
                            String nombreCarpeta = servidor.recibirCadena();

                            //Creando la nueva carpeta.
                            File carpetaNueva = new File(new File(rutaRecibida), nombreCarpeta + "/");
                            carpetaNueva.mkdirs();

                            //Enviando JTree actualizado.
                            socketCliente = servidor.obtenerSocket().accept();
                            servidor.enviarArbolCarpetas(socketCliente, new ArbolCarpetas(new File(CARPETA_REMOTA)));
                            socketCliente.close();
                        }

                        case Flags.COPIAR -> {
                            //Recibir ruta del archivo y nombre.
                            String rutaRecibida = servidor.recibirCadena();
                            String nombreCopia = servidor.recibirCadena();

                            //Apuntando al archivo a copiar.
                            File archivo = new File(CARPETA_REMOTA + rutaRecibida);
                            
                            //Creando copia
                            File copia;
                            if (archivo.isDirectory()) {
                                copia = new File(archivo.getParent(), nombreCopia);
                            } else {
                                copia = new File(archivo.getParent(), nombreCopia + "." + obtenerExtensionArchivo(archivo.getAbsolutePath()));
                            }
                            
                            //Copiando archivos.
                            Util.copiarDirectorio(archivo, copia);
                            
                            //Enviando JTree actualizado.
                            socketCliente = servidor.obtenerSocket().accept();
                            servidor.enviarArbolCarpetas(socketCliente, new ArbolCarpetas(new File(CARPETA_REMOTA)));
                            socketCliente.close();
                        }

                        case Flags.DESCONECTAR -> {
                            servidor.cerrarConexion();
                            socketCliente.close();
                            clienteCerrado = true;
                        }
                    }

                    if (clienteCerrado) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
