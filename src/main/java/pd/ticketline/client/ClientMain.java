package pd.ticketline.client;

import pd.ticketline.client.ui.ManagementUI;
public class ClientMain {

    public static void main(String[] args) throws Exception {
        if(args.length!=1){
            System.out.println("Not enough arguments\n");
        }
        String serverIP = args[0];

        ManagementUI ui = new ManagementUI(serverIP);
        ui.init();

    }

}
