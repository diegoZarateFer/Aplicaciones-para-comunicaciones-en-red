package servidor;

import directorio.ArbolCarpetas;
import directorio.Directorio;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import util.Flags;

public class CarpetaRemota {

    public final static String CARPETA_REMOTA = "C:\\Users\\diego\\Documents\\Clases\\Aplicaciones para comunicaciones en red\\Aplicaciones-para-comunicaciones-en-red\\servidor\\archivos\\";

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

                //Preparando la carpta remota.
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

                    switch (comando) {
//                        case Flags.ELIMINAR -> {
//
//                            //Recibe las rutas de los archivos que se van a eliminar de la carpeta.
//                            String[] rutasArchivos = servidor.recibirNombresDeArchivo();
//
//                            //Iteramos las rutas recibidas par eliminar el archivo correspondiente.
//                            for (String rutaArchivo : rutasArchivos) {
//                                File archivoAEliminar = new File(rutaArchivo);
//                                eliminarDirectorio(archivoAEliminar);
//                            }
//
//                            //Regresamos un JTree que contiene el esquema de la carpeta actualizada.
//                            raiz = new DefaultMutableTreeNode("archivos");
//                            modelo = new DefaultTreeModel(raiz);
//                            arbol = new JTree(raiz);
//
//                            construirCarpeta(carpetaRemota, modelo, raiz);
//
//                            directorio = new Directorio(arbol);
//                            socketCliente = servidor.obtenerSocket().accept();
//                            servidor.enviarCarpeta(socketCliente, directorio);
//                        }

                        case Flags.SUBIR -> {

                            //Recibindo la ruta destino.
                            String rutaDestino = servidor.recibirRuta();

                            //Recibiendo archivos subidos del cliente.
                            servidor.recibirArchivoZip(rutaDestino + "/");

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
