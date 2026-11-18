package com.dam.jobtrack.controller;

import com.dam.jobtrack.model.Contacto;
import com.dam.jobtrack.model.EmpresaOpcion;
import com.dam.jobtrack.service.ContactosService;
import jakarta.persistence.PersistenceException;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;

public final class ContactosController {
    private final ContactosService servicio = new ContactosService();

    @FXML private TableView<Contacto> tabla;
    @FXML private TableColumn<Contacto, String> colNombre;
    @FXML private TableColumn<Contacto, String> colEmpresa;
    @FXML private TableColumn<Contacto, String> colPuesto;
    @FXML private TableColumn<Contacto, String> colEmail;
    @FXML private Label aviso;
    @FXML private Label titulo;
    @FXML private Button borrar;
    @FXML private Button guardar;
    @FXML private TextField nombre;
    @FXML private TextField email;
    @FXML private TextField telefono;
    @FXML private ComboBox<EmpresaOpcion> empresa;
    @FXML private TextField puesto;
    @FXML private DatePicker fechaContacto;
    @FXML private TextArea notas;

    @FXML
    private void initialize() {
        colNombre.setCellValueFactory(fila -> new ReadOnlyStringWrapper(fila.getValue().nombre()));
        colEmpresa.setCellValueFactory(fila -> new ReadOnlyStringWrapper(fila.getValue().empresa().nombre()));
        colPuesto.setCellValueFactory(fila -> new ReadOnlyStringWrapper(fila.getValue().puesto()));
        colEmail.setCellValueFactory(fila -> new ReadOnlyStringWrapper(fila.getValue().email()));
        tabla.getSelectionModel().selectedItemProperty().addListener((observable, anterior, actual) -> formulario(actual));
        borrar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        guardar.disableProperty().bind(empresa.valueProperty().isNull());
        formulario(null);
        try {
            empresa.getItems().setAll(servicio.empresas());
            recargar(empresa.getItems().isEmpty() ? "Añade una empresa antes de crear un contacto." : "");
        } catch (PersistenceException error) {
            aviso.setText("No se pudieron cargar los contactos. Comprueba la conexión y ejecuta schema-v3.sql.");
        }
    }

    @FXML
    private void nuevo() {
        tabla.getSelectionModel().clearSelection();
        formulario(null);
        nombre.requestFocus();
        aviso.setText("");
    }

    @FXML
    private void guardar() {
        Contacto seleccionado = tabla.getSelectionModel().getSelectedItem();
        try {
            servicio.guardar(new Contacto(seleccionado == null ? null : seleccionado.id(), empresa.getValue(),
                    nombre.getText(), email.getText(), telefono.getText(), puesto.getText(),
                    fechaContacto.getValue(), notas.getText()));
        } catch (IllegalArgumentException error) {
            aviso.setText(error.getMessage());
            return;
        } catch (PersistenceException error) {
            aviso.setText("No se pudo guardar el contacto.");
            return;
        }
        nuevo();
        actualizar("Contacto guardado.");
    }

    @FXML
    private void borrar() {
        Contacto contacto = tabla.getSelectionModel().getSelectedItem();
        if (contacto == null || !GestionDialogos.confirmarBorrado(tabla.getScene().getWindow(),
                "Borrar contacto", "¿Quieres borrar a «" + contacto.nombre() + "»?")) {
            return;
        }
        try {
            servicio.borrar(contacto.id());
        } catch (IllegalArgumentException | PersistenceException error) {
            aviso.setText("No se pudo borrar el contacto. Vuelve a abrir el listado para actualizarlo.");
            return;
        }
        nuevo();
        actualizar("Contacto borrado.");
    }

    private void formulario(Contacto contacto) {
        titulo.setText(contacto == null ? "Nuevo contacto" : "Editar contacto");
        empresa.setValue(contacto == null ? null : contacto.empresa());
        nombre.setText(contacto == null ? "" : contacto.nombre());
        email.setText(contacto == null ? "" : contacto.email());
        telefono.setText(contacto == null ? "" : contacto.telefono());
        puesto.setText(contacto == null ? "" : contacto.puesto());
        fechaContacto.setValue(contacto == null ? LocalDate.now() : contacto.fechaContacto());
        notas.setText(contacto == null ? "" : contacto.notas());
    }

    private void actualizar(String mensaje) {
        try {
            recargar(mensaje);
        } catch (PersistenceException error) {
            aviso.setText(mensaje + " Vuelve a abrir el listado para actualizarlo.");
        }
    }

    private void recargar(String mensaje) {
        tabla.getItems().setAll(servicio.listar());
        aviso.setText(mensaje);
    }

    @FXML
    private void cerrar() {
        ((Stage) tabla.getScene().getWindow()).close();
    }
}
