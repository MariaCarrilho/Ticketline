package pd.ticketline.client.logic;

import java.io.IOException;
import java.net.HttpURLConnection;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pd.ticketline.server.model.Reservation;
import pd.ticketline.server.model.User;
import pd.ticketline.utils.*;
import pd.ticketline.server.model.Show;
import pd.ticketline.server.model.Sit;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class APIRequests {
    private static String apiUrl;
    private static String serverIp;
    private String token;
    private boolean admin;
    private Thread waitNotification;
    public final static ArrayList<UnbookedReservations> unbookedReservations = new ArrayList<>();
    private final RestTemplate restTemplate;

    public APIRequests(String serverIP) {
        this.restTemplate = new RestTemplate();
        APIRequests.serverIp = serverIP;
        APIRequests.apiUrl = "http://"+serverIP+":8080";
    }

    public boolean isServerAlive(){
        int timeout = 3000;
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(timeout);
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            return true;
        } catch (IOException e) {
            System.out.println("Server is not running. Goodbye!");
            return false;
        }
    }

    private String parseJSon(Exception e) throws Exception {
        int startIndex = e.getMessage().indexOf('{');
        String jsonPart = e.getMessage().substring(startIndex);
        JSONObject jsonObject = new JSONObject(jsonPart);
        String msg = jsonObject.getString("message");
        if(msg.equals("Invalid or expired token."))
            throw new Exception("Token expired, login again.");
        else if(msg.equals("This user was deleted."))
            throw new Exception(msg);
        return jsonObject.getString("message");
    }
    private HttpHeaders getAuthorizationHeader(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer "+token);
        return headers;
    }

    public String auth(String username, String password) throws Exception {
        LoginUser loginUser = new LoginUser(username, password);
        try{
            Auth auth = restTemplate.postForObject(apiUrl+"/users/auth",loginUser, Auth.class);
            this.token = auth.getToken();
            this.admin = auth.isAdmin();
            return "You are now authenticated!";
        }catch (HttpClientErrorException e){
            return parseJSon(e);
        }
    }

    public void getTCPPort() {
        HttpHeaders headers = getAuthorizationHeader();
        ResponseEntity<String> response = restTemplate.exchange(apiUrl+ "/getPort", HttpMethod.GET,new HttpEntity<>(headers),String.class);
        String port = response.getBody();
        try {
            this.waitNotification = new Thread(new WaitNotification(serverIp, Integer.parseInt(port)));
            this.waitNotification.start();
            WaitNotification.active = true;
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

    public String adicUser(String name ,String username, String password) throws Exception {
        User user = new User(username, name, password);
        try {
            user = restTemplate.postForObject(apiUrl + "/users/add", user, User.class);
            return user.toString();
        }catch (Exception e){
            return parseJSon(e);
        }
    }

    public String editUser(EditUser user) throws Exception {
        HttpHeaders headers = getAuthorizationHeader();
        try{
            ResponseEntity<User> response = restTemplate.exchange(apiUrl+ "/users", HttpMethod.PUT,new HttpEntity<>(user, headers),User.class);
            return response.getBody().toString();
        }catch (Exception e){
            return parseJSon(e);
        }
    }
    public String editShow(Show show) throws Exception {
        HttpHeaders headers = getAuthorizationHeader();
        try{
            System.out.println(show.toString());
            ResponseEntity<Show> response = restTemplate.exchange(apiUrl+ "/shows", HttpMethod.PUT,new HttpEntity<>(show, headers),Show.class);
            return response.getBody().toString();
        }catch (Exception e){
            return parseJSon(e);
        }
    }

    public Integer adicShow(Show showInfo) throws Exception {
        HttpHeaders headers = getAuthorizationHeader();
        try{
            ResponseEntity<Show> response = restTemplate.exchange(apiUrl+ "/shows/add", HttpMethod.POST,new HttpEntity<>(showInfo, headers),Show.class);
            return response.getBody().getId();
        }catch (Exception e){
            System.out.println(parseJSon(e));
            return -1;
        }
    }
    public String deleteShow(int id) throws Exception {
        HttpHeaders headers = getAuthorizationHeader();
        try{
            restTemplate.exchange(apiUrl+ "/shows/"+id, HttpMethod.DELETE,new HttpEntity<>(headers),Show.class);
            return "Show with id " + id + " deleted.";
        }catch (Exception e){
            return parseJSon(e);
        }
    }

    public String deleteUser(String username) throws Exception {
        HttpHeaders headers = getAuthorizationHeader();
        try{
            restTemplate.exchange(apiUrl+ "/users/"+username, HttpMethod.DELETE , new HttpEntity<>(headers), User.class);
            return "User with username " + username + " deleted.";
        }catch (Exception e){
            return parseJSon(e);
        }
    }

    public void adicSit(Sit sit) throws Exception {
        HttpHeaders headers = getAuthorizationHeader();
        try{
            restTemplate.exchange(apiUrl+ "/sits/add", HttpMethod.POST,new HttpEntity<>(sit, headers),Sit.class);
        }catch (Exception e){
            System.out.println(parseJSon(e));
        }
    }

    public String deleteUnpaidReservation(int id) throws Exception {
        HttpHeaders headers = getAuthorizationHeader();
        try{
            restTemplate.exchange(apiUrl+ "/reservations/"+id, HttpMethod.DELETE,new HttpEntity<>(headers),Show.class);
            return "Reserva com id " + id + "apagada.";
        }catch (Exception e){
            return parseJSon(e);
        }
    }

    public ArrayList<Show> getShows() throws Exception {
        try {
            return new ArrayList<>(Arrays.asList(restTemplate.getForObject(apiUrl+ "/shows",Show[].class)));
        }catch (Exception e){
            System.out.println(parseJSon(e));
            return new ArrayList<>();
        }
    }

    public ArrayList<Sit> getSits(int id) {
        HttpHeaders headers = getAuthorizationHeader();
        try {
            ResponseEntity<Sit[]> responseEntity = restTemplate.exchange(apiUrl+ "/sits/"+id,HttpMethod.GET, new HttpEntity<>(headers) ,Sit[].class);
            return new ArrayList<>(Arrays.asList(responseEntity.getBody()));

        }catch (Exception e){
            return new ArrayList<>();
        }
    }

    public ArrayList<Show> getCriteriaShows(Search search){
        try {
            return new ArrayList<>(Arrays.asList(Objects.requireNonNull(restTemplate.getForObject(apiUrl + "/shows/" + search.getCriterio() + "/" + search.getPesquisa(), Show[].class))));
        }catch (Exception e){
            return new ArrayList<>();
        }
    }

    public ArrayList<Reservation> checkUnpaidReservations() throws Exception {
        try{
            HttpHeaders headers = getAuthorizationHeader();
            ResponseEntity<Reservation[]> response = restTemplate.exchange(
                apiUrl + "/reservations/unpaid",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Reservation[].class);
            return new ArrayList<>(Arrays.asList(Objects.requireNonNull(response.getBody())));
        }catch (Exception e){
            System.out.println(parseJSon(e));
            return new ArrayList<>();
        }
    }

    public ArrayList<Reservation> checkPaidReservations() throws Exception {
        try{
            HttpHeaders headers = getAuthorizationHeader();
            ResponseEntity<Reservation[]> response = restTemplate.exchange(
                apiUrl + "/reservations/paid",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Reservation[].class);
        return new  ArrayList<>(Arrays.asList(Objects.requireNonNull(response.getBody())));
    }catch (Exception e){
        System.out.println(parseJSon(e));
        return new ArrayList<>();
    }
    }


    public String payReservation(int id) throws Exception {
        HttpHeaders headers = getAuthorizationHeader();
        try{
            ResponseEntity<Reservation> response = restTemplate.exchange(apiUrl+ "/reservations/"+id, HttpMethod.GET,new HttpEntity<>(headers), Reservation.class);
            return response.getBody().toString() + " foi paga.";
        }catch (Exception e){
            return parseJSon(e);
        }

    }

    public String bookSit(BookSit bookSit) throws Exception{
        HttpHeaders headers = getAuthorizationHeader();
        try {
            ResponseEntity<Reservation> response = restTemplate.exchange(apiUrl+ "/reservations", HttpMethod.POST,new HttpEntity<>(bookSit,headers), Reservation.class);
            return response.getBody().toString();
        }catch (Exception e){
            return parseJSon(e);
        }
    }

    public boolean isAdmin() {
        return admin;
    }

    public void endThread() throws InterruptedException {
        WaitNotification.active = false;
        this.waitNotification.join();
    }

    public ArrayList<User> getAllUsers() throws Exception {
        HttpHeaders headers = getAuthorizationHeader();
        try {
            return new ArrayList<>(Arrays.asList(restTemplate.exchange(apiUrl+ "/users", HttpMethod.GET ,new HttpEntity<>(headers), User[].class).getBody()));
        }catch (Exception e){
            System.out.println(parseJSon(e));
            return new ArrayList<>();
        }
    }
}
