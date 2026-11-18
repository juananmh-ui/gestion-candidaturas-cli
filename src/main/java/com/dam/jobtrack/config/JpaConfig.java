package com.dam.jobtrack.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.Map;
import java.util.function.Function;

public final class JpaConfig {
    private static EntityManagerFactory factory;

    private JpaConfig() {
    }

    private static synchronized EntityManagerFactory factory() {
        if (factory == null) {
            factory = Persistence.createEntityManagerFactory("jobtrack",
                    Map.of("jakarta.persistence.nonJtaDataSource", DatabaseConfig.getDataSource()));
        }
        return factory;
    }

    public static <T> T enTransaccion(Function<EntityManager, T> operacion) {
        try (EntityManager manager = factory().createEntityManager()) {
            var transaccion = manager.getTransaction();
            transaccion.begin();
            try {
                T resultado = operacion.apply(manager);
                transaccion.commit();
                return resultado;
            } catch (RuntimeException error) {
                if (transaccion.isActive()) {
                    try {
                        transaccion.rollback();
                    } catch (RuntimeException falloRollback) {
                        error.addSuppressed(falloRollback);
                    }
                }
                throw error;
            }
        }
    }

    public static synchronized void close() {
        if (factory != null) {
            factory.close();
            factory = null;
        }
    }
}
