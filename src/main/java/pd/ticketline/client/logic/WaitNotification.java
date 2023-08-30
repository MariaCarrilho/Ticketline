package pd.ticketline.client.logic;

import pd.ticketline.client.ui.ManagementUI;
import pd.ticketline.utils.UnbookedReservations;

import java.io.*;
import java.net.Socket;

public class WaitNotification implements Runnable {
    private final String serverIP;
    private final int port;
    public static boolean active;
    private final String token;

    public WaitNotification(String serverIP, int port, String token) {
        this.serverIP = serverIP;
        this.port = port;
        active = true;
        this.token = token;
    }

    @Override
    public void run() {
        try {
            UnbookedReservations unbooked = null;
            Socket clientSocket = new Socket(serverIP, port);
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            oos.writeObject(token);
            while (active) {
                if (clientSocket.getInputStream().available() > 0) {
                    ObjectInputStream ois =
                            new ObjectInputStream(clientSocket.getInputStream());
                    Object receivedObject = ois.readObject();
                    if (receivedObject instanceof String response) {
                        System.out.println("\n"+response+"\n");
                        if(response.equals("Server was terminated.")){
                            active = false;
                            ManagementUI.close();
                        }
                        if(response.equals("Este lugar está indisponível."))
                            APIRequests.unbookedReservations.remove(unbooked);

                    } else if (receivedObject instanceof UnbookedReservations booked) {
                        unbooked=booked;
                        System.out.println(booked);

                    } else {
                        System.out.println("Received unknown object type: " + receivedObject.getClass());
                    }
                }
            }

            clientSocket.close();
            System.out.println("Connection closed.");
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
