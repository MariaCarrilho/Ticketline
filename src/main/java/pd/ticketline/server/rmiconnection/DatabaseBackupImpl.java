package pd.ticketline.server.rmiconnection;

import pd.ticketline.rmi.DatabaseListener;

import java.io.*;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DatabaseBackupImpl extends UnicastRemoteObject implements RemoteInterfaceServer {

    private static List<DatabaseListener> list;
    private static String dbDir;
    public DatabaseBackupImpl(String dbPath) throws RemoteException {
        super();
        dbDir =  dbPath;
        list = new ArrayList<>();
    }

    public DatabaseBackupImpl() throws RemoteException {
        super();
    }

    @Override
    public byte[] getDatabase() throws IOException {

        File databaseFile = new File(dbDir);
        return Files.readAllBytes(databaseFile.toPath());
    }

    public synchronized void registerBackupService
            (DatabaseListener rmi) throws RemoteException {
        System.out.println("Adding listener - " + rmi);
        list.add(rmi);
    }

    public synchronized void unregisterBackupService
            (DatabaseListener rmi) throws RemoteException, SQLException {
        System.out.println("Removing listener - " + rmi);
        rmi.endListener();
        list.remove(rmi);
    }

    public void unregisterAllServices() throws RemoteException, SQLException {
        List<DatabaseListener> copyOfList = new ArrayList<>(list);
        for (DatabaseListener databaseListener : copyOfList) {
            unregisterBackupService(databaseListener);
        }
    }

    public synchronized void notifyListeners(String query) {
        System.out.println(list.size());
        for(int i =0; i<list.size();i++){
            try{
                list.get(i).databaseUpdated(query);
            }catch (RemoteException | SQLException e){
                System.out.println("Removing listener");
                list.remove(i--);
            }
        }
    }

}
