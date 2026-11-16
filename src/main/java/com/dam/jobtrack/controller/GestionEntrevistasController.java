package com.dam.jobtrack.controller;

import com.dam.jobtrack.config.DatabaseConfig;
import com.dam.jobtrack.exception.DatabaseException;
import com.dam.jobtrack.model.Entrevista;
import com.dam.jobtrack.model.Oferta;
import com.dam.jobtrack.repository.EntrevistaRepositoryJdbc;
import com.dam.jobtrack.repository.OfertaRepositoryJdbc;
import com.dam.jobtrack.service.ValidacionGestion;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import java.util.List;

public final class GestionEntrevistasController {
    private record OpcionOferta(int id, String titulo, String empresa) {
        @Override
        public String toString() {
            return titulo + " · " + empresa;
        }
    }

    private final EntrevistaRepositoryJdbc repositorio = new EntrevistaRepositoryJdbc(DatabaseConfig.getDataSource());
    private final OfertaRepositoryJdbc ofertas = new OfertaRepositoryJdbc(DatabaseConfig.getDataSource());
    private Integer ofertaDetalleId;

    @FXML private TableView<Entrevista> tabla;
    @FXML private TableColumn<Entrevista, LocalDate> colFecha;
    @FXML private TableColumn<Entrevista, String> colOferta;
    @FXML private TableColumn<Entrevista, String> colTipo;
    @FXML private TableColumn<Entrevista, String> colResultado;
    @FXML private Label encabezado;
    @FXML private Label tituloFormulario;
    @FXML private ComboBox<OpcionOferta> oferta;
    @FXML private DatePicker fecha;
    @FXML private TextField tipo;
    @FXML private TextField resultado;
    @FXML private TextArea notas;
    @FXML private Button borrar;
    @FXML private Label aviso;

    @FXML
    private void initialize() {
        colFecha.setCellValueFactory(fila -> new ReadOnlyObjectWrapper<>(fila.getValue().fecha()));
        colOferta.setCellValueFactory(fila -> new ReadOnlyStringWrapper(fila.getValue().oferta()));
        colTipo.setCellValueFactory(fila -> new ReadOnlyStringWrapper(fila.getValue().tipo()));
        colResultado.setCellValueFactory(fila -> {
            String valor = fila.getValue().resultado();
            return new ReadOnlyStringWrapper(valor == null ? "Pendiente" : valor);
        });
        tabla.getSelectionModel().selectedItemProperty().addListener((observable, anterior, seleccion) ->
                mostrarFormulario(seleccion));
        borrar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
    }

    public void abrir(Oferta candidatura) {
        ofertaDetalleId = candidatura == null ? null : candidatura.getId();
        if (candidatura != null) {
            encabezado.setText("Entrevistas · " + candidatura.getTitulo());
        }
        try {
            List<Oferta> disponibles = candidatura == null ? ofertas.findAll() : List.of(candidatura);
            oferta.getItems().setAll(disponibles.stream()
                    .map(item -> new OpcionOferta(item.getId(), item.getTitulo(), item.getNombreEmpresa())).toList());
            oferta.setDisable(candidatura != null);
            mostrarFormulario(null);
            recargar(disponibles.isEmpty() ? "Añade una candidatura para programar una entrevista." : "");
        } catch (DatabaseException error) {
            aviso.setText(error.getMessage());
        }
    }

    @FXML
    private void nueva() {
        tabla.getSelectionModel().clearSelection();
        mostrarFormulario(null);
        aviso.setText("");
        tipo.requestFocus();
    }

    @FXML
    private void guardar() {
        try {
            OpcionOferta candidatura = oferta.getValue();
            if (candidatura == null) {
                throw new IllegalArgumentException("Selecciona una candidatura.");
            }
            Entrevista seleccion = tabla.getSelectionModel().getSelectedItem();
            Entrevista entrevista = new Entrevista(seleccion == null ? null : seleccion.id(), candidatura.id(),
                    ValidacionGestion.fecha(fecha.getValue()),
                    ValidacionGestion.texto(tipo.getText(), "El tipo de entrevista", 50, true),
                    ValidacionGestion.texto(notas.getText(), "Las notas", 16000, false),
                    ValidacionGestion.texto(resultado.getText(), "El resultado", 50, false),
                    candidatura.titulo(), candidatura.empresa());
            if (entrevista.id() == null) {
                repositorio.save(entrevista);
            } else {
                repositorio.update(entrevista);
            }
            nueva();
            recargar("Entrevista guardada.");
        } catch (IllegalArgumentException | DatabaseException error) {
            aviso.setText(error.getMessage());
        }
    }

    @FXML
    private void borrar() {
        Entrevista seleccion = tabla.getSelectionModel().getSelectedItem();
        if (seleccion == null || !GestionDialogos.confirmarBorrado(tabla.getScene().getWindow(),
                "Borrar entrevista", "¿Quieres borrar la entrevista de «" + seleccion.oferta() + "»?")) {
            return;
        }
        try {
            repositorio.delete(seleccion.id());
            nueva();
            recargar("Entrevista borrada.");
        } catch (DatabaseException error) {
            aviso.setText(error.getMessage());
        }
    }

    @FXML
    private void cerrar() {
        ((Stage) tabla.getScene().getWindow()).close();
    }

    private void mostrarFormulario(Entrevista entrevista) {
        tituloFormulario.setText(entrevista == null ? "Nueva entrevista" : "Editar entrevista");
        Integer id = entrevista == null ? ofertaDetalleId : Integer.valueOf(entrevista.idOferta());
        oferta.setValue(oferta.getItems().stream().filter(item -> Integer.valueOf(item.id()).equals(id))
                .findFirst().orElse(null));
        fecha.setValue(entrevista == null ? LocalDate.now() : entrevista.fecha());
        tipo.setText(entrevista == null ? "" : entrevista.tipo());
        resultado.setText(entrevista == null ? "" : entrevista.resultado());
        notas.setText(entrevista == null ? "" : entrevista.notas());
    }

    private void recargar(String mensaje) {
        try {
            List<Entrevista> lista = ofertaDetalleId == null ? repositorio.findAll() : repositorio.findByOferta(ofertaDetalleId);
            tabla.getItems().setAll(lista);
            aviso.setText(mensaje);
        } catch (DatabaseException error) {
            aviso.setText((mensaje.isEmpty() ? "" : mensaje + " ") + error.getMessage());
        }
    }
}
