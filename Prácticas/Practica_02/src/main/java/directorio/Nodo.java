package directorio;

import java.io.Serializable;

public class Nodo implements Serializable {

    private String rutaAsociada;
    private String nombre;
    
    private boolean esCarpeta;
    private boolean estaExpandido;

    public Nodo(String nombre, String rutaAsociada, boolean esCarpeta, boolean estaExpandido) {
        this.rutaAsociada = rutaAsociada;
        this.esCarpeta = esCarpeta;
        this.estaExpandido = estaExpandido;
        this.nombre = nombre;
    }

    public String obtenerNombre() {
        return nombre;
    }
    
    public String rutaAsociada() {
        return rutaAsociada;
    }

    public boolean esCarpeta() {
        return esCarpeta;
    }

    public boolean estaExpandido() {
        return estaExpandido;
    }

    public void establecerEsCarpeta(boolean esCarpeta) {
        this.esCarpeta = esCarpeta;
    }

    public void establecerEstaExpandido(boolean estaExpandido) {
        this.estaExpandido = estaExpandido;
    }
    
    @Override
    public String toString() {
        return obtenerNombre();
    }

}
