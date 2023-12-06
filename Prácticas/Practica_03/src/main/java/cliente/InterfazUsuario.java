package cliente;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.text.html.HTMLEditorKit;

public class InterfazUsuario {

    // Dimensiones de la ventana.
    public final static int ANCHO_VENTANA = 700;
    public final static int ALTO_VENTANA = 750;

    // Alto del panel de chat.
    public final static int ALTO_PANEL_CHAT = 550;

    // Alto del panel de selector de chat.
    public final static int ALTO_SELECTOR_CHAT = 50;

    // Alto del panel de acciones.
    public final static int ALTO_ACCIONES = 200;

    // Altura del area de envio de mensaje.
    public final static int ALTURA_AREA_ENVIO_DE_MENSAJE = 100;

    // Dimensiones del area de texto.
    public final static int ANCHO_AREA_TEXTO = 570;
    public final static int ALTO_AREA_TEXTO = 30;

    // Ruta de las imagenes de los emojis
    public final static String RUTA_EMOJI_FELIZ = "C:\\Users\\diego\\Documents\\Clases\\Aplicaciones para comunicaciones en red\\Aplicaciones-para-comunicaciones-en-red\\Prácticas\\Practica_03\\img\\emoji_1.png";
    public final static String RUTA_EMOJI_TRISTE = "C:\\Users\\diego\\Documents\\Clases\\Aplicaciones para comunicaciones en red\\Aplicaciones-para-comunicaciones-en-red\\Prácticas\\Practica_03\\img\\emoji_2.png";
    public final static String RUTA_EMOJI_RISA = "C:\\Users\\diego\\Documents\\Clases\\Aplicaciones para comunicaciones en red\\Aplicaciones-para-comunicaciones-en-red\\Prácticas\\Practica_03\\img\\emoji_3.png";
    public final static String RUTA_EMOJI_CORAZON = "C:\\Users\\diego\\Documents\\Clases\\Aplicaciones para comunicaciones en red\\Aplicaciones-para-comunicaciones-en-red\\Prácticas\\Practica_03\\img\\emoji_4.png";
    public final static String RUTA_EMOJI_SORPRENDIDO = "C:\\Users\\diego\\Documents\\Clases\\Aplicaciones para comunicaciones en red\\Aplicaciones-para-comunicaciones-en-red\\Prácticas\\Practica_03\\img\\emoji_5.png";

    // Parametros de conexio.
    private static final String HOST = "230.1.1.1";
    private static final int PUERTO = 1234;

    // Nombre de usuario.
    private static String nombreUsuario = "";

    // Cliente.
    private static Cliente cliente;

    // Contenido inicial del editor pane.
    private static final String INITIAL_HTML_TEXT = "<html><body></body></html>";

    // Area de texto de mensaje.
    private static JTextField areaDeTexto;

    public static ImageIcon obtenerIconoEmoji(String rutaImagen) {
        try {
            BufferedImage imgEmoji = ImageIO.read(new File(rutaImagen));
            BufferedImage imgEmojiRedimensionado = new BufferedImage(64, 64, imgEmoji.getType());
            Graphics2D g = imgEmojiRedimensionado.createGraphics();
            g.drawImage(imgEmoji, 0, 0, 64, 64, null);
            g.dispose();

            return new ImageIcon(imgEmojiRedimensionado);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) {

        // Oyentes para los botones.
        ActionListener oyenteBotonEnviajeMensaje = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // Obteniendo mensaje redactado.
                String contenido = areaDeTexto.getText();

                // Limpiando area de texto.
                areaDeTexto.setText("");

                // Enviando mensaje.
                cliente.enviarMensaje(contenido);
            }
        };

        ActionListener oyenteBotonEmojiFeliz = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cliente.enviarEmoji("&#x1F604;");
            }
        };

        ActionListener oyenteBotonEmojiTriste = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cliente.enviarEmoji("&#x1F641;");
            }
        };

        ActionListener oyenteBotonEmojiRisa = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cliente.enviarEmoji("&#x1F602;");
            }
        };

        ActionListener oyenteBotonEmojiCorazon = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cliente.enviarEmoji("&#x1F60D;");
            }
        };

        ActionListener oyenteBotonEmojiSorprendido = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cliente.enviarEmoji("&#x1F62F;");
            }
        };

        // Preparando objetos de tipo ImageIcon para cargar las imagenes de los emojis.
        ImageIcon iconoEmojiFeliz = new ImageIcon();
        ImageIcon iconoEmojiTriste = new ImageIcon();
        ImageIcon iconoEmojiRisa = new ImageIcon();
        ImageIcon iconoEmojiCorazon = new ImageIcon();
        ImageIcon iconoEmojiSorprendido = new ImageIcon();

        // Cargando las imagenes de los emojis.
        try {
            iconoEmojiFeliz = obtenerIconoEmoji(RUTA_EMOJI_FELIZ);
            iconoEmojiTriste = obtenerIconoEmoji(RUTA_EMOJI_TRISTE);
            iconoEmojiRisa = obtenerIconoEmoji(RUTA_EMOJI_RISA);
            iconoEmojiCorazon = obtenerIconoEmoji(RUTA_EMOJI_CORAZON);
            iconoEmojiSorprendido = obtenerIconoEmoji(RUTA_EMOJI_SORPRENDIDO);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Pidiendo el nombre del usuario que se conecta.
        do {
            nombreUsuario = JOptionPane.showInputDialog(null, "Ingresa tu nickname:");
            if (nombreUsuario == null) {
                System.exit(0);
            }
        } while (nombreUsuario.equals(""));

        // Crear un panel para el selector de chat.
        JPanel panelSelectorChat = new JPanel();

        // Establecer tamanio del panel para seleccionar un chat.
        panelSelectorChat.setPreferredSize(new Dimension(ANCHO_VENTANA, ALTO_SELECTOR_CHAT));

        //  Crear una etiqueta para el combo box.
        JLabel etiquetaSelectorDeChat = new JLabel("Chat Grupal");

        // Agregar la etiqueta del combo box.
        panelSelectorChat.add(etiquetaSelectorDeChat);

        // Crear un panel para las acciones del usuario.
        JPanel panelDeAcciones = new JPanel();

        // Establecer tamanio del panel de acciones.
        panelDeAcciones.setPreferredSize(new Dimension(ANCHO_VENTANA, ALTO_ACCIONES));

        // Crear panel para contener el envio de mensajes.
        JPanel panelEnviarMsj = new JPanel();

        // Establece las dimensiones del panel de mensajes.
        panelEnviarMsj.setPreferredSize(new Dimension(ANCHO_VENTANA, ALTURA_AREA_ENVIO_DE_MENSAJE));

        // Crear area de texto para escribir mensajes.
        areaDeTexto = new JTextField();

        // Crear un scroll pane para contener el area de texto.
        JScrollPane contenedorAreaDeTexto = new JScrollPane(areaDeTexto);

        // Establecer las dimensiones del area de texto.
        contenedorAreaDeTexto.setPreferredSize(new Dimension(ANCHO_AREA_TEXTO, ALTO_AREA_TEXTO));

        // Agregar area de texto a su contenedor.
        panelEnviarMsj.add(contenedorAreaDeTexto);

        // Crear boton para envio de mensajes.
        JButton botonEnviarMensaje = new JButton("Enviar");

        // Cambiando la fuente del texto del boton.
        Font fuenteBotonEnviar = new Font("Arial", Font.BOLD, 14);
        botonEnviarMensaje.setFont(fuenteBotonEnviar);

        // Agregando manejador de evento al boton.
        botonEnviarMensaje.addActionListener(oyenteBotonEnviajeMensaje);

        // Establecer las dimensiones del boton de enviar.
        botonEnviarMensaje.setPreferredSize(new Dimension(80, 40));

        // Agregar boton a su panel.
        panelEnviarMsj.add(botonEnviarMensaje);

        // Crear un panel para contener los botones de emojis.
        JPanel panelEmojis = new JPanel();

        //Creando botones con emojis con su icono y oyente de evento.
        JButton botonEmojiFeliz = new JButton(iconoEmojiFeliz);
        botonEmojiFeliz.setPreferredSize(new Dimension(70, 70));
        botonEmojiFeliz.addActionListener(oyenteBotonEmojiFeliz);

        JButton botonEmojiTriste = new JButton(iconoEmojiTriste);
        botonEmojiTriste.setPreferredSize(new Dimension(70, 70));
        botonEmojiTriste.addActionListener(oyenteBotonEmojiTriste);

        JButton botonEmojiRisa = new JButton(iconoEmojiRisa);
        botonEmojiRisa.setPreferredSize(new Dimension(70, 70));
        botonEmojiRisa.addActionListener(oyenteBotonEmojiRisa);

        JButton botonEmojiSorprendido = new JButton(iconoEmojiSorprendido);
        botonEmojiSorprendido.setPreferredSize(new Dimension(70, 70));
        botonEmojiSorprendido.addActionListener(oyenteBotonEmojiSorprendido);

        JButton botonEmojiCorazon = new JButton(iconoEmojiCorazon);
        botonEmojiCorazon.setPreferredSize(new Dimension(70, 70));
        botonEmojiCorazon.addActionListener(oyenteBotonEmojiCorazon);

        // Agregar boton de emoji feliz al panel de emojis
        panelEmojis.add(botonEmojiFeliz);
        panelEmojis.add(botonEmojiTriste);
        panelEmojis.add(botonEmojiSorprendido);
        panelEmojis.add(botonEmojiCorazon);
        panelEmojis.add(botonEmojiRisa);

        // Establecer el acomodo del panel de acciones.
        panelDeAcciones.setLayout(new GridLayout(2, 1));

        // Agregar contenido al panel de acciones.
        panelDeAcciones.add(panelEnviarMsj);
        panelDeAcciones.add(panelEmojis);

        // Crear un edior panel para mostrar los mensajes del chat.
        JEditorPane renderizadorDeChat = new JEditorPane();

        // Configurando el chat para que no se pueda editar.
        renderizadorDeChat.setEditable(false);

        // Preparando el editor pane para mostrar codigo HTML.
        renderizadorDeChat.setContentType("text/html");

        // Configura el EditorKit para manejar contenido HTML
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        renderizadorDeChat.setEditorKit(htmlEditorKit);

        // Establecer el contenido inicial del documento.
        renderizadorDeChat.setText(INITIAL_HTML_TEXT);

        // Creando la ventana.
        JFrame ventana = new JFrame("Sala de chat de " + nombreUsuario);

        // Hacer que la ventana no pueda cambiar de tamaño.
        ventana.setResizable(false);

        // Hacer que el programa termine con el cierre de ventana.
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  
        // Crear un panel para mostrar el chat.
        JScrollPane panelDeChat = new JScrollPane(renderizadorDeChat);

        // Ejecutando cliente.
        cliente = new Cliente(nombreUsuario, HOST, PUERTO, renderizadorDeChat);
        cliente.saludar();

        // Establecer el tamanio del panel que contiene al renderizador del chat.
        panelDeChat.setPreferredSize(new Dimension(ANCHO_VENTANA, ALTO_PANEL_CHAT));

        // Establecer el acomodo del contenido de la ventana.
        ventana.getContentPane()
                .setLayout(new BoxLayout(ventana.getContentPane(), BoxLayout.Y_AXIS));

        // Agregando contenido a la ventana
        ventana.getContentPane().add(panelSelectorChat);
        ventana.getContentPane().add(panelDeChat);
        ventana.getContentPane().add(panelDeAcciones);

        // Establecer las dimensiones de la ventana.
        ventana.setSize(ANCHO_VENTANA, ALTO_VENTANA);

        // Hacer visible la ventana.
        ventana.setVisible(true);
    }
}
