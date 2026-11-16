package com.dam.jobtrack.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

final class GestionDialogos {
    private GestionDialogos() {
    }

    static boolean confirmarBorrado(Window ventana, String titulo, String mensaje) {
        ButtonType borrar = new ButtonType("Borrar");
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.CANCEL, borrar);
        alerta.initOwner(ventana);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        return alerta.showAndWait().filter(borrar::equals).isPresent();
    }
}
