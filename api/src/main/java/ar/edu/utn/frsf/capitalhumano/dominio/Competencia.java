package ar.edu.utn.frsf.capitalhumano.dominio;

import java.util.ArrayList;
import java.util.List;

public class Competencia {

    private String codigo;
    private String nombre;
    private String descripcion;
    private TipoCompetencia tipo;
    private boolean eliminado;
    private List<Factor> factores = new ArrayList<>();

    public Competencia() {
    }

    public Competencia(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public void agregarFactor(Factor factor) {
        factores.add(factor);
        factor.setCompetencia(this);
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoCompetencia getTipo() {
        return tipo;
    }

    public void setTipo(TipoCompetencia tipo) {
        this.tipo = tipo;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    public List<Factor> getFactores() {
        return factores;
    }

    public void setFactores(List<Factor> factores) {
        this.factores = factores;
    }
}
