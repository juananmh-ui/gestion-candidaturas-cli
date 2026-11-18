package com.dam.jobtrack.persistence;

import com.dam.jobtrack.model.Contacto;
import com.dam.jobtrack.model.EmpresaOpcion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "CONTACTO")
public class ContactoJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_empresa", nullable = false)
    private EmpresaJpa empresa;

    @Column(length = 150, nullable = false)
    private String nombre;

    @Column(length = 254, nullable = false)
    private String email;

    @Column(length = 40)
    private String telefono;

    @Column(length = 150)
    private String puesto;

    @Column(name = "fechaContacto", nullable = false)
    private LocalDate fechaContacto;

    private String notas;

    public void actualizar(Contacto contacto, EmpresaJpa empresa) {
        this.empresa = empresa;
        nombre = contacto.nombre();
        email = contacto.email();
        telefono = contacto.telefono();
        puesto = contacto.puesto();
        fechaContacto = contacto.fechaContacto();
        notas = contacto.notas();
    }

    public Contacto datos() {
        return new Contacto(id, new EmpresaOpcion(empresa.getId(), empresa.getNombre()),
                nombre, email, telefono, puesto, fechaContacto, notas);
    }
}
