package servidor;

import directorio.ArbolCarpetas;
import directorio.Directorio;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import util.Util;

public class Servidor {

    private int puerto;

    private ServerSocket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private InputStream is;
    private FileOutputStream fos;
    private DataOutputStream dos;

    public static final String RUTA_SERVIDOR = "C:\\Users\\diego\\Documents\\Clases\\Aplicaciones para comunicaciones en red\\Aplicaciones-para-comunicaciones-en-red\\servidor\\";

    public Servidor(int puerto) {
        this.puerto = puerto;
    }

    public void establecerConexion() {
        try {
            this.socket = new ServerSocket(this.puerto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cerrarConexion() {
        try {
            this.socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ServerSocket obtenerSocket() {
        return this.socket;
    }

    public int recibirComando(Socket cl) {
        try {
            DataInputStream dis = new DataInputStream(cl.getInputStream());
            int comando = dis.readInt();

            dis.close();

            return comando;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public String recibirRuta() {
        try {
            Socket cl = this.socket.accept();
            DataInputStream dis = new DataInputStream(cl.getInputStream());

            String rutaRecibida = dis.readUTF();
            return rutaRecibida;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public void recibirArchivoZip(String rutaDestino) {
        try {

            Socket cl = this.socket.accept();
            DataInputStream dis = new DataInputStream(cl.getInputStream());

            //Obteniendo datos del archivo.
            String nombreArchivo = dis.readUTF();
            long tamArchivo = dis.readLong();

            DataOutputStream dos = new DataOutputStream(new FileOutputStream(RUTA_SERVIDOR + nombreArchivo));

            long recibidos = 0;
            int l = 0;
            while (recibidos < tamArchivo) {
                byte[] b = new byte[Util.TAM_BUFFER];
                l = dis.read(b);

                if (l == -1) {
                    break;
                }

                dos.write(b, 0, l);
                dos.flush();

                recibidos += l;
            }

            dos.close();
            dis.close();
            cl.close();

            //Descomprimir el archivo en la carpeta remota.
            Util.descomprimirArchivoZIP(RUTA_SERVIDOR + nombreArchivo, rutaDestino);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enviarArbolCarpetas(Socket cl, ArbolCarpetas arbol) {
        try {

            this.oos = new ObjectOutputStream(cl.getOutputStream());
            this.oos.writeObject(arbol);

            this.oos.flush();
            this.oos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] recibirNombresDeArchivo() {
        try {
            Socket cl = this.socket.accept();
            this.ois = new ObjectInputStream(cl.getInputStream());

            String[] rutasRecibidas = (String[]) this.ois.readObject();

            this.ois.close();
            cl.close();

            return rutasRecibidas;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
