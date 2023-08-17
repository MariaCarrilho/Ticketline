package pd.ticketline.client.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import pd.ticketline.utils.UnbookedReservations;
import pd.ticketline.server.model.Show;
import pd.ticketline.server.model.Sit;
import pd.ticketline.utils.BookSit;
import pd.ticketline.utils.EditUser;
import pd.ticketline.utils.Search;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class APIRequests {
    private static String apiUrl;
    private static String serverIp;
    private String token;
    private boolean admin;
    private Thread waitNotification;
    private String username;
    public final static ArrayList<UnbookedReservations> unbookedReservations = new ArrayList<>();


    public APIRequests(String serverIP) {
        APIRequests.serverIp = serverIP;
        APIRequests.apiUrl = "http://"+serverIP+":8080";
    }

    public String getConnectionResponse(String jsonInput, String endpoint, String req, boolean auth, String token) throws Exception {
        HttpURLConnection con = postConnection(jsonInput, endpoint, req, auth, token);
        int statusCode = con.getResponseCode();
        if(statusCode==401){
            throw new Exception("Token expired, login again.");
        }
        else if (statusCode == 200 && !req.equals("DELETE")) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String responseBody = reader.readLine();
                return responseBody.trim();
            }
        } else{
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(con.getErrorStream()))) {
                JSONObject errorJson = new JSONObject(errorReader.readLine());
                return errorJson.getString("message");

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    private static HttpURLConnection postConnection(String jsonInput, String endpoint, String req, boolean auth, String token) throws IOException {
        String reqUrl = apiUrl + endpoint;
        URL url = new URL(reqUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(req);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        if(auth){
            String authorization = "Bearer " + token;
            con.setRequestProperty("Authorization", authorization);
        }
        if(req.equals("POST") || req.equals("PUT")) {
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }
        con.connect();
        return con;
    }

    public String auth(String username, String password, String token) throws Exception {
        String jsonInput = "{ \"username\": \"" +  username + "\", \"password\": \"" + password + "\" }";

        String responseBody = getConnectionResponse(jsonInput, "/auth", "POST", false, token);
        try{
            JSONObject jsonObject = new JSONObject(responseBody);
            this.token = jsonObject.getString("token");
            this.admin = jsonObject.getBoolean("admin");
            this.username = username;
            return "You are now authenticated!";
        }catch (Exception e){
            return responseBody;
        }

    }

    public String getTCPPort(String token) throws Exception {

        String port = getConnectionResponse(null, "/getPort", "GET", true, token);
        if(port.length()>5)
            return "Unable to connect to TCP port";
        else{
            this.waitNotification = new Thread(new WaitNotification(serverIp, Integer.parseInt(port)));
            this.waitNotification.start();
            return "Ready to receive notifications";
        }

    }

    public String adicUser(String name ,String username, String password) throws Exception {
            String jsonInput = "{ \"username\": \"" +  username + "\", \"password\": \"" + password + "\", \"name\": \"" + name + "\" }";
            return getConnectionResponse(jsonInput, "/user/add", "POST", false, null);
    }

    public String editUser(EditUser user) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String object = mapper.writeValueAsString(user);
        return this.getConnectionResponse(object, "/user/update", "PUT", true, token);
    }
    public String editShow(Show show) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String object = mapper.writeValueAsString(show);
        return this.getConnectionResponse(object, "/shows", "PUT", true, token);
    }
    public Integer adicShow(Show showInfo) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String object = mapper.writeValueAsString(showInfo);
        String responseBody = this.getConnectionResponse(object, "/shows/add", "POST", true, token);
        try{
            JSONObject jsonObject = new JSONObject(responseBody);
            return jsonObject.getInt("id");
        } catch (JSONException e) {
            return -1;
        }
    }

    public void adicSit(Sit sit) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String object = mapper.writeValueAsString(sit);

        getConnectionResponse(object, "/sit/add", "POST", true, token);
    }

    public String deleteShow(int id) throws Exception {
        return getConnectionResponse(null, "/shows/" + id, "DELETE", true, token);
    }

    public String deleteUnpaidReservation(int id) throws Exception {
        return getConnectionResponse(null, "/reservation/" + id, "DELETE", true, token);

    }

    public ArrayList<Show> getShows() throws Exception {

        String responseBody = getConnectionResponse(null, "/shows", "GET", false, null);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(responseBody, new TypeReference<>() {});
    }

    public ArrayList<Sit> getSits(int id) throws Exception {
        String responseBody = getConnectionResponse(null, "/sits/"+id, "GET", true, token);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(responseBody, new TypeReference<>() {});
    }

    public ArrayList<Show> getCriteriaShows(Search search) throws Exception {
        String responseBody = getConnectionResponse(null, "/shows/"+search.getCriterio()+"/"+search.getPesquisa(), "GET", false, null);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(responseBody, new TypeReference<>() {});
    }

    public String[] checkUnpaidReservations() throws Exception {
        return getConnectionResponse(null, "/reservation/unpaid", "GET", true, token).replace("\"","")
                .replace("[", "").replace("]", "").split(",");
    }

    public String[] checkPaidReservations() throws Exception {
        return getConnectionResponse(null, "/reservation/paid", "GET", true, token).replace("\"","")
                .replace("[", "").replace("]", "").split(",");
    }

    public String payReservation(int id) throws Exception {
        return getConnectionResponse(null, "/reservation/" +id, "GET", true, token);
    }

    public String bookSit(BookSit bookSit) throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        String object = mapper.writeValueAsString(bookSit);

        return getConnectionResponse(object, "/reservation", "POST", true, token);
    }



    public boolean isAdmin() {
        return admin;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public void endThread() throws InterruptedException {
        WaitNotification.active = false;
        this.waitNotification.join();
    }

}
