package pd.ticketline.server.rmiconnection;

import pd.ticketline.rmi.BackupServiceRMI;

import java.io.*;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseBackupImpl extends UnicastRemoteObject implements  RemoteInterfaceServer {
    private static final String LOCK_FILE = "transfer.lock";
    private final BackupServiceRMI remoteRef;

    private static List<BackupServiceRMI> list;

    private static String dbDir;
    public DatabaseBackupImpl(String dbPath, BackupServiceRMI remoteRef) throws RemoteException {
        super();
        this.remoteRef=remoteRef;
        dbDir =  dbPath;
        list = new ArrayList<>();
    }

    public DatabaseBackupImpl() throws RemoteException {
        super();
        this.remoteRef=null;
    }

    @Override
    public void getDatabase() throws IOException, SQLException {
        File lockFile = new File(LOCK_FILE);
        lockFile.createNewFile();


        File databaseFile = new File(dbDir);
        byte[] databaseBytes = Files.readAllBytes(databaseFile.toPath());
        remoteRef.createDBCopy(databaseBytes);

        lockFile.delete();

    }

    @Override
    public void registerBackupService(BackupServiceRMI rmi) throws RemoteException {
        System.out.println("Adding listener - " + rmi);
        list.add(rmi);
    }

    @Override
    public void unregisterBackupService(BackupServiceRMI rmi) throws RemoteException {
        System.out.println("Removing listener - " + rmi);
        list.remove(rmi);

    }


    @Override
    public synchronized void updateDatabase(String query) {
        notifyListeners(query);
    }

    private synchronized void notifyListeners(String query) {
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
