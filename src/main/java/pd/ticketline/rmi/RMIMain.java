package pd.ticketline.rmi;

import pd.ticketline.server.rmiconnection.DatabaseBackupImpl;
import pd.ticketline.server.rmiconnection.RemoteInterfaceServer;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;

public class RMIMain {

    public static void main(String[] args) throws IOException, NotBoundException {

        if(args.length!=2){
            System.out.println("Not enough arguments\n");
        }
        String serverIP = args[0];
        String dbPath = args[1];
        Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        BackupServiceImpl backupService = new BackupServiceImpl(serverIP, dbPath);
        registry.rebind("RESERVAS", backupService);

        DatabaseBackupImpl databaseBackup = new DatabaseBackupImpl();
        Remote remoteService = Naming.lookup("rmi://"+serverIP+"/RESERVAS");
        BackupServiceRMI backupServiceRMI = (BackupServiceRMI) remoteService;

        databaseBackup.registerBackupService(backupServiceRMI);
    }


}
