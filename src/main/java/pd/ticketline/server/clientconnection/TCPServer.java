package pd.ticketline.server.clientconnection;

import pd.ticketline.utils.UnbookedReservations;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public  class  TCPServer implements Runnable{
    private static int port;
    private static int id = 1;

    private static final Map<Integer, ClientHandler> connectedClients = new HashMap<>();

    public static void sendMessageToAllClients(UnbookedReservations unbookedReservations) throws IOException {
        for(Map.Entry<Integer, ClientHandler> entry : connectedClients.entrySet()){
            ClientHandler clientHandler = entry.getValue();
            clientHandler.sendMessage(unbookedReservations);
        }
    }

    @Override
    public void run() {

        try {
            ServerSocket serverSocket = new ServerSocket(0);
            port = serverSocket.getLocalPort();
            while (true){
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                connectedClients.put(id, clientHandler);
                id++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessageToAllClients(String message) throws IOException {
        for(Map.Entry<Integer, ClientHandler> entry : connectedClients.entrySet()){
            ClientHandler clientHandler = entry.getValue();
            clientHandler.sendMessage(message);
        }
    }

    public static int getPort() {
        return port;
    }

    private void removeClient(int clientId) throws IOException {
        if(connectedClients.containsKey(clientId)){
            connectedClients.get(clientId).removeClient();
            connectedClients.remove(clientId);
        }

    }
}
