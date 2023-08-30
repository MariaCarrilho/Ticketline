package pd.ticketline.client;

import pd.ticketline.client.ui.ManagementUI;

public class ClientMain {
    public static void main(String[] args) throws Exception {
        if(args.length!=1){
            System.out.println("Not enough arguments\n");
            return;
        }
        String serverIP = args[0];

        ManagementUI ui = new ManagementUI(serverIP);
        if (ui.isServerAlive()) ui.init();
        else return;

        System.exit(0);



    }

}
