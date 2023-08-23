package pd.ticketline.server.clientconnection;

import pd.ticketline.utils.UnbookedReservations;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class TCPServer implements Runnable{
    private static int port;
    private static int id = 1;
    public static volatile boolean active = true;

    private static final Map<Integer, ClientHandler> connectedClients = new HashMap<>();
    private static ServerSocket serverSocket;

    public TCPServer() {}

    public static void stop() throws IOException {
        active = false;
        serverSocket.close();
    }

    @Override
    public void run() {

        try {
            serverSocket = new ServerSocket(0);
            port = serverSocket.getLocalPort();
            while (active){
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                connectedClients.put(id, clientHandler);
                id++;
            }

        } catch (IOException e) {
            active=false;
        }
    }

    public void sendMessageToAllClients(String message) throws IOException {
        for(Map.Entry<Integer, ClientHandler> entry : connectedClients.entrySet()){
            try {
                ClientHandler clientHandler = entry.getValue();
                clientHandler.sendMessage(message);
            }catch (IOException e){
                System.out.println("Error sending message to client");
                removeClient(entry.getKey());
            }

        }
    }

    public static int getPort() {
        return port;
    }

    public void removeClient(int clientId) throws IOException {
        if(connectedClients.containsKey(clientId)){
            connectedClients.get(clientId).removeClient();
            connectedClients.remove(clientId);
        }
    }

    public void sendMessageToAllClients(UnbookedReservations unbookedReservations) throws IOException {
        for(Map.Entry<Integer, ClientHandler> entry : connectedClients.entrySet()){
            ClientHandler clientHandler = entry.getValue();
            clientHandler.sendMessage(unbookedReservations);
        }
    }


}
