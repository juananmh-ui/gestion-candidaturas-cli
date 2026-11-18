package com.dam.jobtrack.service;

import com.dam.jobtrack.config.JpaConfig;
import com.dam.jobtrack.model.Contacto;
import com.dam.jobtrack.model.EmpresaOpcion;
import com.dam.jobtrack.persistence.ContactoJpa;
import com.dam.jobtrack.persistence.EmpresaJpa;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.regex.Pattern;

public final class ContactosService {
    private static final Pattern EMAIL = Pattern.compile("[^\\s@]+@[^\\s@]+\\.[^\\s@]+");

    public List<EmpresaOpcion> empresas() {
        return JpaConfig.enTransaccion(manager -> manager.createQuery("""
                select e from EmpresaJpa e order by e.nombre
                """, EmpresaJpa.class).getResultList().stream()
                .map(empresa -> new EmpresaOpcion(empresa.getId(), empresa.getNombre())).toList());
    }

    public List<Contacto> listar() {
        return JpaConfig.enTransaccion(manager -> manager.createQuery("""
                select c from ContactoJpa c join fetch c.empresa order by c.nombre, c.id
                """, ContactoJpa.class).getResultList().stream().map(ContactoJpa::datos).toList());
    }

    public Contacto guardar(Contacto datos) {
        if (datos.empresa() == null || datos.empresa().id() == null) {
            throw new IllegalArgumentException("Selecciona la empresa del contacto.");
        }
        String email = ValidacionGestion.texto(datos.email(), "El correo electrónico", 254, true);
        if (!EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("Introduce un correo electrónico válido.");
        }
        Contacto contacto = new Contacto(datos.id(), datos.empresa(),
                ValidacionGestion.texto(datos.nombre(), "El nombre", 150, true), email,
                ValidacionGestion.texto(datos.telefono(), "El teléfono", 40, false),
                ValidacionGestion.texto(datos.puesto(), "El puesto", 150, false),
                ValidacionGestion.fecha(datos.fechaContacto()),
                ValidacionGestion.texto(datos.notas(), "Las notas", 16000, false));

        return JpaConfig.enTransaccion(manager -> {
            EmpresaJpa empresa = manager.find(EmpresaJpa.class, contacto.empresa().id());
            if (empresa == null) {
                throw new IllegalArgumentException("La empresa ya no está disponible.");
            }
            ContactoJpa entidad = contacto.id() == null ? new ContactoJpa() : buscar(manager, contacto.id());
            entidad.actualizar(contacto, empresa);
            if (contacto.id() == null) {
                manager.persist(entidad);
            }
            return entidad.datos();
        });
    }

    public void borrar(int id) {
        JpaConfig.enTransaccion(manager -> {
            manager.remove(buscar(manager, id));
            return null;
        });
    }

    private ContactoJpa buscar(EntityManager manager, int id) {
        ContactoJpa contacto = manager.find(ContactoJpa.class, id);
        if (contacto == null) {
            throw new IllegalArgumentException("El contacto ya no existe.");
        }
        return contacto;
    }
}
