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
            for (int j = 1; j < componentesDeRuta.length; j++) {
                Object componente = componentesDeRuta[j];
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
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
        JButton botonCrearCarpeta = new JButton("Crear carpeta");
        JButton botonCopiar = new JButton("Copiar");

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
                        drectorioTemporal.delete();

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

                //Obtener archivos seleccionados para descargar.
                TreePath[] seleccion = arbolCarpetas.obtenerArbol().getSelectionPaths();

                //Verificando que se seleccione un elemento.
                if (seleccion == null) {
                    JOptionPane.showMessageDialog(ventana, "¡Selecciona los archivos que deseas descargar!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                //Creando el selector de archivos.
                JFileChooser selectorArchivos = new JFileChooser();

                //Habilitando solo la seleccion de carpetas.
                selectorArchivos.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                //Mostrando el selector de archivos.
                int respuesta = selectorArchivos.showOpenDialog(ventana);
                if (respuesta == JFileChooser.APPROVE_OPTION) {

                    //Guardando ruta donde se guarda el archivo.
                    File destinoDescarga = selectorArchivos.getSelectedFile();

                    //Enviando informacion al servidor.
                    cliente.enviarComando(Flags.DESCARGAR);
                    cliente.enviarRutasSeleccionadas(convertirRutasACadena(seleccion));

                    //Recibiendo el archivo de descarga.
                    cliente.recibirArchivoZip(destinoDescarga.getAbsolutePath());
                }
            }
        });

        botonRenombrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Obtener elemento seleccionados para renombrar.
                TreePath[] seleccion = arbolCarpetas.obtenerArbol().getSelectionPaths();

                //Verificando que se seleccione un elemento.
                if (seleccion == null) {
                    JOptionPane.showMessageDialog(ventana, "¡Selecciona los archivos que deseas renombrar!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                //Verificando que solo se seleccione un archivo.
                if (seleccion.length > 1) {
                    JOptionPane.showMessageDialog(ventana, "¡Selecciona solo un elemento!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String nuevoNombre = JOptionPane.showInputDialog(ventana, "Ingresa el nuevo nombre del elemento:");

                //Cancelar operacion.
                if (nuevoNombre == null) {
                    return;
                }

                //Arreglo de rutas.
                String rutas[] = convertirRutasACadena(seleccion);

                //Enviando acciones al servidor.
                cliente.enviarComando(Flags.RENOMBRAR);
                cliente.enviarRutaSeleccionada(rutas[0]);
                cliente.enviarCadena(nuevoNombre);

                //Recibimos el JTree del estado actual de la carpeta.
                arbolCarpetas = cliente.recibirArbolCarpetas();

                //Mostando el JTree actualizado.
                panelCarpeta.setViewportView(arbolCarpetas.obtenerArbol());
                panelCarpeta.updateUI();
            }
        });

        botonEliminar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath[] rutasElegidas = arbolCarpetas.obtenerArbol().getSelectionPaths();

                if (rutasElegidas == null) {
                    JOptionPane.showMessageDialog(ventana, "¡Selecciona que archivos eliminar!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String[] rutas = convertirRutasACadena(rutasElegidas);

                //Enviando acciones al servidor.
                cliente.enviarComando(Flags.ELIMINAR);
                cliente.enviarRutasSeleccionadas(rutas);

                //Recibimos el JTree del estado actual de la carpeta.
                arbolCarpetas = cliente.recibirArbolCarpetas();

                //Mostando el JTree actualizado.
                panelCarpeta.setViewportView(arbolCarpetas.obtenerArbol());
                panelCarpeta.updateUI();
            }
        });

        botonCrearCarpeta.addActionListener(new ActionListener() {
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

                String nombreCarpeta = JOptionPane.showInputDialog(ventana, "Ingresa el nombre de la carpeta:");

                //Cancelar operacion.
                if (nombreCarpeta == null) {
                    return;
                }

                //Enviando acciones al servidor.
                cliente.enviarComando(Flags.CREAR_CARPETA);
                cliente.enviarRutaSeleccionada(valorNodo.rutaAsociada());
                cliente.enviarCadena(nombreCarpeta);

                //Recibimos el JTree del estado actual de la carpeta.
                arbolCarpetas = cliente.recibirArbolCarpetas();

                //Mostando el JTree actualizado.
                panelCarpeta.setViewportView(arbolCarpetas.obtenerArbol());
                panelCarpeta.updateUI();
            }
        });

        botonCopiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Obteniendo todos los elementos selccionados.
                TreePath[] seleccion = arbolCarpetas.obtenerArbol().getSelectionPaths();

                //Verificando que se seleccione al menos un elemento.
                if (seleccion == null) {
                    JOptionPane.showMessageDialog(ventana, "¡Selecciona una carpeta para subir tus archivos!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                //Verificando que solo se seleccione solo un archivo.
                if (seleccion.length > 1) {
                    JOptionPane.showMessageDialog(ventana, "¡Selecciona solo un elemento!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String nombreCopia = JOptionPane.showInputDialog(ventana, "Ingresa el nombre de la copia:");
                
                if(nombreCopia == null) {
                    return;
                }

                String[] rutas = convertirRutasACadena(seleccion);

                //Enviando acciones al servidor.
                cliente.enviarComando(Flags.COPIAR);
                cliente.enviarRutaSeleccionada(rutas[0]);
                cliente.enviarCadena(nombreCopia);

                //Recibimos el JTree del estado actual de la carpeta.
                arbolCarpetas = cliente.recibirArbolCarpetas();

                //Mostando el JTree actualizado.
                panelCarpeta.setViewportView(arbolCarpetas.obtenerArbol());
                panelCarpeta.updateUI();
            }
        });

        //Agregando los botones a su contenedor.
        panelBotones.add(botonSubir);
        panelBotones.add(botonDescargar);
        panelBotones.add(botonRenombrar);
        panelBotones.add(botonEliminar);
        panelBotones.add(botonCrearCarpeta);
        panelBotones.add(botonCopiar);

        ventana.getContentPane()
                .setLayout(new BoxLayout(ventana.getContentPane(), BoxLayout.Y_AXIS));

        // Agregar el contenido de la ventana.
        ventana.getContentPane()
                .add(panelCarpeta);
        ventana.getContentPane()
                .add(panelBotones);

        ventana.setSize(
                600, 600);
        ventana.setResizable(
                false);
        ventana.setVisible(
                true);
    }
}
