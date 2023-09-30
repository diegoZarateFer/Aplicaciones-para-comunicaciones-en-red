package cliente;

import directorio.ArbolCarpetas;
import directorio.Nodo;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import static servidor.CarpetaRemota.obtenerExtensionArchivo;
import util.Flags;
import util.Util;

public class Drive {

    public static ArbolCarpetas arbolCarpetaRemota;
    public static ArbolCarpetas arbolCarpetaLocal;

    public static final String RUTA_CLIENTE = "C:\\Users\\diego\\Documents\\Clases\\Aplicaciones para comunicaciones en red\\Aplicaciones-para-comunicaciones-en-red\\cliente\\";
    public static final String CARPETA_LOCAL = RUTA_CLIENTE + "Mi carpeta local";

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
        arbolCarpetaRemota = cliente.recibirArbolCarpetas();

        //Cargando la carpeta local.
        File carpetaLocal = new File(CARPETA_LOCAL);
        carpetaLocal.mkdirs();

        //Preparando el arbol de la carpeta local.
        arbolCarpetaLocal = new ArbolCarpetas(carpetaLocal);

        //Creando la ventana principal.
        JFrame ventana = new JFrame("Mi servidor");

        //El programa termina al cerrar esta ventana.
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Contenedor para el arbol de la carpeta remota.
        JScrollPane panelCarpetaRemota = new JScrollPane(arbolCarpetaRemota.obtenerArbol());
        panelCarpetaRemota.setPreferredSize(new Dimension(400, 750));

        //Contenedor para el árbol de la carpeta local.
        JScrollPane panelCarpetaLocal = new JScrollPane(arbolCarpetaLocal.obtenerArbol());
        panelCarpetaLocal.setPreferredSize(new Dimension(400, 750));

        //Contenedor de las carpetas.
        JPanel panelCarpetas = new JPanel(new GridLayout(1, 2));

        //Contenedor de los botones.
        JPanel panelBotones = new JPanel(new GridLayout(1, 2));
        
        //Contenedor de los títulos.
        JPanel panelTitulos = new JPanel(new GridLayout(1, 2));
        
        //Labels.
        JLabel labelCarpetaLocal = new JLabel("Carpeta Local");
        labelCarpetaLocal.setPreferredSize(new Dimension(400,50));
        labelCarpetaLocal.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel labelCarpetaRemota = new JLabel("Carpeta Remota");
        labelCarpetaRemota.setPreferredSize(new Dimension(400,50));
        labelCarpetaRemota.setHorizontalAlignment(SwingConstants.CENTER);

        // Contenedor para los botones de la carpeta remota.
        JPanel panelBotonesRemotos = new JPanel();
        panelBotonesRemotos.setPreferredSize(new Dimension(400, 100));

        // Contenedor para los botones de la carpeta local.
        JPanel panelBotonesLocales = new JPanel();
        panelBotonesRemotos.setPreferredSize(new Dimension(400, 100));

        //Creando los botones de la carpeta remota.
        JButton botonSubirRemoto = new JButton("Subir");
        JButton botonDescargarRemoto = new JButton("Descargar");
        JButton botonRenombrarRemoto = new JButton("Renombrar");
        JButton botonEliminarRemoto = new JButton("Eliminar");
        JButton botonCrearCarpetaRemota = new JButton("Crear carpeta");
        JButton botonCopiarRemota = new JButton("Copiar");

        //Creando botones de la carpeta local.
        JButton botonRenombrarLocal = new JButton("Renombrar");
        JButton botonEliminarLocal = new JButton("Eliminar");
        JButton botonCopiarLocal = new JButton("Copiar");

        //Agregando acciones a cada boton.
        botonSubirRemoto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //Obteniendo todos los elementos selccionados.
                TreePath[] seleccion = arbolCarpetaRemota.obtenerArbol().getSelectionPaths();

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
                        arbolCarpetaRemota = cliente.recibirArbolCarpetas();

                        //Mostando el JTree actualizado.
                        panelCarpetaRemota.setViewportView(arbolCarpetaRemota.obtenerArbol());
                        panelCarpetaRemota.updateUI();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    //Eliminando directorio auxiliar.
                    Util.eliminarDirectorio(drectorioTemporal);

                }
            }
        });

        botonDescargarRemoto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //Obtener archivos seleccionados para descargar.
                TreePath[] seleccion = arbolCarpetaRemota.obtenerArbol().getSelectionPaths();

                //Verificando que se seleccione un elemento.
                if (seleccion == null) {
                    JOptionPane.showMessageDialog(ventana, "¡Selecciona los archivos que deseas descargar!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                //Enviando informacion al servidor.
                cliente.enviarComando(Flags.DESCARGAR);
                cliente.enviarRutasSeleccionadas(convertirRutasACadena(seleccion));

                //Recibiendo el archivo de descarga.
                cliente.recibirArchivoZip(carpetaLocal.getAbsolutePath());

                //Actualizando el arbol de la carpeta local.
                arbolCarpetaLocal = new ArbolCarpetas(carpetaLocal);

                //Mostando el JTree actualizado.
                panelCarpetaLocal.setViewportView(arbolCarpetaLocal.obtenerArbol());
                panelCarpetaLocal.updateUI();
            }
        });

        botonRenombrarRemoto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Obtener elemento seleccionados para renombrar.
                TreePath[] seleccion = arbolCarpetaRemota.obtenerArbol().getSelectionPaths();

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
                arbolCarpetaRemota = cliente.recibirArbolCarpetas();

                //Mostando el JTree actualizado.
                panelCarpetaRemota.setViewportView(arbolCarpetaRemota.obtenerArbol());
                panelCarpetaRemota.updateUI();
            }
        });

        botonEliminarRemoto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath[] rutasElegidas = arbolCarpetaRemota.obtenerArbol().getSelectionPaths();

                if (rutasElegidas == null) {
                    JOptionPane.showMessageDialog(ventana, "¡Selecciona que archivos eliminar!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String[] rutas = convertirRutasACadena(rutasElegidas);

                //Enviando acciones al servidor.
                cliente.enviarComando(Flags.ELIMINAR);
                cliente.enviarRutasSeleccionadas(rutas);

                //Recibimos el JTree del estado actual de la carpeta.
                arbolCarpetaRemota = cliente.recibirArbolCarpetas();

                //Mostando el JTree actualizado.
                panelCarpetaRemota.setViewportView(arbolCarpetaRemota.obtenerArbol());
                panelCarpetaRemota.updateUI();
            }
        });

        botonCrearCarpetaRemota.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Obteniendo todos los elementos selccionados.
                TreePath[] seleccion = arbolCarpetaRemota.obtenerArbol().getSelectionPaths();

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
                arbolCarpetaRemota = cliente.recibirArbolCarpetas();

                //Mostando el JTree actualizado.
                panelCarpetaRemota.setViewportView(arbolCarpetaRemota.obtenerArbol());
                panelCarpetaRemota.updateUI();
            }
        });

        botonCopiarRemota.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Obteniendo todos los elementos selccionados.
                TreePath[] seleccion = arbolCarpetaRemota.obtenerArbol().getSelectionPaths();

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

                if (nombreCopia == null) {
                    return;
                }

                String[] rutas = convertirRutasACadena(seleccion);

                //Enviando acciones al servidor.
                cliente.enviarComando(Flags.COPIAR);
                cliente.enviarRutaSeleccionada(rutas[0]);
                cliente.enviarCadena(nombreCopia);

                //Recibimos el JTree del estado actual de la carpeta.
                arbolCarpetaRemota = cliente.recibirArbolCarpetas();

                //Mostando el JTree actualizado.
                panelCarpetaRemota.setViewportView(arbolCarpetaRemota.obtenerArbol());
                panelCarpetaRemota.updateUI();
            }
        });

        botonRenombrarLocal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Obtener elemento seleccionados para renombrar.
                TreePath[] seleccion = arbolCarpetaLocal.obtenerArbol().getSelectionPaths();

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

                //Dialogo para obtener nuevo nombre de archivo.
                String nuevoNombre = JOptionPane.showInputDialog(ventana, "Ingresa el nuevo nombre del elemento:");

                //Cancelar operacion.
                if (nuevoNombre == null) {
                    return;
                }

                String[] rutas = convertirRutasACadena(seleccion);

                //Apuntando al archivo a renombrar.
                File archivo = new File(CARPETA_LOCAL + "\\" + rutas[0]);

                //Preparando archivo con nuevo nombre.
                File nuevoArchivo;
                if (archivo.isDirectory()) {
                    nuevoArchivo = new File(archivo.getParent(), nuevoNombre);
                } else {
                    nuevoArchivo = new File(archivo.getParent(), nuevoNombre + "." + obtenerExtensionArchivo(archivo.getAbsolutePath()));
                }

                //Renombrando archivo.
                archivo.renameTo(nuevoArchivo);

                //Actualizando el arbol de la carpeta local.
                arbolCarpetaLocal = new ArbolCarpetas(carpetaLocal);

                //Mostando el JTree actualizado.
                panelCarpetaLocal.setViewportView(arbolCarpetaLocal.obtenerArbol());
                panelCarpetaLocal.updateUI();
            }
        });

        botonEliminarLocal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath[] rutasElegidas = arbolCarpetaLocal.obtenerArbol().getSelectionPaths();

                if (rutasElegidas == null) {
                    JOptionPane.showMessageDialog(ventana, "¡Selecciona que archivos eliminar!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String[] rutas = convertirRutasACadena(rutasElegidas);

                for (String rutaArchivo : rutas) {
                    File archivo = new File(CARPETA_LOCAL + "\\" + rutaArchivo);
                    Util.eliminarDirectorio(archivo);
                }

                //Actualizando el arbol de la carpeta local.
                arbolCarpetaLocal = new ArbolCarpetas(carpetaLocal);

                //Mostando el JTree actualizado.
                panelCarpetaLocal.setViewportView(arbolCarpetaLocal.obtenerArbol());
                panelCarpetaLocal.updateUI();
            }
        });

        botonCopiarLocal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Obteniendo todos los elementos selccionados.
                TreePath[] seleccion = arbolCarpetaLocal.obtenerArbol().getSelectionPaths();

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

                if (nombreCopia == null) {
                    return;
                }

                String[] rutas = convertirRutasACadena(seleccion);

                //Apuntando al archivo a copiar.
                File archivo = new File(CARPETA_LOCAL + "\\" + rutas[0]);

                //Creando copia
                File copia;
                if (archivo.isDirectory()) {
                    copia = new File(archivo.getParent(), nombreCopia);
                } else {
                    copia = new File(archivo.getParent(), nombreCopia + "." + obtenerExtensionArchivo(archivo.getAbsolutePath()));
                }

                try {
                    //Copiando archivos.
                    Util.copiarDirectorio(archivo, copia);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                //Actualizando el arbol de la carpeta local.
                arbolCarpetaLocal = new ArbolCarpetas(carpetaLocal);

                //Mostando el JTree actualizado.
                panelCarpetaLocal.setViewportView(arbolCarpetaLocal.obtenerArbol());
                panelCarpetaLocal.updateUI();
            }
        });

        //Agregando carpetas a su contenedor.
        panelCarpetas.add(panelCarpetaLocal);
        panelCarpetas.add(panelCarpetaRemota);

        //Agregando los botones de carpeta remota a su contenedor.
        panelBotonesRemotos.add(botonSubirRemoto);
        panelBotonesRemotos.add(botonDescargarRemoto);
        panelBotonesRemotos.add(botonRenombrarRemoto);
        panelBotonesRemotos.add(botonEliminarRemoto);
        panelBotonesRemotos.add(botonCrearCarpetaRemota);
        panelBotonesRemotos.add(botonCopiarRemota);

        //Agregando los botones de la carpeta local a su contenedor.
        panelBotonesLocales.add(botonRenombrarLocal);
        panelBotonesLocales.add(botonEliminarLocal);
        panelBotonesLocales.add(botonCopiarLocal);
        
        //Agregando los labels.
        panelTitulos.add(labelCarpetaLocal);
        panelTitulos.add(labelCarpetaRemota);

        //Agregando paneles de botones
        panelBotones.add(panelBotonesLocales);
        panelBotones.add(panelBotonesRemotos);

        ventana.getContentPane()
                .setLayout(new BoxLayout(ventana.getContentPane(), BoxLayout.Y_AXIS));

        // Agregar el contenido de la ventana.{
        ventana.getContentPane().add(panelTitulos);
        ventana.getContentPane().add(panelCarpetas);
        ventana.getContentPane().add(panelBotones);
        ventana.setSize(
                800, 800);
        ventana.setResizable(
                false);
        ventana.setVisible(
                true);
    }
}
