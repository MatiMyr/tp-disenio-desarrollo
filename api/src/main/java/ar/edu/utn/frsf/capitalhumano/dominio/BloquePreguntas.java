package ar.edu.utn.frsf.capitalhumano.dominio;

import java.util.ArrayList;
import java.util.List;

public class BloquePreguntas {

    private int nroBloque;
    private boolean respondido;
    private List<PreguntaCuestionario> preguntas = new ArrayList<>();

    public BloquePreguntas() {
    }

    public BloquePreguntas(int nroBloque) {
        this.nroBloque = nroBloque;
    }

    public void agregarPregunta(PreguntaCuestionario pregunta) {
        preguntas.add(pregunta);
    }

    public int getNroBloque() {
        return nroBloque;
    }

    public void setNroBloque(int nroBloque) {
        this.nroBloque = nroBloque;
    }

    public boolean isRespondido() {
        return respondido;
    }

    public void setRespondido(boolean respondido) {
        this.respondido = respondido;
    }

    public List<PreguntaCuestionario> getPreguntas() {
        return preguntas;
    }

    public void setPreguntas(List<PreguntaCuestionario> preguntas) {
        this.preguntas = preguntas;
    }
}
