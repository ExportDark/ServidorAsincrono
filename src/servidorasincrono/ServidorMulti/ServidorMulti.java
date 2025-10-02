package servidorasincrono.ServidorMulti;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import servidorasincrono.ServidorMulti.UnCliente;

public class ServidorMulti {
    static HashMap<String,UnCliente> clientes = new HashMap<>();
    public static void main(String[] args) throws IOException {
        ServerSocket servidorSocket = new ServerSocket(8080);
        int contador = 0;
        String cadenita = "0";
        int algo1 = Integer.valueOf(cadenita);
        int algo2 = Integer.parseInt(cadenita);
        while (true) {            
            Socket s = servidorSocket.accept();
            UnCliente unCliente = new UnCliente(s);
            Thread hilo = new Thread(unCliente);
            clientes.put(Integer.toString(contador), unCliente);
            hilo.start();
            
            contador++;
        }
        
    }
    
}
