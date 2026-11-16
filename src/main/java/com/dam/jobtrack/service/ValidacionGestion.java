package com.dam.jobtrack.service;

import java.net.URI;
import java.time.LocalDate;

/** Validaciones de los formularios de empresas y entrevistas. */
public final class ValidacionGestion {
    private ValidacionGestion() {
    }

    public static String texto(String valor, String campo, int maximo, boolean obligatorio) {
        String limpio = valor == null ? "" : valor.strip();
        if (limpio.isEmpty()) {
            if (obligatorio) {
                throw new IllegalArgumentException(campo + " es obligatorio.");
            }
            return null;
        }
        if (limpio.length() > maximo) {
            throw new IllegalArgumentException(campo + " admite hasta " + maximo + " caracteres.");
        }
        return limpio;
    }

    public static LocalDate fecha(LocalDate valor) {
        if (valor == null || valor.getYear() < 1000 || valor.getYear() > 9999) {
            throw new IllegalArgumentException("Selecciona una fecha válida.");
        }
        return valor;
    }

    public static String web(String valor) {
        String web = texto(valor, "El sitio web", 255, false);
        if (web == null) {
            return null;
        }
        try {
            URI direccion = URI.create(web);
            if (direccion.getHost() != null
                    && ("https".equalsIgnoreCase(direccion.getScheme())
                    || "http".equalsIgnoreCase(direccion.getScheme()))) {
                return web;
            }
        } catch (IllegalArgumentException error) {
            throw new IllegalArgumentException("Escribe una dirección web válida que empiece por http:// o https://.", error);
        }
        throw new IllegalArgumentException("Escribe una dirección web válida que empiece por http:// o https://.");
    }
}
