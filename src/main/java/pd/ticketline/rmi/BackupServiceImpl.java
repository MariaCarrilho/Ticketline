package pd.ticketline.rmi;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackupServiceImpl extends UnicastRemoteObject implements BackupServiceRMI {

    private final String serverIP;
    private String dbPath;
    private Connection dbConn;


    public BackupServiceImpl(String serverIP, String dbPath) throws RemoteException {
        super();
        this.serverIP=serverIP;
        this.dbPath = dbPath;
    }


    @Override
    public void createDBCopy(byte[] dbBytes) throws IOException, SQLException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dataHoraFormatada = dateFormat.format(new Date());

        File destino = new File(dbPath + "/backup_"+ dataHoraFormatada+ ".db");
        dbPath = destino.getPath();
        try (FileOutputStream fileOutputStream = new FileOutputStream(destino)) {
            fileOutputStream.write(dbBytes);
        }
        this.dbConn = DriverManager.getConnection("jdbc:sqlite:"+dbPath);

    }

    @Override
    public void databaseUpdated(String query) throws RemoteException, SQLException {
        Statement statement = dbConn.createStatement();
        statement.execute(query);
        statement.close();
        System.out.println("Base de dados atualizada");
    }

    @Override
    public void registerBackupService(BackupServiceRMI backupServiceRMI) {

    }

    private void close() throws SQLException
    {
        if (dbConn != null)
            dbConn.close();
    }

}
