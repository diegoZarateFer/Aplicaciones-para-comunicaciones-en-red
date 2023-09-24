package cliente;

import directorio.Directorio;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import util.Flags;

public class Drive {

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
        Directorio carpeta = cliente.recibirCarpeta();

        //Creando el arbol de la carpeta.
        JTree arbolCarpeta = carpeta.getDirectory();

        //Creando la ventana principal.
        JFrame ventana = new JFrame("Mi carpeta");

        //El programa termina al cerrar esta ventana.
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Contenedor para el arbol de la carpeta.
        JScrollPane panelCarpeta = new JScrollPane(arbolCarpeta);
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
                cliente.enviarComando(Flags.SUBIR);
//                JOptionPane.showMessageDialog(ventana, "Subir");
            }
        });

        botonDescargar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cliente.enviarComando(Flags.DESCARGAR);
                JOptionPane.showMessageDialog(ventana, "Descargar");
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
                TreePath[] rutasElegidas = arbolCarpeta.getSelectionPaths();

                if (rutasElegidas == null) {
                    JOptionPane.showMessageDialog(ventana, "Debes seleccionar un elemento para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String[] rutas = convertirRutasACadena(rutasElegidas);

                //Enviando acciones al servidor.
                cliente.enviarComando(Flags.ELIMINAR);
                cliente.enviarRutasSeleccionadas(rutas);
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
