package pd.ticketline.server.clientconnection;

import pd.ticketline.utils.UnbookedReservations;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler {
    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket){
        this.clientSocket = clientSocket;
    }
    public void sendMessage(String message) throws IOException {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            oos.writeObject(message);

        } catch (IOException e) {
            removeClient();
        }
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void removeClient() throws IOException {
        clientSocket.close();
    }

    public void sendMessage(UnbookedReservations unbookedReservations) throws IOException {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            oos.writeObject(unbookedReservations);

        } catch (IOException e) {
            System.out.println("Removing Client");
            removeClient();
        }
    }
}
