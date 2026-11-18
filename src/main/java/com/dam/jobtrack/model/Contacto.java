package com.dam.jobtrack.model;

import java.time.LocalDate;

public record Contacto(Integer id, EmpresaOpcion empresa, String nombre, String email,
                       String telefono, String puesto, LocalDate fechaContacto, String notas) {
}
