package com.dam.jobtrack.repository;

import com.dam.jobtrack.exception.DatabaseException;
import com.dam.jobtrack.model.Empresa;

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

/** Acceso JDBC al directorio de empresas. */
public final class EmpresaRepositoryJdbc implements DAO<Empresa, Integer> {
    private static final String CAMPOS = "SELECT id, nombre, sector, web, fechaRegistro FROM EMPRESA";
    private final DataSource dataSource;

    public EmpresaRepositoryJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Empresa> findAll() {
        List<Empresa> empresas = new ArrayList<>();
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(CAMPOS + " ORDER BY nombre");
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) {
                empresas.add(leer(resultado));
            }
            return empresas;
        } catch (SQLException error) {
            throw new DatabaseException("No se pudieron consultar las empresas.", error);
        }
    }

    @Override
    public Optional<Empresa> findById(Integer id) {
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement consulta = conexion.prepareStatement(CAMPOS + " WHERE id = ?")) {
            consulta.setInt(1, id);
            try (ResultSet resultado = consulta.executeQuery()) {
                return resultado.next() ? Optional.of(leer(resultado)) : Optional.empty();
            }
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo consultar la empresa.", error);
        }
    }

    @Override
    public Empresa save(Empresa empresa) {
        String sql = "INSERT INTO EMPRESA (nombre, sector, web, fechaRegistro) VALUES (?, ?, ?, ?)";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarDatos(sentencia, empresa);
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    return new Empresa(claves.getInt(1), empresa.nombre(), empresa.sector(), empresa.web(),
                            empresa.fechaRegistro());
                }
                throw new SQLException("La inserción no devolvió el identificador.");
            }
        } catch (SQLException error) {
            throw errorGuardado(error);
        }
    }

    @Override
    public void update(Empresa empresa) {
        String sql = "UPDATE EMPRESA SET nombre = ?, sector = ?, web = ?, fechaRegistro = ? WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            asignarDatos(sentencia, empresa);
            sentencia.setInt(5, empresa.id());
            comprobarExistencia(sentencia.executeUpdate());
        } catch (SQLException error) {
            throw errorGuardado(error);
        }
    }

    @Override
    public void delete(Integer id) {
        // Bloquea la empresa mientras se comprueba si tiene ofertas vinculadas.
        // Así una inserción concurrente no puede terminar en un borrado en cascada.
        try (Connection conexion = dataSource.getConnection()) {
            conexion.setAutoCommit(false);
            try (PreparedStatement empresa = conexion.prepareStatement("SELECT id FROM EMPRESA WHERE id = ? FOR UPDATE");
                 PreparedStatement ofertas = conexion.prepareStatement("SELECT id FROM OFERTA WHERE id_empresa = ? LIMIT 1");
                 PreparedStatement borrar = conexion.prepareStatement("DELETE FROM EMPRESA WHERE id = ?")) {
                empresa.setInt(1, id);
                try (ResultSet resultado = empresa.executeQuery()) {
                    if (!resultado.next()) {
                        throw new DatabaseException("La empresa ya no existe.");
                    }
                }
                ofertas.setInt(1, id);
                try (ResultSet resultado = ofertas.executeQuery()) {
                    if (resultado.next()) {
                        throw new DatabaseException("La empresa tiene candidaturas vinculadas. Reasígnalas antes de borrarla.");
                    }
                }
                borrar.setInt(1, id);
                comprobarExistencia(borrar.executeUpdate());
                conexion.commit();
            } catch (SQLException | RuntimeException error) {
                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    error.addSuppressed(errorRollback);
                }
                throw error;
            }
        } catch (SQLException error) {
            throw new DatabaseException("No se pudo borrar la empresa.", error);
        }
    }

    private static Empresa leer(ResultSet fila) throws SQLException {
        return new Empresa(fila.getInt("id"), fila.getString("nombre"), fila.getString("sector"),
                fila.getString("web"), fila.getDate("fechaRegistro").toLocalDate());
    }

    private static void asignarDatos(PreparedStatement sentencia, Empresa empresa) throws SQLException {
        sentencia.setString(1, empresa.nombre());
        sentencia.setString(2, empresa.sector());
        sentencia.setString(3, empresa.web());
        sentencia.setDate(4, Date.valueOf(empresa.fechaRegistro()));
    }

    private static void comprobarExistencia(int filas) {
        if (filas == 0) {
            throw new DatabaseException("La empresa ya no existe.");
        }
    }

    private static DatabaseException errorGuardado(SQLException error) {
        if (error.getErrorCode() == 1062) {
            return new DatabaseException("Ya existe una empresa con ese nombre.", error);
        }
        return new DatabaseException("No se pudo guardar la empresa.", error);
    }
}
