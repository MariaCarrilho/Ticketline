package pd.ticketline.rmi;

import pd.ticketline.server.rmiconnection.RemoteInterfaceServer;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseListenerImpl extends UnicastRemoteObject implements DatabaseListener {

    private String dbPath;
    private Connection dbConn;
    private final RemoteInterfaceServer remoteRef;

    public DatabaseListenerImpl(String dbPath, RemoteInterfaceServer remoteRef) throws RemoteException {
        super();
        this.dbPath = dbPath;
        this.remoteRef = remoteRef;
    }


    @Override
    public void createDBCopy() throws IOException, SQLException {

        byte[] dbBytes = remoteRef.getDatabase();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dataHoraFormatada = dateFormat.format(new Date());

        File destino = new File(dbPath + "/backup_"+ dataHoraFormatada+ ".db");
        dbPath = destino.getPath();
        try (FileOutputStream fileOutputStream = new FileOutputStream(destino)) {
            fileOutputStream.write(dbBytes);
        }
        this.dbConn = DriverManager.getConnection("jdbc:sqlite:"+dbPath);
        if(dbConn!=null) System.out.println("Db is connected");

    }

    @Override
    public void databaseUpdated(String query) throws RemoteException {
        try {
            Statement statement = this.dbConn.createStatement();
            statement.execute(query);
            statement.close();
            System.out.println("Base de dados atualizada");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void endListener() throws RemoteException, SQLException {
        close();
        UnicastRemoteObject.unexportObject(remoteRef, true);
        System.exit(0);
    }


    private void close() throws SQLException
    {
        if (dbConn != null)
            dbConn.close();
    }

}
