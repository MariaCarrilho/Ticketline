package pd.ticketline.rmi;

import java.io.IOException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;

public interface BackupServiceRMI extends Remote {
    void createDBCopy(byte[] dbBytes) throws IOException, SQLException;
    void databaseUpdated(String query) throws RemoteException, SQLException;

    void registerBackupService(BackupServiceRMI backupServiceRMI);


}
