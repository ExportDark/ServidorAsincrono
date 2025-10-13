package servidorasincrono.ServidorMulti;

import Datos.UsuarioDao;
import Dominio.Usuario;
import com.sun.source.tree.ContinueTree;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class UnCliente implements Runnable {

    boolean sesionInvitado;
    int mensajesPrueba = 3;
    final DataOutputStream salida;
    final BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
    final DataInputStream entrada;
    String id;
    UsuarioDao ud = new UsuarioDao();

    public UnCliente(Socket s, String id) throws IOException {
        this.salida = new DataOutputStream(s.getOutputStream());
        this.entrada = new DataInputStream(s.getInputStream());
        this.id = id;
        sesionInvitado = false;
    }

    public String pedirCredenciales() throws IOException {
        UnCliente cliente = ServidorMulti.clientes.get(id);
        cliente.salida.writeUTF("Usr: ");
        String usr = entrada.readUTF();
        cliente.salida.writeUTF("pws: ");
        String psw = entrada.readUTF();
        return usr + " " + psw;
    }

    public boolean validarCredenciales(String usr, String psw) {
        return usr.length() > 8 && psw.length() > 8 && usr.matches("^[a-zA-Z0-9_]+$") && psw.matches("^[a-zA-Z0-9_]+$");
    }

    public boolean registrarse(String usr, String psw) throws IOException {
        return ud.registrarUsuario(new Usuario(usr, psw));
    }

    public boolean iniciarSesion(String usr, String psw) throws IOException {
        return ud.iniciarSesion(new Usuario(usr, psw));
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

    public void menuBienvenida() {
        UnCliente cliente = ServidorMulti.clientes.get(id);
        String opcion;
        try {

            while (true) {
                cliente.salida.writeUTF("===Aplicacion de Mensjes===\n1.Iniciar Sesion\n2.Registrarse\n3.Modo Invitado\n");
                opcion = entrada.readUTF();
                if (opcion.equals("1")) {
                    String partes[] = pedirCredenciales().split(" ");

                    if (validarCredenciales(partes[0], partes[1])) {
                        sesionInvitado = !iniciarSesion(partes[0], partes[1]);
                        break;
                    }
                }
                if (opcion.equals("2")) {
                    String partes[] = pedirCredenciales().split(" ");
                    cliente.salida.writeUTF(partes[0] + " " + partes[1]);
                    cliente.salida.writeUTF("validacion: " + validarCredenciales(partes[0], partes[1]));
                    if (validarCredenciales(partes[0], partes[1])) {
                        sesionInvitado = !registrarse(partes[0], partes[1]);
                        break;
                    }
                }
                if (opcion.equals("3")) {
                    cliente.salida.writeUTF("S I "+sesionInvitado);
                    if (sesionInvitado) {
                        continue;
                        //no hace nada.jpg
                    } else {
                        sesionInvitado = true;
                        break;
                    }

                }
            }
        } catch (IOException ex) {
            System.getLogger(UnCliente.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    @Override
    public void run() {
        menuBienvenida();
        String mensaje;
        while (true) {
            UnCliente cliente;
            cliente = ServidorMulti.clientes.get(id);
            try {//unicast o multicast
                if (sesionInvitado) {
                    cliente.salida.writeUTF("Mensaje restantes: " + mensajesPrueba);
                    if (mensajesPrueba == 0) {
                        cliente.salida.writeUTF("prueba agotada...!\n");
                        menuBienvenida();
                    }
                }
                mensaje = entrada.readUTF();
                if (mensaje.startsWith("@") && (mensajesPrueba > 0 || !sesionInvitado)) {
                    String[] partes = mensaje.split(" ");
                    enviarMensajeUM(getQuienes(partes[0]), partes[1]);
                    cliente.salida.writeUTF("mensaje enviado a : " + getQuienes(partes[0]));
                    continue;
                }
                //broadcast
                cliente.salida.writeUTF("en broadcast...");
                enviarMensajeB(mensaje);

            } catch (Exception ex) {
            }
        }
    }
}
