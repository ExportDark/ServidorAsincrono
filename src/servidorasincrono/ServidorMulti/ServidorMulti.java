package servidorasincrono.ServidorMulti;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import servidorasincrono.ServidorMulti.UnCliente;

public class ServidorMulti {
    static HashMap<String,UnCliente> clientes = new HashMap<>();
    static int contador;
    public static void main(String[] args) throws IOException {
        ServerSocket servidorSocket = new ServerSocket(8080);
            contador = 0;
        while (true) {            
            Socket s = servidorSocket.accept();
            UnCliente unCliente = new UnCliente(s,Integer.toString(contador));
            Thread hilo = new Thread(unCliente);
            clientes.put(Integer.toString(contador), unCliente);
            hilo.start();
            System.out.println("Cliente: "+contador);
            contador++;
        }
        
    }
    
}
