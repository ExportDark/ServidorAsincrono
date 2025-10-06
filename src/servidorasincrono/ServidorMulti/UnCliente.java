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
    final String id;

    public UnCliente(Socket s,String id) throws IOException {
        this.salida = new DataOutputStream(s.getOutputStream());
        this.entrada = new DataInputStream(s.getInputStream());
        this.id = id;
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
                    cliente.salida.writeUTF(id+" "+mensaje);
                }else{
                    for (UnCliente cliente : ServidorMulti.clientes.values()) {
                        if (cliente.id!=id) {
                            cliente.salida.writeUTF(id+" "+mensaje);
                        }
                    }
                }
            } catch (Exception ex) {
            }                               
        }
    }

}
