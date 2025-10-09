package servidorasincrono.ServidorMulti;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UnCliente implements Runnable {

    boolean sesionInvitado;
    int mensajesPrueba = 3;
    final DataOutputStream salida;
    final BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
    final DataInputStream entrada;
    String id;

    public UnCliente(Socket s, String id) throws IOException {
        this.salida = new DataOutputStream(s.getOutputStream());
        this.entrada = new DataInputStream(s.getInputStream());
        this.id = id;
        sesionInvitado = true;
    }

    public boolean iniciarSesion() throws IOException {
        UnCliente cliente = ServidorMulti.clientes.get(id);
        cliente.salida.writeUTF("Usr: \n");
        String usr = entrada.readUTF();
        cliente.salida.writeUTF("pws: \n");
        String psw = entrada.readUTF();
        if (usr.equals("admin") && psw.equals("admin")) {//buscar en base de datos etc
            id = usr;
            mensajesPrueba = 10000;
            return false;

        }
        return true;
    }

    public String getQuienes(String mensaje) {
        if (mensaje.startsWith("@")) {
            return mensaje.replace("@", "");
        }
        return mensaje;
    }

    public void enviarMensajeB(String mensaje) throws IOException {
        for (UnCliente c : ServidorMulti.clientes.values()) {
            if (c.id != id) {
                c.salida.writeUTF(id + " " + mensaje);
            }
        }
        mensajesPrueba--;
    }

    public void enviarMensajeUM(String quienes, String mensaje) throws IOException {
        UnCliente cliente;
        if (quienes.contains(",")) {//multicast
            String[] partes = quienes.split(",");
            for (String quien : partes) {
                cliente = ServidorMulti.clientes.get(quien);
                cliente.salida.writeUTF(id + "->" + mensaje);
            }
            mensajesPrueba--;
        }
        cliente = ServidorMulti.clientes.get(quienes);
        cliente.salida.writeUTF(id + "->" + mensaje);
        mensajesPrueba--;
    }

    @Override
    public void run() {
        UnCliente cliente = ServidorMulti.clientes.get(id);
        String opcion;

        try {

            while (true) {
                cliente.salida.writeUTF("Iniciar Sesion\n1.si\n2.no\n");
                opcion = entrada.readUTF();
                if (opcion.toLowerCase().equals("si")) {
                    sesionInvitado = iniciarSesion();
                }
                if (opcion.toLowerCase().equals("no")) {
                    break;
                }
            }
        } catch (IOException ex) {
            System.getLogger(UnCliente.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

        String mensaje;

        while (true) {

            try {//unicast o multicast
                cliente = ServidorMulti.clientes.get(id);
                cliente.salida.writeUTF("Mensaje restantes: " + mensajesPrueba);
                mensaje = entrada.readUTF();
                if (mensaje.startsWith("@") && (mensajesPrueba > 0 || !sesionInvitado)) {
                    cliente.salida.writeUTF("en UM...");
                    String[] partes = mensaje.split(" ");
                    enviarMensajeUM(getQuienes(partes[0]), partes[1]);
                    cliente.salida.writeUTF("mensaje enviado a : " + getQuienes(partes[0]));
                    continue;
                }
                //broadcast
                cliente.salida.writeUTF("en broadcast...");
                enviarMensajeB(mensaje);

                if (sesionInvitado) {
                    cliente = ServidorMulti.clientes.get(id);
                    if (mensajesPrueba == 0) {
                        cliente.salida.writeUTF("prueba agotada, por favor inicie sesion...!\n");
                        sesionInvitado = iniciarSesion();
                        if (sesionInvitado) {
                            cliente.salida.writeUTF("credenciales incorrectas...\n");
                        }
                    }

                }

            } catch (Exception ex) {
            }
        }
    }
}
