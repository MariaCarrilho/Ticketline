package pd.ticketline.utils;

public enum Endpoints {
    USER_ADD("/user/add"),
    USER_UPDATE("/user/update"),
    AUTH("/auth"),
    GET_PORT("/getPort"),
    SHOW_ADD("/show/add");
    private final String endpoint;

    Endpoints(String path) {
        this.endpoint = path;
    }

}
