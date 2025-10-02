package servidorasincrono.ServidorMulti;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class UnCliente implements Runnable{
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
                for(UnCliente cliente :ServidorMulti.clientes.values()){
                    cliente.salida.writeUTF(mensaje);
                }
            } catch (Exception e) {
            }
        }
    }
    
    
}
