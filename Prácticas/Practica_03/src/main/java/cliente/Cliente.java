package cliente;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import javax.swing.JEditorPane;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

public class Cliente {

    private final int TAM = 6500;

    private String nombre;
    private String host;

    private int puerto;

    private InetAddress grupo;
    private MulticastSocket cliente;

    private Receptor receptor;
    private Thread hiloReceptor;

    private JEditorPane renderizadorDeChat;

    public Cliente(String nombre, String host, int puerto, JEditorPane renderizadorDeChat) {
        this.nombre = nombre;
        this.puerto = puerto;
        this.host = host;
        this.renderizadorDeChat = renderizadorDeChat;

        try {
            cliente = new MulticastSocket(puerto);
            grupo = InetAddress.getByName(host);
            cliente.joinGroup(grupo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        receptor = new Receptor();
        hiloReceptor = new Thread(receptor);
        hiloReceptor.start();
    }

    private class Emisor implements Runnable {

        private Mensaje mensaje;

        private Emisor(Mensaje mensaje) {
            this.mensaje = mensaje;
        }

        @Override
        public void run() {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);

                oos.writeObject(mensaje);
                oos.flush();

                byte[] msj = baos.toByteArray();
                DatagramPacket paquete = new DatagramPacket(msj, msj.length, grupo, puerto);

                cliente.send(paquete);

                oos.close();
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class Receptor implements Runnable {

        @Override
        public void run() {
            try {
                DatagramPacket paqueteRecibido = new DatagramPacket(new byte[TAM], TAM);
                while (true) {
                    cliente.receive(paqueteRecibido);
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(paqueteRecibido.getData()));
                    Mensaje mensajeRecibido = (Mensaje) ois.readObject();
                    
                    HTMLDocument doc = (HTMLDocument) renderizadorDeChat.getDocument();
                    Element cuerpoDoc = doc.getElement(doc.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.BODY);
                    doc.insertBeforeEnd(cuerpoDoc, mensajeRecibido.getContenido());

                    ois.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void enviarMensaje(String contenido) {
        Mensaje mensaje = new Mensaje("<p><b>" + nombre + "</b>: " + contenido + "</p>", nombre, "TODOS", 2);
        new Thread(new Emisor(mensaje)).start();
    }

    public void enviarEmoji(String codigoEmoji) {
        Mensaje mensaje = new Mensaje("<p><b>" + nombre + "</b>: " + codigoEmoji + "</p>", nombre, "TODOS", 2);
        new Thread(new Emisor(mensaje)).start();
    }

    public void saludar() {
        String saludo = "<b> Se ha conectado: " + nombre + "</b>";
        Mensaje mensajeSaludo = new Mensaje(saludo, nombre, "TODOS", 0);
        new Thread(new Emisor(mensajeSaludo)).start();
    }
}
