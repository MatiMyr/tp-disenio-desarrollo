package ar.edu.utn.frsf.capitalhumano.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frsf.capitalhumano.dao.IConfiguracionDAO;
import ar.edu.utn.frsf.capitalhumano.dto.ConfiguracionDTO;

/**
 * Expone los parámetros de configuración que las pantallas necesitan para
 * armarse. Hoy solo la cantidad de candidatos por página de CU025.
 */
@RestController
@RequestMapping("/configuracion")
public class ConfiguracionController {

    private static final String CLAVE_CANDIDATOS_POR_PAGINA = "candidatos_por_pagina";
    private static final int CANDIDATOS_POR_PAGINA_POR_DEFECTO = 10;

    private final IConfiguracionDAO configuracionDAO;

    public ConfiguracionController(IConfiguracionDAO configuracionDAO) {
        this.configuracionDAO = configuracionDAO;
    }

    @GetMapping
    public ConfiguracionDTO obtenerConfiguracion() {
        Object valor = configuracionDAO.obtenerParametro(CLAVE_CANDIDATOS_POR_PAGINA);
        int porPagina = CANDIDATOS_POR_PAGINA_POR_DEFECTO;
        if (valor != null) {
            try {
                porPagina = Integer.parseInt(valor.toString().trim());
            } catch (NumberFormatException e) {
                porPagina = CANDIDATOS_POR_PAGINA_POR_DEFECTO;
            }
        }
        return new ConfiguracionDTO(porPagina);
    }
}
