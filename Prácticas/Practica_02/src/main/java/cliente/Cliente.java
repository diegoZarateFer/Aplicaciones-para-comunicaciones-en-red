package cliente;

import directorio.ArbolCarpetas;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import util.Util;

public class Cliente {

    private String host;
    private int puerto;
    private ServerSocket socketRecibidor;

    public static final String RUTA_CLIENTE = "C:\\Users\\diego\\Documents\\Clases\\Aplicaciones para comunicaciones en red\\Aplicaciones-para-comunicaciones-en-red\\cliente\\";

    public Cliente(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    public void comprimirCarpeta(String ruta, ZipOutputStream zos) {
        try {
            File carpetaZip = new File(ruta);
            String[] lista = carpetaZip.list();
            byte[] bufferLectura = new byte[3000];
            int bytesDentro = 0;
            for (int i = 0; i < lista.length; i++) {
                File f = new File(carpetaZip, lista[i]);
                if (f.isDirectory()) {
                    String rutaArchivo = f.getPath();
                    comprimirCarpeta(rutaArchivo, zos);
                    continue;
                }

                FileInputStream fis = new FileInputStream(f);
                ZipEntry entry = new ZipEntry(f.getPath());
                zos.putNextEntry(entry);
                while ((bytesDentro = fis.read(bufferLectura)) != -1) {
                    zos.write(bufferLectura, 0, bytesDentro);
                }
                fis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enviarComando(int comando) {
        try {
            Socket socket = new Socket(this.host, this.puerto);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            dos.writeInt(comando);
            dos.flush();

            dos.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enviarCadena(String cadena) {
        try {
            Socket socket = new Socket(this.host, this.puerto);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            dos.writeUTF(cadena);
            dos.flush();

            dos.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enviarRutasSeleccionadas(String[] rutas) {
        try {
            Socket socket = new Socket(this.host, this.puerto);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            oos.writeObject(rutas);
            oos.flush();

            oos.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArbolCarpetas recibirArbolCarpetas() {
        ArbolCarpetas arbolVacio = null;
        try {
            Socket socket = new Socket(this.host, this.puerto);
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            ArbolCarpetas arbolRecibido = (ArbolCarpetas) ois.readObject();

            ois.close();
            socket.close();

            return arbolRecibido;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arbolVacio;
    }

    public void enviarArchivo(File archivo) {
        try {

            //Obteniendo datos del archivo.
            String nombreArchivo = archivo.getName();
            String rutaArchivo = archivo.getAbsolutePath();
            long tamArchivo = archivo.length();

            //Canales.
            Socket socket = new Socket(this.host, this.puerto);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(new FileInputStream(rutaArchivo));

            dos.writeUTF(nombreArchivo);
            dos.flush();

            dos.writeLong(tamArchivo);
            dos.flush();

            long enviados = 0;
            int l = 0;
            byte[] b = new byte[Util.TAM_BUFFER];
            while (enviados < tamArchivo && (l = dis.read(b)) != -1) {
                dos.write(b, 0, l);
                dos.flush();
                enviados += l;
            }

            dis.close();
            dos.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void recibirArchivoZip(String rutaDestino) {
        try {

            Socket cl = new Socket(this.host, this.puerto);
            DataInputStream dis = new DataInputStream(cl.getInputStream());

            //Obteniendo datos del archivo.
            String nombreArchivo = dis.readUTF();
            long tamArchivo = dis.readLong();

            DataOutputStream dos = new DataOutputStream(new FileOutputStream(RUTA_CLIENTE + "/" + nombreArchivo));

            long recibidos = 0;
            int l;
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

            Util.descomprimirArchivoZIP(RUTA_CLIENTE + nombreArchivo, rutaDestino + "\\");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enviarRutaSeleccionada(String rutaDestino) {
        try {
            Socket socket = new Socket(this.host, this.puerto);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            dos.writeUTF(rutaDestino);
            dos.flush();

            dos.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Funciones usando datagramas
    public void enviarArchivoDatagramas(File archivo) {
        try {
            int pto = 1234;
            String dir = "127.0.0.1";
            InetAddress dst = InetAddress.getByName(dir);
            int tam = 1024;
            byte x = 0;
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            DatagramSocket cl = new DatagramSocket();
            FileInputStream fileInputStream = new FileInputStream(archivo);
            int totalBytesLeidos = 0;

            x = 0;
            byte[] buffer = new byte[tam];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                DatagramPacket p = new DatagramPacket(buffer, bytesRead, dst, pto);
                cl.send(p);

                byte[] arregloAcuse = new byte[tam];
                String acuse = "";
                do {
                    // Recibiendo acuse del servidor
                    DatagramPacket paqueteAcuse = new DatagramPacket(arregloAcuse, arregloAcuse.length);
                    cl.receive(paqueteAcuse);   
                    
                    acuse = new String(arregloAcuse);
                } while (acuse.equals("NCK"));
            }

            // Enviar un último paquete vacío para indicar el fin del archivo
            DatagramPacket p1 = new DatagramPacket(new byte[0], 0, dst, pto);
            cl.send(p1);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
