package com.dam.jobtrack.model;

import java.time.LocalDate;

public record EntrevistaResumen(
        Integer id,
        LocalDate fecha,
        String tipo,
        String resultado,
        String notas
) {
    public static EntrevistaResumen desde(Entrevista entrevista) {
        return new EntrevistaResumen(entrevista.id(), entrevista.fecha(), entrevista.tipo(),
                entrevista.resultadoVisible(), entrevista.notas());
    }
}
