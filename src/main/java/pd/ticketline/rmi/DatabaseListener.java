package pd.ticketline.rmi;

import java.io.IOException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;

public interface DatabaseListener extends Remote {
    void createDBCopy() throws IOException, SQLException;
    void databaseUpdated(String query) throws RemoteException, SQLException;

    void endListener() throws RemoteException, SQLException;

}
