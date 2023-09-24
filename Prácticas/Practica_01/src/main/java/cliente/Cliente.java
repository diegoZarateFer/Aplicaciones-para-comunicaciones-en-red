package cliente;

import directorio.Directorio;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JFileChooser;

public class Cliente {
    
    private String host;
    private int puerto;
    
    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream dis;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    
    private JFileChooser selectorArchivos;
    
    public Cliente(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }
    
    public void comprimirCarpeta(String ruta, ZipOutputStream zos) {
        try {
            File carpetaZip = new File(ruta);
            String [] lista = carpetaZip.list();
            byte []  bufferLectura = new byte[3000];
            int bytesDentro = 0;
            for(int i = 0;i < lista.length;i++) {
                File f = new File(carpetaZip,lista[i]);
                if(f.isDirectory()) {
                    String rutaArchivo = f.getPath();
                    comprimirCarpeta(rutaArchivo, zos);
                    continue;
                }
                
                FileInputStream fis = new FileInputStream(f);
                ZipEntry entry = new ZipEntry(f.getPath());
                zos.putNextEntry(entry);
                while((bytesDentro = fis.read(bufferLectura)) != -1) {
                    zos.write(bufferLectura,0,bytesDentro);
                }
                fis.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void enviarComando(int comando) {
        try {
            this.socket = new Socket(this.host,this.puerto);
            this.dos = new DataOutputStream(this.socket.getOutputStream());
            
            this.dos.writeInt(comando);
            
            this.dos.close();
            this.socket.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void enviarRutasSeleccionadas(String [] rutas) {
        try {
            this.socket = new Socket(this.host,this.puerto);
            this.oos = new ObjectOutputStream(socket.getOutputStream());
            
            this.oos.writeObject(rutas);
            this.oos.flush();
            
            this.oos.close();
            this.socket.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public Directorio recibirCarpeta() {
        Directorio directorioVacio = new Directorio(null);
        try {
            this.socket = new Socket(this.host,this.puerto);
            this.ois = new ObjectInputStream(this.socket.getInputStream());
            
            Directorio directorio = (Directorio) ois.readObject();
            
            this.ois.close();
            this.socket.close();
            
            return directorio;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return directorioVacio;
    }
    
    public void subirArchivo() {
        try {
            this.socket = new Socket(this.host,this.puerto);
            this.selectorArchivos = new JFileChooser();
            int r = selectorArchivos.showOpenDialog(null);
            if(r == JFileChooser.APPROVE_OPTION) {
                long tamArchivo;
                String nombreArchivo, rutaArchivo;
                File f = selectorArchivos.getSelectedFile();
                if(f.isDirectory()) { // Si se selecciona una carpeta, la enviamos comprimida.
                    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("./" + f.getName() + ".zip"));
                    comprimirCarpeta(f.getAbsolutePath(),zos);
                    
                    File archivoZip = new File("./" + f.getName() + ".zi´P");
                    
                    nombreArchivo = archivoZip.getName();
                    rutaArchivo = archivoZip.getPath();
                    tamArchivo = archivoZip.length();
                    
                    zos.close();
                } else {
                    nombreArchivo = f.getName();
                    rutaArchivo = f.getPath();
                    tamArchivo = f.length();
                }
                
                this.dos = new DataOutputStream(socket.getOutputStream());
                this.dis = new DataInputStream(new FileInputStream(rutaArchivo));
                
                this.dos.writeUTF(nombreArchivo);
                this.dos.flush();
                
                this.dos.writeLong(tamArchivo);
                this.dos.flush();
                
                int n,porcentaje;
                long enviado = 0;
                while(enviado < tamArchivo) {
                    byte [] b = new byte[3000];
                    n = this.dis.read(b);
                    
                    this.dos.write(b,0,n);
                    this.dos.flush();
                    
                    enviado += n;
                    porcentaje = (int) ((enviado * 100) / tamArchivo);
                    
                    System.out.println(porcentaje + "% completado.");
                }
                
                this.dos.close();
                this.dis.close();
                this.socket.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
