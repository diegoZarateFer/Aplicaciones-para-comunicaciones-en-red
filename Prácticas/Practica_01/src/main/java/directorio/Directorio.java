package directorio;

import cliente.*;
import java.io.Serializable;
import javax.swing.JTree;

public class Directorio implements Serializable {

    private JTree directorio;

    public Directorio(JTree directorio) {
        this.directorio = directorio;
    }

    public JTree getDirectory() {
        return this.directorio;
    }

    public void setDirectory(JTree directory) {
        this.directorio = directory;
    }
}
