package ar.edu.utn.frsf.capitalhumano.dominio;

import java.util.ArrayList;
import java.util.List;

public class Puesto {

    private String codigo;
    private String nombre;
    private String descripcion;
    private String empresa;
    private boolean eliminado;
    private List<CompetenciaRequerida> competenciasRequeridas = new ArrayList<>();

    public void agregarCompetenciaRequerida(Competencia competencia, double ponderacionMinima) {
        competenciasRequeridas.add(new CompetenciaRequerida(competencia, ponderacionMinima));
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

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    public List<CompetenciaRequerida> getCompetenciasRequeridas() {
        return competenciasRequeridas;
    }

    public void setCompetenciasRequeridas(List<CompetenciaRequerida> competenciasRequeridas) {
        this.competenciasRequeridas = competenciasRequeridas;
    }
}
