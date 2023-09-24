package servidor;

import directorio.Directorio;
import java.io.DataInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    
    private int puerto;
    
    private ServerSocket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private DataInputStream dis;
    
    public Servidor(int puerto) {
        this.puerto = puerto;
    }
    
    public void establecerConexion() {
        try {
            this.socket = new ServerSocket(this.puerto);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void cerrarConexion() {
        try {
            this.socket.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public ServerSocket obtenerSocket() {
        return this.socket;
    }
    
    public int recibirComando(Socket cl) {
        try {
            this.dis = new DataInputStream(cl.getInputStream());
            int comando = dis.readInt();
            
            dis.close();
            
            return comando;
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return -1;
    }
    
    public void enviarCarpeta(Socket cl, Directorio archivo) {
        try {
            this.oos = new ObjectOutputStream(cl.getOutputStream());
            this.oos.writeObject(archivo);
            
            this.oos.flush();
            this.oos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public String [] recibirNombresDeArchivo() {
        try {
            Socket cl = this.socket.accept();
            this.ois = new ObjectInputStream(cl.getInputStream());
            
            String [] rutasRecibidas = (String []) this.ois.readObject();
            
            this.ois.close();
            cl.close();
            
            return rutasRecibidas;
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
}