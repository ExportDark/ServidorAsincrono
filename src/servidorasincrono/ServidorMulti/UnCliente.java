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

    final DataOutputStream salida;
    final BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
    final DataInputStream entrada;

    public UnCliente(Socket s) throws IOException {
        this.salida = new DataOutputStream(s.getOutputStream());
        this.entrada = new DataInputStream(s.getInputStream());
    }


    @Override
    public void run() {
        String mensaje;
        while (true) {            
            try {
                mensaje = entrada.readUTF();
                
                if (mensaje.startsWith("@")) {
                    String[] partes= mensaje.split(" ");
                    String aQuien = partes[0].substring(1);
                    UnCliente cliente = ServidorMulti.clientes.get(aQuien);
                    cliente.salida.writeUTF(mensaje);
                }else{
                    for (UnCliente cliente : ServidorMulti.clientes.values()) {
                        cliente.salida.writeUTF(mensaje);
                    }
                }
            } catch (Exception ex) {
            }                               
        }
    }

}
