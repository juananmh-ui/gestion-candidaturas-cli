package com.dam.jobtrack.model;

import java.time.LocalDate;

public record Entrevista(Integer id, int idOferta, LocalDate fecha, String tipo, String notas,
                         String resultado, String oferta, String empresa) {
    public String resultadoVisible() {
        return resultado == null || resultado.isBlank() ? "Pendiente" : resultado;
    }
}
