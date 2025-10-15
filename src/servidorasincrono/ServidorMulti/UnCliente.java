package servidorasincrono.ServidorMulti;

import Datos.BloqueoDao;
import Datos.UsuarioDao;
import Dominio.Bloqueo;
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
        if (usr.startsWith(" ")||psw.startsWith(" ")||usr.isBlank()||psw.isBlank()) {//empieza con espacio o esta vacio
            return "vacio vacio";
        }
        return usr + " " + psw;
    }

    public boolean validarCredenciales(String usr, String psw) {
        return usr.length() >= 3 && psw.length() >= 5 && usr.matches("^[a-zA-Z0-9_]+$") && psw.matches("^[a-zA-Z0-9_]+$");
    }

    public boolean registrarse(String usr, String psw) throws IOException {
        return ud.registrarUsuario(new Usuario(usr, psw));
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
        String partes[];
        try {

            while (true) {
                cliente.salida.writeUTF("===Aplicacion de Mensjes===\n1.Iniciar Sesion\n2.Registrarse\n3.Modo Invitado\n");
                opcion = entrada.readUTF();
                if (opcion.equals("1")) {
                        partes = pedirCredenciales().split(" ");
                        cliente.salida.writeUTF("Credenciales: "+partes[0]+"_"+partes[1]);
                    if (validarCredenciales(partes[0], partes[1])) {
                        sesionInvitado = !ud.iniciarSesion(new Usuario(partes[0], partes[1]));
                        ServidorMulti.cambiarIdCliente(id, partes[0]);
                        cliente.salida.writeUTF("sesion invitado: "+sesionInvitado);
                        cliente.salida.writeUTF("/b {user} -> para bloquear un usuario");
                        break;
                    }
                }
                if (opcion.equals("2")) {
                        partes = pedirCredenciales().split(" ");
                    cliente.salida.writeUTF(partes[0] + " " + partes[1]);
                    cliente.salida.writeUTF("validacion: " + validarCredenciales(partes[0], partes[1]));
                    if (validarCredenciales(partes[0], partes[1])) {
                        sesionInvitado = !registrarse(partes[0], partes[1]);
                        id = partes[0];
                        cliente.salida.writeUTF("/b {user} -> para bloquear un usuario");
                        break;
                    }
                }
                if (opcion.equals("3")) {
                    cliente.salida.writeUTF("S I " + sesionInvitado);
                    if (sesionInvitado) {
                        continue;
                        //no hace nada.jpg
                    } else {
                        sesionInvitado = true;
                        cliente.salida.writeUTF("/b {user} -> para bloquear un usuario");
                        break;
                    }

                }
                cliente.salida.writeUTF("bucle infinito D:");
            }
        } catch (IOException ex) {
            System.getLogger(UnCliente.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    @Override
    public void run() {
        menuBienvenida();
        String mensaje;
        UnCliente cliente = ServidorMulti.clientes.get(id);
        String partes[];
        while (true) {
            try {//unicast o multicast
                cliente.salida.writeUTF("id: "+id);
                if (sesionInvitado) {
                    cliente.salida.writeUTF("Mensaje restantes: " + mensajesPrueba);
                    if (mensajesPrueba == 0) {
                        cliente.salida.writeUTF("prueba agotada...!\n");
                        menuBienvenida();
                    }
                }
                mensaje = entrada.readUTF();
                //aqui para bloquear gente ↓
                    partes = mensaje.split(" ");
                if (partes[0].equals("/b")) {
                    BloqueoDao x = new BloqueoDao();
                    Bloqueo b = new Bloqueo(id, partes[1]);
                    if (ud.verificarExiste(partes[1]) != null && !x.estaBloqueado(b)) {
                        x.bloquear(b);
                        cliente.salida.writeUTF("usuario bloqueado...\n");
                        continue;
                    }if (x.estaBloqueado(b)) {
                        cliente.salida.writeUTF("usuario ya estaba bloqueado...\n");
                    }
                      cliente.salida.writeUTF("usuario no encontrado...\n");
                      continue;
                }
                if (mensaje.startsWith("@") && (mensajesPrueba > 0 || !sesionInvitado)) {
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
