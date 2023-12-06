package cliente;

import java.io.Serializable;

public class Mensaje implements Serializable {

    public Mensaje(String contenido, String origen, String destino, int tipo) {
        this.contenido = contenido;
        this.origen = origen;
        this.destino = destino;
        this.tipo = tipo;
    }

    public String getContenido() {
        return contenido;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public int getTipo() {
        return tipo;
    }

    private String contenido;
    private String origen;
    private String destino;

    private int tipo;
}
