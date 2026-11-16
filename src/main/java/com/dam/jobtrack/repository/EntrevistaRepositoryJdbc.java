package com.dam.jobtrack.repository;

import com.dam.jobtrack.exception.DatabaseException;
import com.dam.jobtrack.model.Entrevista;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Entrevistas y datos del puesto y de la empresa asociados. */
public final class EntrevistaRepositoryJdbc implements DAO<Entrevista, Integer> {
    private static final String CONSULTA = """
            SELECT i.id, i.id_oferta, i.fecha, i.tipo, i.notas, i.resultado,
                   o.titulo AS oferta, e.nombre AS empresa
            FROM ENTREVISTA i
            JOIN OFERTA o ON i.id_oferta = o.id
            JOIN EMPRESA e ON o.id_empresa = e.id
            """;
    private final DataSource dataSource;

    public EntrevistaRepositoryJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Entrevista> findAll() {
        return listar(null);
    }

    public List<Entrevista> findByOferta(Integer ofertaId) {
        return listar(ofertaId);
    }

    private List<Entrevista> listar(Integer ofertaId) {
        String sql = CONSULTA + (ofertaId == null ? "" : " WHERE i.id_oferta = ?")
                + " ORDER BY i.fecha, i.id";
        List<Entrevista> entrevistas = new ArrayList<>();
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            if (ofertaId != null) {
                consulta.setInt(1, ofertaId);
            }
            try (ResultSet resultado = consulta.executeQuery()) {
                while (resultado.next()) {
                    entrevistas.add(leer(resultado));
                }
            }
            return entrevistas;
        } catch (SQLException error) {
            throw new DatabaseException("No se pudieron consultar las entrevistas.", error);
        }
    }

    @Override
    public Optional<Entrevista> findById(Integer id) {
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(CONSULTA + " WHERE i.id = ?")) {
            consulta.setInt(1, id);
            try (ResultSet resultado = consulta.executeQuery()) {
                return resultado.next() ? Optional.of(leer(resultado)) : Optional.empty();
            }
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo consultar la entrevista.", error);
        }
    }

    @Override
    public Entrevista save(Entrevista entrevista) {
        String sql = "INSERT INTO ENTREVISTA (id_oferta, fecha, tipo, notas, resultado) VALUES (?, ?, ?, ?, ?)";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarDatos(sentencia, entrevista);
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    return new Entrevista(claves.getInt(1), entrevista.idOferta(), entrevista.fecha(),
                            entrevista.tipo(), entrevista.notas(), entrevista.resultado(),
                            entrevista.oferta(), entrevista.empresa());
                }
                throw new SQLException("La inserción no devolvió el identificador.");
            }
        } catch (SQLException error) {
            throw errorGuardado(error);
        }
    }

    @Override
    public void update(Entrevista entrevista) {
        String sql = "UPDATE ENTREVISTA SET id_oferta = ?, fecha = ?, tipo = ?, notas = ?, resultado = ? WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            asignarDatos(sentencia, entrevista);
            sentencia.setInt(6, entrevista.id());
            comprobarExistencia(sentencia.executeUpdate());
        } catch (SQLException error) {
            throw errorGuardado(error);
        }
    }

    @Override
    public void delete(Integer id) {
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement("DELETE FROM ENTREVISTA WHERE id = ?")) {
            sentencia.setInt(1, id);
            comprobarExistencia(sentencia.executeUpdate());
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo borrar la entrevista.", error);
        }
    }

    private static Entrevista leer(ResultSet fila) throws SQLException {
        return new Entrevista(fila.getInt("id"), fila.getInt("id_oferta"), fila.getDate("fecha").toLocalDate(),
                fila.getString("tipo"), fila.getString("notas"), fila.getString("resultado"),
                fila.getString("oferta"), fila.getString("empresa"));
    }

    private static void asignarDatos(PreparedStatement sentencia, Entrevista entrevista) throws SQLException {
        sentencia.setInt(1, entrevista.idOferta());
        sentencia.setDate(2, Date.valueOf(entrevista.fecha()));
        sentencia.setString(3, entrevista.tipo());
        sentencia.setString(4, entrevista.notas());
        sentencia.setString(5, entrevista.resultado());
    }

    private static void comprobarExistencia(int filas) {
        if (filas == 0) {
            throw new DatabaseException("La entrevista ya no existe.");
        }
    }

    private static DatabaseException errorGuardado(SQLException error) {
        if (error.getErrorCode() == 1452) {
            return new DatabaseException("La candidatura asociada ya no está disponible.", error);
        }
        return new DatabaseException("No se pudo guardar la entrevista.", error);
    }
}
