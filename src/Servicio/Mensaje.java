package Servicio;

import Datos.BloqueoDao;
import Dominio.Bloqueo;
import java.io.IOException;
import servidorasincrono.ServidorMulti.ServidorMulti;
import servidorasincrono.ServidorMulti.UnCliente;

public class Mensaje {

    private final UnCliente cliente;

    public Mensaje(UnCliente cliente) {
        this.cliente = cliente;
    }

    public void procesarMensaje(String mensaje) {
        if (mensaje.startsWith(mensaje)) {

        }
    }

    public void bloquearUsuario(String mensaje) {
        String partes[] = mensaje.split(" ");
        BloqueoDao x = new BloqueoDao();
        x.bloquear(new Bloqueo(cliente.getId(), partes[1]));
    }

    public void enviarUMcast(String mensaje) throws IOException {
        String quienes = getQuienes(mensaje);
        UnCliente destino;
        if (quienes.contains(",")) {//multicast
            String[] partes = quienes.split(",");
            for (String quien : partes) {
                destino = ServidorMulti.clientes.get(quien);
                destino.salida().writeUTF(cliente.getId() + "-> " + mensaje);
            }
            //mensajesPrueba--;
        }
        destino = ServidorMulti.clientes.get(quienes);
        destino.salida().writeUTF(cliente.getId() + "-> " + mensaje);
        //mensajesPrueba--;
    }

    public void enviarBroadcast(String mensaje) throws IOException {
        for (UnCliente c : ServidorMulti.clientes.values()) {
            if (!c.getId().equals(cliente.getId())) {
                c.salida().writeUTF(cliente.getId() + "-> " + mensaje);
            }
        }
    }

    public String getQuienes(String mensaje) {
        if (mensaje.startsWith("@")) {
            return mensaje.replace("@", "");
        }
        return mensaje;
    }
}
