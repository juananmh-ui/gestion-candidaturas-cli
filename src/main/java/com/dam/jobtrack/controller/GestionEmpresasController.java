package com.dam.jobtrack.controller;

import com.dam.jobtrack.config.DatabaseConfig;
import com.dam.jobtrack.exception.DatabaseException;
import com.dam.jobtrack.model.Empresa;
import com.dam.jobtrack.repository.EmpresaRepositoryJdbc;
import com.dam.jobtrack.service.ValidacionGestion;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Locale;

public final class GestionEmpresasController {
    private final EmpresaRepositoryJdbc repositorio = new EmpresaRepositoryJdbc(DatabaseConfig.getDataSource());
    private final ObservableList<Empresa> empresas = FXCollections.observableArrayList();

    @FXML private TableView<Empresa> tabla;
    @FXML private TableColumn<Empresa, String> colNombre;
    @FXML private TableColumn<Empresa, String> colSector;
    @FXML private TableColumn<Empresa, LocalDate> colFecha;
    @FXML private TextField buscar;
    @FXML private Label tituloFormulario;
    @FXML private TextField nombre;
    @FXML private TextField sector;
    @FXML private TextField web;
    @FXML private DatePicker fecha;
    @FXML private Button borrar;
    @FXML private Label aviso;

    @FXML
    private void initialize() {
        colNombre.setCellValueFactory(fila -> new ReadOnlyStringWrapper(fila.getValue().nombre()));
        colSector.setCellValueFactory(fila -> new ReadOnlyStringWrapper(fila.getValue().sector()));
        colFecha.setCellValueFactory(fila -> new ReadOnlyObjectWrapper<>(fila.getValue().fechaRegistro()));
        FilteredList<Empresa> filtradas = new FilteredList<>(empresas);
        buscar.textProperty().addListener((observable, anterior, texto) -> {
            String filtro = normalizar(texto);
            filtradas.setPredicate(empresa -> filtro.isEmpty()
                    || normalizar(empresa.nombre()).contains(filtro) || normalizar(empresa.sector()).contains(filtro));
        });
        SortedList<Empresa> ordenadas = new SortedList<>(filtradas);
        ordenadas.comparatorProperty().bind(tabla.comparatorProperty());
        tabla.setItems(ordenadas);
        tabla.getSelectionModel().selectedItemProperty().addListener((observable, anterior, seleccion) ->
                mostrarFormulario(seleccion));
        borrar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        mostrarFormulario(null);
        recargar("");
    }

    @FXML
    private void nueva() {
        tabla.getSelectionModel().clearSelection();
        mostrarFormulario(null);
        aviso.setText("");
        nombre.requestFocus();
    }

    @FXML
    private void guardar() {
        try {
            Empresa seleccion = tabla.getSelectionModel().getSelectedItem();
            Empresa empresa = new Empresa(seleccion == null ? null : seleccion.id(),
                    ValidacionGestion.texto(nombre.getText(), "El nombre", 150, true),
                    ValidacionGestion.texto(sector.getText(), "El sector", 100, false),
                    ValidacionGestion.web(web.getText()), ValidacionGestion.fecha(fecha.getValue()));
            if (empresa.id() == null) {
                repositorio.save(empresa);
            } else {
                repositorio.update(empresa);
            }
            nueva();
            buscar.clear();
            recargar("Empresa guardada.");
        } catch (IllegalArgumentException | DatabaseException error) {
            aviso.setText(error.getMessage());
        }
    }

    @FXML
    private void borrar() {
        Empresa seleccion = tabla.getSelectionModel().getSelectedItem();
        if (seleccion == null || !GestionDialogos.confirmarBorrado(tabla.getScene().getWindow(),
                "Borrar empresa", "¿Quieres borrar «" + seleccion.nombre() + "»?")) {
            return;
        }
        try {
            repositorio.delete(seleccion.id());
            nueva();
            recargar("Empresa borrada.");
        } catch (DatabaseException error) {
            aviso.setText(error.getMessage());
        }
    }

    @FXML
    private void cerrar() {
        ((Stage) tabla.getScene().getWindow()).close();
    }

    private void mostrarFormulario(Empresa empresa) {
        tituloFormulario.setText(empresa == null ? "Nueva empresa" : "Editar empresa");
        nombre.setText(empresa == null ? "" : empresa.nombre());
        sector.setText(empresa == null ? "" : empresa.sector());
        web.setText(empresa == null ? "" : empresa.web());
        fecha.setValue(empresa == null ? LocalDate.now() : empresa.fechaRegistro());
    }

    private void recargar(String mensaje) {
        try {
            empresas.setAll(repositorio.findAll());
            aviso.setText(mensaje);
        } catch (DatabaseException error) {
            aviso.setText((mensaje.isEmpty() ? "" : mensaje + " ") + error.getMessage());
        }
    }

    private static String normalizar(String texto) {
        return texto == null ? "" : texto.strip().toLowerCase(Locale.ROOT);
    }
}
