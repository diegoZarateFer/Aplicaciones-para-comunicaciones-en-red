package directorio;

import java.io.File;
import java.io.Serializable;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class ArbolCarpetas implements Serializable {

    private File carpeta;
    private JTree arbol;
    private DefaultMutableTreeNode raiz;
    private DefaultTreeModel modelo;
    

    public ArbolCarpetas(File carpeta) {
        this.carpeta = carpeta;
        raiz = new DefaultMutableTreeNode(new Nodo(carpeta.getName(),carpeta.getAbsolutePath(),true,false));
        modelo = new DefaultTreeModel(raiz);
        arbol = new JTree(raiz);
        
        construirArbol(this.carpeta, modelo, raiz);
    }

    public JTree obtenerArbol() {
        return arbol;
    }

    public static void construirArbol(File f, DefaultTreeModel modelo, DefaultMutableTreeNode raiz) {
        File directorios[] = f.listFiles();
        for (int i = 0; i < directorios.length; i++) {
            if (directorios[i].isDirectory()) {
                DefaultMutableTreeNode folder = new DefaultMutableTreeNode(new Nodo(directorios[i].getName(),directorios[i].getAbsolutePath(),true,false));
                modelo.insertNodeInto(folder, raiz, i);
                construirArbol(directorios[i], modelo, folder);
            } else {
                DefaultMutableTreeNode folder = new DefaultMutableTreeNode(new Nodo(directorios[i].getName(),directorios[i].getAbsolutePath(),false,false));
                modelo.insertNodeInto(folder, raiz, i);
            }
        }
    }
}
