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
    private boolean menuInicio = true;

    public Sesion(UnCliente cliente) {
        this.cliente = cliente;
    }

    public Sesion() {
        this.cliente = null;
    }
    public void mostrarComandos() throws IOException{
        cliente.salida().writeUTF("/b {usr} -> bloquear un usuario\n/ub -> desbloquear usuarios\n/lb -> listar bloqueados\n@{usr/s} -> para enviar UM\n/exit -> para salir de la app");
    }
    public void mostrarMenu() throws IOException {
        while (menuInicio) {
            cliente.salida().writeUTF("""
                === Aplicacion de Mensajes ===
                1. Iniciar Sesion
                2. Registrarse
                3. Modo Invitado
                """);

            String opcion = cliente.entrada().readUTF();
            switch (opcion) {
                case "1":
                    String mensajito = iniciarSesion();
                    cliente.salida().writeUTF(mensajito);
                    if (mensajito.equals("sesion iniciada")) {
                        mostrarComandos();
                        sesionInvitado = false;
                        menuInicio = false;
                    }
                    break;
                case "2":
                    mensajito = registrarse();
                    cliente.salida().writeUTF(mensajito);
                    if (mensajito.equals("registro exitoso")) {
                        mostrarComandos();
                        sesionInvitado = false;
                        menuInicio = false;
                    }
                    break;
                case "3":
                    sesionInvitado = true;
                    cliente.salida().writeUTF("entraste en modo invitado.");
                    mostrarComandos();
                    menuInicio = false;
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
                return "sesion iniciada";
            }
            return "usr o psw incorrectos";

        }
        return "formato No valido";
    }

    private String registrarse() throws IOException {
        String cred[] = pedirCredenciales().split(" ");
        if (validarCredenciales(cred[0], cred[1])) {
            if (ud.registrarUsuario(new Usuario(cred[0], cred[1]))) {
                ServidorMulti.cambiarIdCliente(cliente.getId(), cred[0]);
                return "registro exitoso";
            }
            return "usuario ya registrado";
        }
        return "formato no valido\n";

    }

    public void cerrarSesion() throws IOException {
        cliente.salida().writeUTF("cerro sesion: "+cliente.getId());
        
        menuInicio = true;
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
    public void setInvitado(boolean invitado){
        this.sesionInvitado = invitado;
    }

    public int getMensajesRestantes() {
        return this.mensajesPrueba;
    }

    public void consumirMensaje() {
        this.mensajesPrueba--;
    }

    public void setMenuInicio(boolean menuInicio) {
        this.menuInicio = menuInicio;
    }

}
