package com.dam.jobtrack.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "EMPRESA")
public class EmpresaJpa {
    @Id
    private Integer id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @OneToMany(mappedBy = "empresa")
    private List<OfertaJpa> ofertas = new ArrayList<>();

    @OneToMany(mappedBy = "empresa")
    private List<ContactoJpa> contactos = new ArrayList<>();

    protected EmpresaJpa() {
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
