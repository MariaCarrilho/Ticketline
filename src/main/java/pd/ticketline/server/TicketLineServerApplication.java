package pd.ticketline.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import pd.ticketline.server.clientconnection.TCPServer;
import pd.ticketline.rmi.BackupServiceRMI;
import pd.ticketline.server.rmiconnection.DatabaseBackupImpl;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;

@SpringBootApplication
@EnableJpaRepositories
public class TicketLineServerApplication extends SpringBootServletInitializer {

    public static void main(String[] args) throws IOException, NotBoundException, SQLException {

        if(args.length!=1) {
            System.out.println("Not enough arguments\n");
            return;
        }
        String commandLine = args[0];
        String dbDir = commandLine.replace("--spring.datasource.url=jdbc:sqlite:", "");

        Registry r = LocateRegistry.getRegistry("127.0.0.1", Registry.REGISTRY_PORT);
        BackupServiceRMI remoteRef = (BackupServiceRMI) r.lookup("RESERVAS");

        DatabaseBackupImpl databaseBackup = new DatabaseBackupImpl(dbDir, remoteRef);
        databaseBackup.getDatabase();

        Thread serverThread = new Thread(new TCPServer());
        serverThread.start();

        SpringApplication.run(TicketLineServerApplication.class, args);


    }


}
