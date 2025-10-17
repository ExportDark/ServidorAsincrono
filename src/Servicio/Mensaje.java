package Servicio;

import Datos.BloqueoDao;
import Datos.UsuarioDao;
import Dominio.Bloqueo;
import java.io.IOException;
import java.util.List;
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
        //cambiar a switch
        if (!sesion.esInvitado() || sesion.getMensajesRestantes() > 0) {
            if (mensaje.split(" ")[0].equals("/b")) {
                cliente.salida().writeUTF(bloquearUsuario(mensaje));
                return;
            }
            if (mensaje.split(" ")[0].equals("/ub")) {
                cliente.salida().writeUTF(desbloquearUsuario(mensaje));
                return;
            }
            if (mensaje.split(" ")[0].equals("/lb")) {
                cliente.salida().writeUTF(listarBloqueados(mensaje));
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
    private String listarBloqueados(String mensaje){
        BloqueoDao x = new BloqueoDao();
        List<Bloqueo> lista = x.listarBloqueados(new Bloqueo(cliente.getId(), ""));
        StringBuilder bloqueados = new StringBuilder();
        for(Bloqueo b : lista){
            bloqueados.append(b.getPablo()+"\n");
        }
        return bloqueados.toString();
    }
    private String desbloquearUsuario(String mensaje) {
        String partes[] = mensaje.split(" ");
        BloqueoDao x = new BloqueoDao();
        Bloqueo b = new Bloqueo(cliente.getId(), partes[1]);
        UsuarioDao ud = new UsuarioDao();
        if (x.estaBloqueado(b)) {
            if (ud.existeUsuario(partes[1]) != null) {
                x.desbloquear(new Bloqueo(cliente.getId(), partes[1]));
                return "usuario desbloqueado " + partes[1];
            }
            return "usuario no existe";
        }
        return "usuario no esta bloqueado";

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
                //excepto gente bloqueada
                if (!new BloqueoDao().comuniacionBloqueada(new Bloqueo(cliente.getId(), c.getId()))) {
                    c.salida().writeUTF(cliente.getId() + "-> " + mensaje);
                }
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
