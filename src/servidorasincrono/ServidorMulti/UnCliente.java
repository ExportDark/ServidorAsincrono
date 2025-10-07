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

    int mensajesPrueba = 3;
    final DataOutputStream salida;
    final BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
    final DataInputStream entrada;
    final String id;

    public UnCliente(Socket s, String id) throws IOException {
        this.salida = new DataOutputStream(s.getOutputStream());
        this.entrada = new DataInputStream(s.getInputStream());
        this.id = id;
    }

    public boolean iniciarSesion() throws IOException {
        UnCliente cliente = ServidorMulti.clientes.get(id);
        cliente.salida.writeUTF("Usr: \n");
        String usr = entrada.readUTF();
        cliente.salida.writeUTF("pws: \n");
        String psw = entrada.readUTF();
        if (usr.equals("admin") && psw.equals("admin")) {//buscar en base de datos etc
            mensajesPrueba = 10000;
            return false;

        }
        return true;
    }

    @Override
    public void run() {
        UnCliente cliente = ServidorMulti.clientes.get(id);
        String opcion;
        boolean sesionInvitado = true;

        try {
            cliente.salida.writeUTF("Iniciar Sesion\n1.si\n2.no\n");
            opcion = entrada.readUTF();
            if (opcion.equals("si")) {
                sesionInvitado = iniciarSesion();
            }
        } catch (IOException ex) {
            System.getLogger(UnCliente.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

        String mensaje;

        while (true) {
            try {
                mensaje = entrada.readUTF();

                //unicast
                if (mensaje.startsWith("@")) {
                    String[] partes = mensaje.split(" ");
                    String aQuien = partes[0].substring(1);
                    cliente = ServidorMulti.clientes.get(aQuien);
                    if (mensajesPrueba > 0 || !sesionInvitado) {
                        cliente.salida.writeUTF(id + " " + mensaje);
                        mensajesPrueba--;
                        continue;
                    }

                }

                //broadcast
                if (mensajesPrueba > 0 || !sesionInvitado) {
                    for (UnCliente c : ServidorMulti.clientes.values()) {
                        if (c.id != id) {
                            c.salida.writeUTF(id + " " + mensaje);
                        }
                    }
                    mensajesPrueba--;
                }

                if (sesionInvitado) {
                    cliente = ServidorMulti.clientes.get(id);
                    cliente.salida.writeUTF("te quedan " + mensajesPrueba + " mensajes");
                    if (mensajesPrueba == 0) {
                        cliente.salida.writeUTF("prueba agotada, por favor inicie sesion...!\n");
                        sesionInvitado = iniciarSesion();
                        if (sesionInvitado) {
                            cliente.salida.writeUTF("credenciales incorrectas...\n");
                            continue;
                        }
                    }

                }

            } catch (Exception ex) {
            }
        }
    }
}
