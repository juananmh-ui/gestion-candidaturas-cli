package com.dam.jobtrack.App;

import com.dam.jobtrack.config.DatabaseConfig;
import com.dam.jobtrack.config.JpaConfig;
import com.dam.jobtrack.controller.DetalleOfertaController;
import com.dam.jobtrack.controller.FormularioOfertaController;
import com.dam.jobtrack.controller.GestionEntrevistasController;
import com.dam.jobtrack.controller.ListadoOfertasController;
import com.dam.jobtrack.model.Oferta;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void stop() {
        try {
            JpaConfig.close();
        } finally {
            DatabaseConfig.close();
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/dam/jobtrack/view/listadoOfertas.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setTitle("JobTrack - Sistema de Gestión de Candidaturas (v2.0)");
        stage.setScene(scene);
        stage.setMinWidth(950);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void abrirDetalleOferta(Oferta oferta, ListadoOfertasController listener) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/dam/jobtrack/view/DetalleOferta.fxml"));
            Parent root = loader.load();

            DetalleOfertaController controller = loader.getController();
            controller.setOferta(oferta, listener);

            Stage modal = new Stage();
            modal.setTitle("Detalle de Candidatura #" + oferta.getId());
            modal.initModality(Modality.WINDOW_MODAL);
            modal.initOwner(primaryStage);
            modal.setScene(new Scene(root));
            modal.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo cargar la ventana de detalle.");
            alert.showAndWait();
        }
    }

    public static void abrirFormularioOferta(Oferta oferta, ListadoOfertasController listener) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/dam/jobtrack/view/FormularioOferta.fxml"));
            Parent root = loader.load();

            FormularioOfertaController controller = loader.getController();
            controller.setDatos(oferta, listener);

            Stage modal = new Stage();
            modal.setTitle(oferta == null ? "Nueva Candidatura" : "Editar Candidatura #" + oferta.getId());
            modal.initModality(Modality.WINDOW_MODAL);
            modal.initOwner(primaryStage);
            modal.setScene(new Scene(root));
            modal.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo abrir el formulario de ofertas.");
            alert.showAndWait();
        }
    }

    public static void abrirVentanaEmpresas() {
        try {
            Parent vista = FXMLLoader.load(App.class.getResource("/com/dam/jobtrack/view/GestionEmpresas.fxml"));
            mostrarGestion(vista, "Gestión de empresas", primaryStage);
        } catch (IOException error) {
            mostrarErrorGestion("No se pudo abrir la gestión de empresas.", primaryStage);
        }
    }

    public static void abrirVentanaEstadisticas() {
        try {
            Parent vista = FXMLLoader.load(App.class.getResource("/com/dam/jobtrack/view/Estadisticas.fxml"));
            mostrarGestion(vista, "Estadísticas de candidaturas", primaryStage);
        } catch (IOException error) {
            mostrarErrorGestion("No se pudo abrir el panel de estadísticas.", primaryStage);
        }
    }

    public static void abrirVentanaContactos() {
        try {
            Parent vista = FXMLLoader.load(App.class.getResource("/com/dam/jobtrack/view/Contactos.fxml"));
            mostrarGestion(vista, "Contactos profesionales", primaryStage);
        } catch (IOException error) {
            mostrarErrorGestion("No se pudo abrir la gestión de contactos.", primaryStage);
        }
    }

    public static void abrirVentanaEntrevistas() {
        abrirVentanaEntrevistas(null, primaryStage);
    }

    public static void abrirVentanaEntrevistas(Oferta candidatura, Window propietario) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/dam/jobtrack/view/GestionEntrevistas.fxml"));
            Parent vista = loader.load();
            GestionEntrevistasController controller = loader.getController();
            controller.abrir(candidatura);
            mostrarGestion(vista, "Gestión de entrevistas", propietario);
        } catch (IOException error) {
            mostrarErrorGestion("No se pudo abrir la gestión de entrevistas.", propietario);
        }
    }

    private static void mostrarGestion(Parent vista, String titulo, Window propietario) {
        Stage ventana = new Stage();
        ventana.setTitle(titulo);
        ventana.initOwner(propietario);
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.setScene(new Scene(vista));
        ventana.setMinWidth(800);
        ventana.setMinHeight(560);
        ventana.showAndWait();
    }

    private static void mostrarErrorGestion(String texto, Window propietario) {
        Alert aviso = new Alert(Alert.AlertType.ERROR, texto);
        aviso.initOwner(propietario);
        aviso.setHeaderText(null);
        aviso.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
