package servidorasincrono.ServidorMulti;

import Datos.BloqueoDao;
import Datos.UsuarioDao;
import Dominio.Bloqueo;
import Dominio.Usuario;
import Servicio.Sesion;
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

    private final Socket socket;
    private final DataInputStream entrada;
    private final DataOutputStream salida;
    private String id;
    private final Sesion sesion;
    UsuarioDao ud = new UsuarioDao();

    public UnCliente(Socket socket, String id) throws IOException {
        this.socket = socket;
        this.salida = new DataOutputStream(socket.getOutputStream());
        this.entrada = new DataInputStream(socket.getInputStream());
        this.id = id;
        sesion = new Sesion(this);
    }
    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return this.id;
    }
    public DataInputStream entrada(){
        return this.entrada;
    }
    public DataOutputStream salida(){
        return this.salida;
    }

    @Override
    public void run() {
        try {
            sesion.mostrarMenu();
        } catch (IOException ex) {
            System.getLogger(UnCliente.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
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
