package com.dam.jobtrack.service;

import com.dam.jobtrack.config.JpaConfig;
import jakarta.persistence.EntityManager;

import java.time.YearMonth;
import java.util.List;

public final class EstadisticasService {
    public record Recuento(String grupo, long total) {
    }

    public record Resumen(List<Recuento> porEmpresa, List<Recuento> porEstado, List<Recuento> porMes) {
        public long total() {
            return porEstado.stream().mapToLong(Recuento::total).sum();
        }

        public long aceptadas() {
            return porEstado.stream()
                    .filter(fila -> "Aceptada".equalsIgnoreCase(fila.grupo().strip()))
                    .mapToLong(Recuento::total).sum();
        }

        public double porcentajeExito() {
            long total = total();
            return total == 0 ? 0 : aceptadas() * 100.0 / total;
        }
    }

    public Resumen consultar() {
        return JpaConfig.enTransaccion(manager -> new Resumen(
                recuentos(manager, """
                        select o.empresa.nombre, count(o) from OfertaJpa o
                        group by o.empresa.nombre order by count(o) desc, o.empresa.nombre
                        """),
                recuentos(manager, """
                        select o.estado, count(o) from OfertaJpa o
                        group by o.estado order by count(o) desc, o.estado
                        """),
                manager.createQuery("""
                        select extract(year from o.fechaPostulacion), extract(month from o.fechaPostulacion), count(o)
                        from OfertaJpa o
                        group by extract(year from o.fechaPostulacion), extract(month from o.fechaPostulacion)
                        order by extract(year from o.fechaPostulacion), extract(month from o.fechaPostulacion)
                        """, Object[].class).getResultList().stream()
                        .map(fila -> new Recuento(YearMonth.of(((Number) fila[0]).intValue(),
                                ((Number) fila[1]).intValue()).toString(), (Long) fila[2]))
                        .toList()));
    }

    private List<Recuento> recuentos(EntityManager manager, String consulta) {
        return manager.createQuery(consulta, Object[].class).getResultList().stream()
                .map(fila -> new Recuento((String) fila[0], (Long) fila[1])).toList();
    }
}
