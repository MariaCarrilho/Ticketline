package pd.ticketline.client.logic;

import java.io.IOException;
import java.net.HttpURLConnection;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import pd.ticketline.server.model.*;
import pd.ticketline.utils.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class APIRequests {
    private static String apiUrl;
    private static String serverIp;
    private String token;
    private boolean admin;
    private static Thread thread;
    private static WaitNotification waitNotification;
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

    private String parseJSon(Exception e) {
        if(e.getMessage().contains("401")) return ("Token expired, login again.");
        int startIndex = e.getMessage().indexOf("\"message\":\"") + "\"message\":\"".length();
        int endIndex = e.getMessage().indexOf("\"", startIndex);

        return e.getMessage().substring(startIndex, endIndex);
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
        }catch (Exception e){
            throw new Exception(parseJSon(e));

        }
    }

    public void getTCPPort() throws Exception {
        HttpHeaders headers = getAuthorizationHeader();
        ResponseEntity<String> response = restTemplate.exchange(apiUrl+ "/getPort"
                , HttpMethod.GET,new HttpEntity<>(headers),String.class);
        String port = response.getBody();
        try {
            waitNotification = new WaitNotification(serverIp, Integer.parseInt(port), token);
            thread = new Thread(waitNotification);
            thread.start();
        }catch (Exception e){
            throw new Exception(parseJSon(e));
        }

    }

    public String adicUser(String name ,String username, String password) throws Exception {
        User user = new User(username, name, password);
        try {
            user = restTemplate.postForObject(apiUrl + "/users/add", user, User.class);
            return user.toString();
        }catch (Exception e){
            throw new Exception(parseJSon(e));

        }
    }

    public String editUser(EditUser user) throws Exception {
        HttpHeaders headers = getAuthorizationHeader();
        try{
            ResponseEntity<User> response = restTemplate.exchange
                    (apiUrl+ "/users", HttpMethod.PUT,
                            new HttpEntity<>(user, headers),User.class);
            return response.getBody().toString();
        }catch (Exception e){
            throw new Exception(parseJSon(e));
        }
    }
    public String editShow(Show show) throws Exception {
        HttpHeaders headers = getAuthorizationHeader();
        try{
            ResponseEntity<Show> response = restTemplate.exchange(apiUrl+ "/shows", HttpMethod.PUT,new HttpEntity<>(show, headers),Show.class);
            return response.getBody().toString();
        }catch (Exception e){
            throw new Exception(parseJSon(e));

        }
    }

    public Integer adicShow(Show showInfo) throws Exception {
        HttpHeaders headers = getAuthorizationHeader();
        try{
            ResponseEntity<Show> response = restTemplate.exchange(apiUrl+ "/shows/add", HttpMethod.POST,new HttpEntity<>(showInfo, headers),Show.class);
            return response.getBody().getId();
        }catch (Exception e){
            throw new Exception(parseJSon(e));

        }
    }
    public String deleteShow(int id) throws Exception {
        HttpHeaders headers = getAuthorizationHeader();
        try{
            restTemplate.exchange(apiUrl+ "/shows/"+id, HttpMethod.DELETE,new HttpEntity<>(headers),Show.class);
            return "Show with id " + id + " deleted.";
        }catch (Exception e){
            throw new Exception(parseJSon(e));

        }
    }

    public String deleteUser(String username) throws Exception {
        HttpHeaders headers = getAuthorizationHeader();
        try{
            restTemplate.exchange(apiUrl+ "/users/"+username, HttpMethod.DELETE , new HttpEntity<>(headers), User.class);
            return "User with username " + username + " deleted.";
        }catch (Exception e){
            throw new Exception(parseJSon(e));

        }
    }

    public void adicSit(Sit sit) throws Exception {
        HttpHeaders headers = getAuthorizationHeader();
        try{
            restTemplate.exchange(apiUrl+ "/sits/add", HttpMethod.POST,new HttpEntity<>(sit, headers),Sit.class);
        }catch (Exception e){
            throw new Exception(parseJSon(e));

        }
    }

    public String deleteUnpaidReservation(int id) throws Exception {
        HttpHeaders headers = getAuthorizationHeader();
        try{
            restTemplate.exchange(apiUrl+ "/reservations/"+id, HttpMethod.DELETE,new HttpEntity<>(headers),Show.class);
            return "Reserva com id " + id + " apagada.";
        }catch (Exception e){
            throw new Exception(parseJSon(e));
        }
    }

    public ArrayList<Show> getShows() throws Exception {
        try {
            return new ArrayList<>(Arrays.asList(restTemplate.getForObject(apiUrl+ "/shows",Show[].class)));
        }catch (Exception e){
            throw new Exception(parseJSon(e));

        }
    }

    public ArrayList<Sit> getSits(int id) throws Exception {
        HttpHeaders headers = getAuthorizationHeader();
        try {
            ResponseEntity<Sit[]> responseEntity = restTemplate.exchange(apiUrl+ "/sits/"+id,HttpMethod.GET, new HttpEntity<>(headers) ,Sit[].class);
            return new ArrayList<>(Arrays.asList(responseEntity.getBody()));

        }catch (Exception e){
            throw new Exception(parseJSon(e));

        }
    }

    public ArrayList<Show> getCriteriaShows(Search search) throws Exception {
        try {
            return new ArrayList<>(Arrays.asList(Objects.requireNonNull(restTemplate.getForObject(apiUrl + "/shows/" + search.getCriterio() + "/" + search.getPesquisa(), Show[].class))));
        }catch (Exception e){
            throw new Exception(parseJSon(e));

        }
    }

    public ArrayList<SitsReservation> checkUnpaidReservations() throws Exception {
        try{
            HttpHeaders headers = getAuthorizationHeader();
            ResponseEntity<SitsReservation[]> response = restTemplate.exchange(
                apiUrl + "/reservations/unpaid",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                    SitsReservation[].class);
            return new ArrayList<>(Arrays.asList(Objects.requireNonNull(response.getBody())));
        }catch (Exception e){
            throw new Exception(parseJSon(e));

        }
    }

    public ArrayList<SitsReservation> checkPaidReservations() throws Exception {
        try{
            HttpHeaders headers = getAuthorizationHeader();
            ResponseEntity<SitsReservation[]> response = restTemplate.exchange(
                apiUrl + "/reservations/paid",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                    SitsReservation[].class);
        return new  ArrayList<>(Arrays.asList(Objects.requireNonNull(response.getBody())));
    }catch (Exception e){
            throw new Exception(parseJSon(e));
    }
    }


    public String payReservation(int id) throws Exception {
        HttpHeaders headers = getAuthorizationHeader();
        try{
            ResponseEntity<Reservation> response = restTemplate.exchange
                    (apiUrl+ "/reservations/"+id, HttpMethod.PUT,
                            new HttpEntity<>(headers),Reservation.class);
            return response.getBody().toString() + " foi paga.";
        }catch (Exception e){
            throw new Exception(parseJSon(e));

        }

    }

    public String bookSit(BookSit bookSit) throws Exception{
        HttpHeaders headers = getAuthorizationHeader();
        try {
            ResponseEntity<Reservation> response = restTemplate.exchange(apiUrl+ "/reservations", HttpMethod.POST,new HttpEntity<>(bookSit,headers), Reservation.class);
            return response.getBody().toString();
        }catch (Exception e){
            throw new Exception(parseJSon(e));

        }
    }

    public boolean isAdmin() {
        return admin;
    }


    public static void endThread() throws InterruptedException {
        waitNotification.active = false;
        thread.join(1000);
    }

    public ArrayList<User> getAllUsers() throws Exception {
        HttpHeaders headers = getAuthorizationHeader();
        try {
            return new ArrayList<>(Arrays.asList(restTemplate.exchange(apiUrl+ "/users", HttpMethod.GET ,new HttpEntity<>(headers), User[].class).getBody()));
        }catch (Exception e){
            throw new Exception(parseJSon(e));
        }
    }
}
