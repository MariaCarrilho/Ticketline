package pd.ticketline.utils;

public class Auth {
    private String token;
    private boolean admin;

    public Auth(String token, boolean admin) {
        this.token = token;
        this.admin = admin;
    }

}
