package Datos;

import Dominio.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDao {

    private static final String SQL_SELECT = "SELECT username, password FROM usuario";
    private static final String SQL_INSERT = "INSERT INTO usuario(username, password) VALUES (?, ?)";
    private static final String SQL_UPDATE = "UPDATE usuario SET password = ? WHERE username = ?";
    private static final String SQL_DELETE = "DELETE FROM usuario WHERE username = ?";
    List<Usuario> usuarios;

    public boolean registrarUsuario(Usuario x) {
        if (existeUsuario(x.getUsername()) == null) {
            insertar(x);
            return true;
        }
        return false;

    }

    public boolean iniciarSesion(Usuario x) {
        Usuario buscar = existeUsuario(x.getUsername());
        if (buscar != null && x.getPassword().equals(buscar.getPassword())) {
            return true;
        }
        return false;
    }
  
    public Usuario existeUsuario(String usr) {
        usuarios = listar();
        for (Usuario usuario : usuarios) {
            if (usr.equals(usuario.getUsername())) {
                return usuario;
            }
        }
        return null;
    }

    // Obtener todos los usuarios
    public List<Usuario> listar() {
        usuarios = new ArrayList<>();
        try (Connection conn = Conexion.getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL_SELECT); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                usuarios.add(new Usuario(username, password));
            }

        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
        return usuarios;
    }

    // Insertar nuevo usuario
    public int insertar(Usuario usuario) {
        int registros = 0;
        try (Connection conn = Conexion.getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {

            stmt.setString(1, usuario.getUsername());
            stmt.setString(2, usuario.getPassword());
            registros = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
        return registros;
    }

    // Actualizar contraseña
    public int actualizar(Usuario usuario) {
        int registros = 0;
        try (Connection conn = Conexion.getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setString(1, usuario.getPassword());
            stmt.setString(2, usuario.getUsername());
            registros = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
        return registros;
    }

    // Eliminar usuario por username
    public int eliminar(Usuario usuario) {
        int registros = 0;
        try (Connection conn = Conexion.getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setString(1, usuario.getUsername());
            registros = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
        return registros;
    }

}
