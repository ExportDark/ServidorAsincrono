package servidorasincrono.ServidorMulti;

import Datos.BloqueoDao;
import Datos.UsuarioDao;
import Dominio.Bloqueo;
import Dominio.Usuario;
import Servicio.Mensaje;
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
    private final Mensaje mensaje;
    UsuarioDao ud = new UsuarioDao();

    public UnCliente(Socket socket, String id) throws IOException {
        this.socket = socket;
        this.salida = new DataOutputStream(socket.getOutputStream());
        this.entrada = new DataInputStream(socket.getInputStream());
        this.id = id;
        sesion = new Sesion(this);
        mensaje = new Mensaje(this, sesion);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public DataInputStream entrada() {
        return this.entrada;
    }

    public DataOutputStream salida() {
        return this.salida;
    }

    @Override
    public void run() {
        UnCliente cliente = ServidorMulti.clientes.get(id);
        try {
            while (true) {
                sesion.mostrarMenu();
                String mensajito = entrada().readUTF();
                this.mensaje.procesarMensaje(mensajito);
            }

        } catch (IOException ex) {
            System.getLogger(UnCliente.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
}
