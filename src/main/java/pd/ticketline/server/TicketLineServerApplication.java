package pd.ticketline.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.PatchMapping;
import pd.ticketline.server.clientconnection.TCPServer;
import pd.ticketline.server.rmiconnection.RegisterHandler;
import pd.ticketline.server.rmiconnection.RemoteInterfaceServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Scanner;

@SpringBootApplication
@EnableJpaRepositories
public class TicketLineServerApplication extends SpringBootServletInitializer {
    private static String getValidDatabaseFile(String arg){
        String dbDir = arg;
        Path path = Paths.get(dbDir);
        if (Files.isDirectory(path)){
            File[] filesInDirectory = new File(dbDir).listFiles();
            if (filesInDirectory != null) {
                for (File file : filesInDirectory) {
                    if (file.isFile() && file.getName().endsWith(".db")) {
                        dbDir = file.getAbsolutePath();
                        System.out.println("Using existing database at: " + dbDir);
                        return dbDir;
                    }
                }
            }
            dbDir = path + "\\server_ticketline_0.db";
            System.out.println("Creating database at " + dbDir );
            return dbDir;
        }
        else if (new File(dbDir).getName().endsWith(".db")) {
            System.out.println("Connected to "+ dbDir);
            return dbDir;
        }
        return null;
    }
    public static void main(String[] args) throws IOException, InterruptedException {

        SpringApplication app = new SpringApplication(TicketLineServerApplication.class);

        args[0] = args[0].replace("--spring.datasource.url=jdbc:sqlite:", "");
        String validDatabase = getValidDatabaseFile(args[0]);
        if(validDatabase==null) return;

        args[0] = "--spring.datasource.url=jdbc:sqlite:"+validDatabase;
        ConfigurableApplicationContext context = app.run(args);

        RegisterHandler registerHandler = new RegisterHandler(validDatabase);
        TCPServer tcpServer = new TCPServer();

        Thread serverThread = new Thread(tcpServer);
        serverThread.start();

        Scanner sc= new Scanner(System.in);
        System.out.print("Enter a string: ");
        sc.nextLine();

        context.close();
        tcpServer.sendMessageToAllClients("Server was terminated.", null);
        TCPServer.stop();
        serverThread.join();
        registerHandler.deleteRegistry();
        System.exit(0);


    }


}
