package pd.ticketline.rmi;


import pd.ticketline.server.rmiconnection.RemoteInterfaceServer;

import java.io.IOException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;

public class RMIMain {
    public static void main(String[] args) throws IOException, NotBoundException, SQLException {

        if(args.length!=2){
            System.out.println("Not enough arguments\n");
        }
        String serverIP = args[0];
        String dbPath = args[1];
        try {
            Registry r = LocateRegistry.getRegistry(serverIP, Registry.REGISTRY_PORT);
            RemoteInterfaceServer remoteRef = (RemoteInterfaceServer) r.lookup("RESERVAS");
            DatabaseListenerImpl backupService = new DatabaseListenerImpl(dbPath, remoteRef);
            backupService.createDBCopy();
            remoteRef.registerBackupService(backupService);
            while (backupService.isActive()){}
            System.out.println("Server was shut down. Goodbye!");
            System.exit(0);
        }catch (ConnectException e){
            System.out.println("Can't connect to registry. Check if server is on.");
        }


    }


}
