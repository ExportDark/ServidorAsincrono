package Servicio;

import Datos.UsuarioDao;
import Dominio.Usuario;
import java.io.IOException;
import servidorasincrono.ServidorMulti.ServidorMulti;
import servidorasincrono.ServidorMulti.UnCliente;

public class Sesion {

    private final UnCliente cliente;
    private final UsuarioDao ud = new UsuarioDao();
    private boolean sesionInvitado = true;
    private int mensajesPrueba = 3;

    public Sesion(UnCliente cliente) {
        this.cliente = cliente;
    }

    public void mostrarMenu() throws IOException {
        while (true) {
            cliente.salida().writeUTF("""
                === Aplicación de Mensajes ===
                1. Iniciar Sesión
                2. Registrarse
                3. Modo Invitado
                """);

            String opcion = cliente.entrada().readUTF();
            switch (opcion) {
                case "1":
                    cliente.salida().writeUTF(iniciarSesion());
                    break;
                case "2":
                    cliente.salida().writeUTF(registrarse());
                    break;
                case "3":
                    sesionInvitado = true;
                    cliente.salida().writeUTF("Entraste en modo invitado.");
                    break;
                default:
                    cliente.salida().writeUTF("opcion no valida.");
            }
        }
    }

    private String iniciarSesion() throws IOException {
        String cred[] = pedirCredenciales().split(" ");
        if (validarCredenciales(cred[0], cred[1])) {
            if (ud.iniciarSesion(new Usuario(cred[0], cred[1]))) {
                ServidorMulti.cambiarIdCliente(cliente.getId(), cred[0]);
                return "Sesion Iniciada\n";
            }
            return "usr o psw Incorrectos\n";

        }
        return "Formato No Valido\n";
    }

    private String registrarse() throws IOException {
        String cred[] = pedirCredenciales().split(" ");
        if (validarCredenciales(cred[0], cred[1])) {
            if (ud.registrarUsuario(new Usuario(cred[0], cred[1]))) {
                return "Registro Exitoso!\n";
            }
            return "Usuario Ya Registrado\n";
        }
        return "Formato No Valido\n";

    }

    private boolean validarCredenciales(String usr, String psw) {
        return usr.length() >= 3 && psw.length() >= 5 && usr.matches("^[a-zA-Z0-9_]+$") && psw.matches("^[a-zA-Z0-9_]+$");
    }

    private String pedirCredenciales() throws IOException {
        cliente.salida().writeUTF("usr: ");
        String usr = cliente.entrada().readUTF();
        cliente.salida().writeUTF("psw ");
        String psw = cliente.entrada().readUTF();
        if (usr.startsWith(" ") || psw.startsWith(" ") || usr.isBlank() || psw.isBlank()) {//empieza con espacio o esta vacio
            return "vacio vacio";
        }
        return usr + " " + psw;
    }

    public boolean esInvitado() {
        return this.sesionInvitado;
    }

    public int getMensajesRestantes() {
        return this.mensajesPrueba;
    }

    public void consumirMensaje() {
        this.mensajesPrueba--;
    }

}
