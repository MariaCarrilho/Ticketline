package pd.ticketline.utils;

public class EditUser {
    private String name;
    private String password;

    public EditUser(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }
}
