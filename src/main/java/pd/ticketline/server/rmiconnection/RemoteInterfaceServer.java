package pd.ticketline.server.rmiconnection;

import pd.ticketline.rmi.DatabaseListener;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;

public interface RemoteInterfaceServer extends Remote {
    byte[] getDatabase() throws IOException, SQLException;
    void registerBackupService(DatabaseListener rmi) throws RemoteException;
    void unregisterBackupService(DatabaseListener rmi) throws RemoteException, SQLException;
    void notifyListeners(String query) throws RemoteException;
}
