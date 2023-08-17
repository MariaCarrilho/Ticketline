package pd.ticketline.server.rmiconnection;

import pd.ticketline.rmi.BackupServiceRMI;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;

public interface RemoteInterfaceServer extends Remote {

    public void getDatabase() throws IOException, SQLException;
    void registerBackupService(BackupServiceRMI rmi) throws RemoteException;
    void unregisterBackupService(BackupServiceRMI rmi) throws RemoteException;


    void updateDatabase(String query);
}
