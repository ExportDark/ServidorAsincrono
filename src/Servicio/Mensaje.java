package Servicio;

import Datos.BloqueoDao;
import Datos.UsuarioDao;
import Dominio.Bloqueo;
import java.io.IOException;
import servidorasincrono.ServidorMulti.ServidorMulti;
import servidorasincrono.ServidorMulti.UnCliente;

public class Mensaje {

    private final UnCliente cliente;
    private final Sesion sesion;

    public Mensaje(UnCliente cliente, Sesion sesion) {
        this.cliente = cliente;
        this.sesion = sesion;
    }

    public void procesarMensaje(String mensaje) throws IOException {
        if (!sesion.esInvitado() || sesion.getMensajesRestantes() > 0) {
            if (mensaje.split(" ")[0].equals("/b")) {
                bloquearUsuario(mensaje);
                sesion.consumirMensaje();
                return;
            }
            if (mensaje.startsWith("@")) {
                enviarUMcast(mensaje);
                sesion.consumirMensaje();
                return;
            }
            if (mensaje.split(" ")[0].toLowerCase().equals("/exit")) {
                sesion.cerrarSesion();
                return;
            }
            enviarBroadcast(mensaje);
            sesion.consumirMensaje();
            return;
        }
        cliente.salida().writeUTF("prueba agotada");
        sesion.setMenuInicio(true);
        sesion.setInvitado(true);
    }

    private String bloquearUsuario(String mensaje) {
        String partes[] = mensaje.split(" ");
        BloqueoDao x = new BloqueoDao();
        Bloqueo b = new Bloqueo(cliente.getId(), partes[1]);
        UsuarioDao ud = new UsuarioDao();
        if (!x.estaBloqueado(b)) {
            if (ud.existeUsuario(partes[1]) != null) {
                x.bloquear(new Bloqueo(cliente.getId(), partes[1]));
                return "usuario bloqueado " + partes[1];
            }
            return "usuario no existe";
        }
        return "usuario ya bloqueado";

    }

    private void enviarUMcast(String mensaje) throws IOException {
        String quienes = getQuienes(mensaje);
        UnCliente destino;
        if (quienes.contains(",")) {//multicast
            String[] partes = quienes.split(",");
            for (String quien : partes) {
                destino = ServidorMulti.clientes.get(quien);
                destino.salida().writeUTF(cliente.getId() + "-> " + mensaje);
            }

        }
        destino = ServidorMulti.clientes.get(quienes);
        destino.salida().writeUTF(cliente.getId() + "-> " + mensaje);
    }

    private void enviarBroadcast(String mensaje) throws IOException {
        for (UnCliente c : ServidorMulti.clientes.values()) {
            if (!c.getId().equals(cliente.getId())) {
                c.salida().writeUTF(cliente.getId() + "-> " + mensaje);
            }
        }
    }

    private String getQuienes(String mensaje) {
        if (mensaje.startsWith("@")) {
            return mensaje.replace("@", "");
        }
        return mensaje;
    }
}
