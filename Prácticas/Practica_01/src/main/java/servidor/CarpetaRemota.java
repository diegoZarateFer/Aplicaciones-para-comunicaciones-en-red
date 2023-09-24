package servidor;

import directorio.Directorio;
import java.io.File;
import java.net.Socket;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import util.Flags;

public class CarpetaRemota {

    public final static String CARPETA_REMOTA = "C:\\archivos";
    
    public static void construirCarpeta(File f, DefaultTreeModel modelo, DefaultMutableTreeNode raiz) {
        File directorios[] = f.listFiles();
        for (int i = 0; i < directorios.length; i++) {
            if (directorios[i].isDirectory()) {
                DefaultMutableTreeNode folder = new DefaultMutableTreeNode(directorios[i].getName());
                modelo.insertNodeInto(folder, raiz, i);
                construirCarpeta(directorios[i], modelo, folder);
            } else {
                DefaultMutableTreeNode folder = new DefaultMutableTreeNode(directorios[i].getName());
                modelo.insertNodeInto(folder, raiz, i);
            }
        }
    }

    public static void main(String[] args) {        
        //Instanciando la clase Servidor.
        Servidor servidor = new Servidor(8000);

        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("archivos");
        DefaultTreeModel modelo = new DefaultTreeModel(raiz);
        JTree arbol = new JTree(raiz);

        try {
            servidor.establecerConexion();
            while (true) {

                //Preparando la carpta remota.
                File carpetaRemota = new File(CARPETA_REMOTA);
                construirCarpeta(carpetaRemota, modelo, raiz);

                //Creando el la carpeta del cliente.
                Directorio directorio = new Directorio(arbol);

                //Aceptando la conexion con el cliente.
                Socket socketCliente = servidor.obtenerSocket().accept();

                //Enviando la carpeta al cliente.
                servidor.enviarCarpeta(socketCliente, directorio);

                //Cerrando cliente.
                socketCliente.close();

                for (;;) {

                    //Recibiendo el comando del cliente.
                    socketCliente = servidor.obtenerSocket().accept();
                    int comando = servidor.recibirComando(socketCliente);
                    switch (comando) {
                        case Flags.ELIMINAR -> {

                            //Recibe las rutas de los archivos que se van a eliminar de la carpeta.
                            String[] rutasArchivos = servidor.recibirNombresDeArchivo();

//                            //Iteramos las rutas recibidas par eliminar el archivo correspondiente.
                            for (String rutaArchivo : rutasArchivos) {
                                File archivoAEliminar = new File(rutaArchivo);
                                System.out.println(rutaArchivo);
                                if(archivoAEliminar.exists()) {
                                    archivoAEliminar.delete();
                                } else {
                                    System.out.println("ERROR: Archivo no encontrado.");
                                }
                            }
                        }
                    }

                    socketCliente.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
