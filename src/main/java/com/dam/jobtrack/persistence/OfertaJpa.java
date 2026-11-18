package com.dam.jobtrack.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "OFERTA")
public class OfertaJpa {
    @Id
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_empresa", nullable = false)
    private EmpresaJpa empresa;

    @Column(nullable = false, length = 50)
    private String estado;

    @Column(name = "fechaPostulacion", nullable = false)
    private LocalDate fechaPostulacion;

    protected OfertaJpa() {
    }
}
