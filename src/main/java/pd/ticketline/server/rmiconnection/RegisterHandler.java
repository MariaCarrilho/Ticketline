package pd.ticketline.server.rmiconnection;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RegisterHandler {
    private final Registry registry;
    private DatabaseBackupImpl databaseBackup;

    public RegisterHandler(String dbPath) throws RemoteException{
        this.registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        this.databaseBackup = new DatabaseBackupImpl(dbPath);
        registry.rebind("RESERVAS", this.databaseBackup);
    }

    public void deleteRegistry() {
        try{
            databaseBackup.unregisterAllServices();
            registry.unbind("RESERVAS");
        }catch (Exception e){
            System.out.println("Error unbinding registry");
        }
    }

}
