package pd.ticketline.client.logic;

import pd.ticketline.utils.UnbookedReservations;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class WaitNotification implements Runnable {
    private String serverIP;
    private int port;
    static boolean active = true;

    public WaitNotification(String serverIP, int port) {
        this.serverIP = serverIP;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            Socket clientSocket = new Socket(serverIP, port);
            UnbookedReservations unbooked = null;
            while (active) {
                if (clientSocket.getInputStream().available() > 0) {
                    ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                    Object receivedObject = ois.readObject();
                    if (receivedObject instanceof String response) {
                        System.out.println(response);
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
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
