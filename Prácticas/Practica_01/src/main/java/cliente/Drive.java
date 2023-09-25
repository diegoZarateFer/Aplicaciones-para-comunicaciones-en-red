package cliente;

import directorio.ArbolCarpetas;
import directorio.Directorio;
import directorio.Nodo;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import util.Flags;
import util.Util;

public class Drive {

    public static ArbolCarpetas arbolCarpetas;

    public static final String RUTA_CLIENTE = "C:\\Users\\diego\\Documents\\Clases\\Aplicaciones para comunicaciones en red\\Aplicaciones-para-comunicaciones-en-red\\cliente\\";

    public static String[] convertirRutasACadena(TreePath[] rutasElegidas) {
        String[] rutas = new String[rutasElegidas.length];
        for (int i = 0; i < rutasElegidas.length; i++) {
            rutas[i] = "";
            Object[] componentesDeRuta = rutasElegidas[i].getPath();
            for (Object componente : componentesDeRuta) {
                DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) componente;
                rutas[i] += "/" + nodo.toString();
            }
        }

        return rutas;
    }

    public static void main(String[] args) {

        //Creando al cliente.
        Cliente cliente = new Cliente(Flags.HOST, Flags.PUERTO);

        //Recibir la carpeta remota.
        arbolCarpetas = cliente.recibirArbolCarpetas();

        //Creando la ventana principal.
        JFrame ventana = new JFrame("Mi carpeta");

        //El programa termina al cerrar esta ventana.
        ventana.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        //Contenedor para el arbol de la carpeta.
        JScrollPane panelCarpeta = new JScrollPane(arbolCarpetas.obtenerArbol());
        panelCarpeta.setPreferredSize(new Dimension(600, 550));

        // Contenedor para los botones.
        JPanel panelBotones = new JPanel();
        panelBotones.setPreferredSize(new Dimension(600, 50));

        //Creando los botones.
        JButton botonSubir = new JButton("Subir");
        JButton botonDescargar = new JButton("Descargar");
        JButton botonRenombrar = new JButton("Renombrar");
        JButton botonEliminar = new JButton("Eliminar");
        JButton botonCopiar = new JButton("Copiar");
        JButton botonCrearCarpeta = new JButton("Crear carpeta");

        //Agregando acciones a cada boton.
        botonSubir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //Obteniendo todos los elementos selccionados.
                TreePath[] seleccion = arbolCarpetas.obtenerArbol().getSelectionPaths();

                //Verificando que se seleccione un elemento.
                if (seleccion == null) {
                    JOptionPane.showMessageDialog(ventana, "¡Selecciona una carpeta para subir tus archivos!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                //Validando que solo se seleccione un elemento.
                if (seleccion.length > 1) {
                    JOptionPane.showMessageDialog(ventana, "¡Selecciona solo una carpeta para subir tus archivos!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                //Obteniendo la ruta del nodo elegido.
                TreePath caminoNodo = seleccion[0];

                //Obtener el objeto asociado a la celda seleccionada.
                DefaultMutableTreeNode celdaSeleccionada = (DefaultMutableTreeNode) caminoNodo.getLastPathComponent();
                Nodo valorNodo = (Nodo) celdaSeleccionada.getUserObject();

                //Validando que se seleccione una carpeta.
                if (!valorNodo.esCarpeta()) {
                    JOptionPane.showMessageDialog(ventana, "¡Debes seleccionar una carpeta!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                //Creando el selector de archivos.
                JFileChooser selectorArchivos = new JFileChooser();

                //Habilitando la seleccion de archivos multiples.
                selectorArchivos.setMultiSelectionEnabled(true);

                //Habiliando la seleccion tanto de archivos como carpetas.
                selectorArchivos.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                //Mostrando el selector de archivos.
                int respuesta = selectorArchivos.showOpenDialog(ventana);
                if (respuesta == JFileChooser.APPROVE_OPTION) {

                    //Extrayendo los archivos seleccionados por el usuario.
                    File[] archivosSeleccionados = selectorArchivos.getSelectedFiles();

                    //Creando el directorio temporal.
                    File drectorioTemporal = new File(RUTA_CLIENTE + "directorio_temporal");
                    drectorioTemporal.mkdirs();

                    try {

                        //Copiando archivos seleccionados a la carpeta temporal.
                        for (File archivo : archivosSeleccionados) {
                            File destino = new File(drectorioTemporal, archivo.getName());
                            Util.copiarDirectorio(archivo, destino);
                        }

                        //Comprimiendo el directorio temporal.
                        Util.comprimirDirectorio(drectorioTemporal, RUTA_CLIENTE + "comprimido.zip");

                        //Apuntando al archivo zip.
                        File archivoZip = new File(RUTA_CLIENTE + "comprimido.zip");

                        //Enviando acciones al servidor.
                        cliente.enviarComando(Flags.SUBIR);
                        cliente.enviarRutaSeleccionada(valorNodo.rutaAsociada());
                        cliente.enviarArchivo(archivoZip);

                        //Eliminando el arhivo zip.
                        archivoZip.delete();

                        //Recibimos el JTree del estado actual de la carpeta.
                        arbolCarpetas = cliente.recibirArbolCarpetas();

                        //Mostando el JTree actualizado.
                        panelCarpeta.setViewportView(arbolCarpetas.obtenerArbol());
                        panelCarpeta.updateUI();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    //Eliminando directorio auxiliar.
                    Util.eliminarDirectorio(drectorioTemporal);

                }
            }
        });

        botonDescargar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cliente.enviarComando(Flags.DESCARGAR);
            }
        });

        botonRenombrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cliente.enviarComando(Flags.RENOMBRAR);
                JOptionPane.showMessageDialog(ventana, "Renombrar");
            }
        });

        botonEliminar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                TreePath[] rutasElegidas = arbolCarpeta.getSelectionPaths();
//
//                if (rutasElegidas == null) {
//                    JOptionPane.showMessageDialog(ventana, "Debes seleccionar un elemento para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
//                    return;
//                }
//
//                String[] rutas = convertirRutasACadena(rutasElegidas);
//
//                //Enviando acciones al servidor.
//                cliente.enviarComando(Flags.ELIMINAR);
//                cliente.enviarRutasSeleccionadas(rutas);
//
//                //Recibimos el JTree del estado actual de la carpeta.
//                Directorio carpeta = cliente.recibirCarpeta();
//                arbolCarpeta = carpeta.getDirectory();
//
//                //Mostando el JTree actualizado.
//                panelCarpeta.setViewportView(arbolCarpeta);
//                panelCarpeta.updateUI();
            }
        });

        botonCopiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cliente.enviarComando(Flags.COPIAR);
                JOptionPane.showMessageDialog(ventana, "Copiar");
            }
        });

        botonCrearCarpeta.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cliente.enviarComando(Flags.CREAR_CARPETA);
                JOptionPane.showMessageDialog(ventana, "Crear Carpeta");
            }
        });

        ventana.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //Cerrando la conexion con el servidor cuando la ventana se cierra.
                cliente.enviarComando(Flags.DESCONECTAR);
                System.exit(0);
            }
        });

        //Agregando los botones a su contenedor.
        panelBotones.add(botonSubir);
        panelBotones.add(botonDescargar);
        panelBotones.add(botonRenombrar);
        panelBotones.add(botonEliminar);
        panelBotones.add(botonCopiar);
        panelBotones.add(botonCrearCarpeta);

        ventana.getContentPane().setLayout(new BoxLayout(ventana.getContentPane(), BoxLayout.Y_AXIS));

        // Agregar el contenido de la ventana.
        ventana.getContentPane().add(panelCarpeta);
        ventana.getContentPane().add(panelBotones);

        ventana.setSize(600, 600);
        ventana.setResizable(false);
        ventana.setVisible(true);
    }
}
