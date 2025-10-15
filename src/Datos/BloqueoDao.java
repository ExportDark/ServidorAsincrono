package Datos;

import Dominio.Bloqueo;
import Dominio.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BloqueoDao {



    private static final String SQL_SELECT = "SELECT pedro, pablo FROM Bloqueo";
    private static final String SQL_INSERT = "INSERT INTO Bloqueo(pedro, pablo) VALUES (?, ?)";
    private static final String SQL_UPDATE = "UPDATE Bloqueo SET pedro = ? WHERE pablo = ?";
    private static final String SQL_DELETE = "DELETE FROM Bloqueo WHERE pedro = ?";
    List<Bloqueo> bloqueos;

    public boolean estaBloqueado(Bloqueo bloqueo){
        for(Bloqueo x : listar()){
            if (bloqueo.getPedro().equals(x.getPedro())&&bloqueo.getPablo().equals(x.getPablo())) {
                return true;
            }
        }
        return false;
    }
    // Obtener todos los usuarios
    public List<Bloqueo> listar() {
        bloqueos = new ArrayList<>();
        try (Connection conn = Conexion.getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL_SELECT); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String pedro = rs.getString("pedro");
                String pablo = rs.getString("pablo");
                bloqueos.add(new Bloqueo(pedro, pablo));
            }

        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
        return bloqueos;
    }

    // Insertar nuevo usuario
    public int bloquear(Bloqueo bloqueo) {
        int registros = 0;
        try (Connection conn = Conexion.getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {

            stmt.setString(1, bloqueo.getPedro());
            stmt.setString(2, bloqueo.getPablo());
            stmt.execute();

        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
        return registros;
    }

    // Actualizar contraseña
    public int actualizar(Bloqueo bloqueo) {
        int registros = 0;
        try (Connection conn = Conexion.getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setString(1, bloqueo.getPedro());
            stmt.setString(2, bloqueo.getPablo());
            registros = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
        return registros;
    }

    // Eliminar usuario por username
    public int eliminar(Bloqueo bloqueo) {
        int registros = 0;
        try (Connection conn = Conexion.getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setString(1, bloqueo.getPedro());
            registros = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
        return registros;
    }

}
    
