package com.dam.jobtrack.controller;

import com.dam.jobtrack.service.EstadisticasService;
import com.dam.jobtrack.service.EstadisticasService.Recuento;
import com.dam.jobtrack.service.EstadisticasService.Resumen;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.Locale;

public final class EstadisticasController {
    private final EstadisticasService servicio = new EstadisticasService();

    @FXML private Label resumen;
    @FXML private Label aviso;
    @FXML private Button actualizar;
    @FXML private TableView<Recuento> empresas;
    @FXML private TableView<Recuento> estados;
    @FXML private TableView<Recuento> meses;
    @FXML private TableColumn<Recuento, String> empresa;
    @FXML private TableColumn<Recuento, Number> totalEmpresa;
    @FXML private TableColumn<Recuento, String> estado;
    @FXML private TableColumn<Recuento, Number> totalEstado;
    @FXML private TableColumn<Recuento, String> mes;
    @FXML private TableColumn<Recuento, Number> totalMes;

    @FXML
    private void initialize() {
        configurar(empresa, totalEmpresa);
        configurar(estado, totalEstado);
        configurar(mes, totalMes);
        cargar();
    }

    private void configurar(TableColumn<Recuento, String> grupo, TableColumn<Recuento, Number> total) {
        grupo.setCellValueFactory(fila -> new ReadOnlyStringWrapper(fila.getValue().grupo()));
        total.setCellValueFactory(fila -> new ReadOnlyLongWrapper(fila.getValue().total()));
    }

    @FXML
    private void cargar() {
        actualizar.setDisable(true);
        aviso.setText("Cargando estadísticas…");
        Task<Resumen> consulta = new Task<>() {
            @Override
            protected Resumen call() {
                return servicio.consultar();
            }
        };
        consulta.setOnSucceeded(event -> {
            mostrar(consulta.getValue());
            actualizar.setDisable(false);
            aviso.setText("");
        });
        consulta.setOnFailed(event -> {
            actualizar.setDisable(false);
            aviso.setText("No se pudieron consultar las estadísticas. Comprueba la conexión con MySQL.");
        });
        Thread.ofVirtual().start(consulta);
    }

    private void mostrar(Resumen datos) {
        empresas.getItems().setAll(datos.porEmpresa());
        estados.getItems().setAll(datos.porEstado());
        meses.getItems().setAll(datos.porMes());

        NumberFormat formato = NumberFormat.getPercentInstance(Locale.forLanguageTag("es-ES"));
        formato.setMaximumFractionDigits(1);
        String tasa = datos.total() == 0 ? "Sin datos" : formato.format(datos.porcentajeExito() / 100);
        resumen.setText("Total: " + datos.total() + "   ·   Aceptadas: " + datos.aceptadas() + "   ·   Tasa de éxito: " + tasa);
    }

    @FXML
    private void cerrar() {
        ((Stage) resumen.getScene().getWindow()).close();
    }
}
