package pd.ticketline.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Auth {
    private String token;
    private boolean admin;

    public Auth(){}

    @JsonCreator
    public Auth(@JsonProperty("token") String token, @JsonProperty("admin") boolean admin) {
        this.token = token;
        this.admin = admin;
    }

   public String getToken() {
        return token;
    }
    public boolean isAdmin() {
        return admin;
    }
}
