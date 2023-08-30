package pd.ticketline.server.clientconnection;

import pd.ticketline.utils.UnbookedReservations;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TCPServer implements Runnable{
    private static int port;
    public static volatile boolean active = true;

    private static final Map<String, ClientHandler> connectedClients = new HashMap<>();
    private static ServerSocket serverSocket;
    public TCPServer() {}

    @Override
    public void run() {

        try {
            serverSocket = new ServerSocket(0);
            port = serverSocket.getLocalPort();
            while (active){
                Socket clientSocket = serverSocket.accept();
                ObjectInputStream ois =
                        new ObjectInputStream(clientSocket.getInputStream());
                String token = (String) ois.readObject();
                ClientHandler clientHandler =
                        new ClientHandler(clientSocket);
                connectedClients.put(token, clientHandler);
            }

        } catch (IOException e) {
            active=false;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessageToAllClients(String message, String token) {
        for(Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()){
            try {
                if(!Objects.equals(entry.getKey(), token)) {
                    ClientHandler clientHandler = entry.getValue();
                    clientHandler.sendMessage(message);
                }
            }catch (IOException e){
                System.out.println("Error sending message to client");
                removeClient(entry.getKey());
            }

        }
    }
    public static void stop() throws IOException {
        active = false;
        serverSocket.close();
    }
    public static int getPort() {
        return port;
    }

    public static void removeClient(String token) {
        try {
            if (connectedClients.containsKey(token)) {
                connectedClients.get(token).removeClient();
                connectedClients.remove(token);
            }
        }catch (Exception e){
            System.out.println("Error removing client");
        }
    }

    public void sendMessageToAllClients(UnbookedReservations unbookedReservations, String token) throws IOException {
        for(Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()){
            if(!Objects.equals(entry.getKey(), token)) {
                ClientHandler clientHandler = entry.getValue();
                clientHandler.sendMessage(unbookedReservations);
            }

        }
    }


}
