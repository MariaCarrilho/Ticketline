package pd.ticketline.client.ui;

import pd.ticketline.client.logic.APIRequests;
import pd.ticketline.client.logic.ReadFile;
import pd.ticketline.utils.UnbookedReservations;
import pd.ticketline.server.model.Show;
import pd.ticketline.server.model.Sit;
import pd.ticketline.utils.BookSit;
import pd.ticketline.utils.EditUser;
import pd.ticketline.utils.Search;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManagementUI {

    private boolean finish = false;
    private boolean auth = false;
    private final APIRequests apiRequests;

    public ManagementUI(String apiURL) {
        this.apiRequests = new APIRequests(apiURL);
    }

    public void init() throws Exception {

        System.out.println("┌────────────────────────────────────────────────┐");
        System.out.println("|                   TicketLine                   |");
        System.out.println("|                                                |");
        System.out.println("|  Trabalho realizado por:                       |");
        System.out.println("|     Maria Carrilho - 2019148211                |");
        System.out.println("└────────────────────────────────────────────────┘");
        while(!finish) {
            switch (Input.chooseOption("======== Menu ========",
                    "Adicionar Utilizador","Consultar Espetáculo","Autenticação","Terminar Programa")) {
                case 1 -> adicUser();
                case 2 -> consShow();
                case 3 -> login();
                case 4 -> finish = true;
            }
        }
        System.out.println("\nA terminar a aplicacao...\n");
    }

    public void normalOptions() throws Exception {

        while(auth) {
            switch (Input.chooseOption("======== Menu Autenticado ========",
                    "Editar Utilizador","Consultar Espetáculo","Selecionar Espetáculo", "Reservar Lugares", "Reservas pagas","Reservas não pagas","Sair")) {
                case 1 -> editUser(); //editar user autenticado
                case 2 -> consShow(); // consultar espetaculos
                case 3 -> selectSits(); // selecionar espetaculos futuros
                // , ver os respetivos lugares e preços
                case 4 -> bookChoosenSits();
                case 5 -> checkPaidReservations(); //pagas
                case 6 -> checkUnpaidReservations(); // n pagas, apagar
                case 7 -> logout(); // n pagas, apagar
            }
        }
    }

    public void adminOptions() throws Exception{

        while(auth) {
            switch (Input.chooseOption("======== Menu Administrador ========","Consultar Espetáculo",
                    "Inserir Espetáculo", "Alterar Espetáculo","Apagar Espetáculo", "Sair")) {
                case 1 -> consShow(); // consultar espetaculos
                case 2 -> insertShow();
                case 3 -> alterShow();
                case 4 -> deleteShow();
                case 5 -> logout();
            }
        }
    }



    private void bookChoosenSits(){
        try{
            int i = 0;
            if (APIRequests.unbookedReservations.isEmpty()) System.out.println("You have nothing in your cart.");
            else {
                for (UnbookedReservations unbooked : APIRequests.unbookedReservations) {
                    System.out.println("Reserva: " + i + " -> " + unbooked.toString());
                    i++;
                }
                String reserva = Input.readString("Reservar? ", true);
                if (reserva.equals("sim")) {
                    int id = Input.readInt("Número da Reserva: ");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                    String formattedDate = dateFormat.format(new Date());
                    BookSit bookSit = new BookSit(APIRequests.unbookedReservations.get(id).getFila(), APIRequests.unbookedReservations.get(id).getAssento(), APIRequests.unbookedReservations.get(id).getShow_id(), formattedDate);
                    System.out.println(this.apiRequests.bookSit(bookSit));
                    APIRequests.unbookedReservations.remove(id);
                }
            }
        }catch (Exception e){
            if(e.getMessage().equals("Token expired, login again.")){
                System.out.println(e.getMessage());
                auth=false;
            }
        }
    }

    private void selectSits(){
        try{
            List<Show> availableShows = checkAvailableShows();

            if (availableShows.isEmpty()) System.out.println("No shows available for reservation.");
            else {
                for (Show entity : availableShows) System.out.println(entity);
                int id = Input.readInt("Show id:");
                ArrayList<Sit> sits = this.apiRequests.getSits(id);
                String fila = "";
                HashMap<String, ArrayList<String>> filaLugares = new HashMap<>();
                for (Sit sit : sits) {
                    if (!Objects.equals(sit.getFila(), fila)) {
                        fila = sit.getFila();
                        System.out.println();
                        System.out.print(fila);
                        filaLugares.put(fila, new ArrayList<>());
                    }
                    filaLugares.get(fila).add(sit.getAssento());
                    System.out.print(sit);
                }
                System.out.println();

                String reserva = Input.readString("Adicionar ao cesto? ", true);
                if (reserva.equalsIgnoreCase("sim")) {
                    String filaReservar = null;
                    String assentoReservar = null;
                    do {
                        filaReservar = Input.readString("Fila:", true);
                    } while (!filaLugares.containsKey(filaReservar));
                    do {
                        assentoReservar = Input.readString("Assento:", true);
                    } while (!filaLugares.get(filaReservar).contains(assentoReservar));
                    APIRequests.unbookedReservations.add(new UnbookedReservations(filaReservar, assentoReservar, id));
                }
            }
        }catch (Exception e){
            if(e.getMessage().equals("Token expired, login again.")){
                System.out.println(e.getMessage());
                auth=false;
            }
        }


    }

    private void checkPaidReservations(){
        try{
            String[] parts = this.apiRequests.checkPaidReservations();
            if (parts[0] == null || parts[0].trim().isEmpty()) System.out.println("You don't have paid bookings.");
            else {
                for (String part : parts) System.out.println(part);
            }
        }catch (Exception e){
            if(e.getMessage().equals("Token expired, login again.")){
                System.out.println(e.getMessage());
                auth=false;
            }
        }
    }

    private void checkUnpaidReservations(){
        try{
            String[] parts = this.apiRequests.checkUnpaidReservations();
            if (parts[0] == null || parts[0].trim().isEmpty()) System.out.println("You don't have unpaid bookings.");
            else {
                for (String part : parts) System.out.println(part);
                switch (Input.chooseOption("======== Opções ========", "Eliminar Reserva",
                        "Pagar Reserva", "Cancelar")) {
                    case 1 -> deleteUnpaidReservation();
                    case 2 -> payReservation();
                    case 3 -> {}
                }
            }
        }catch (Exception e){
            if(e.getMessage().equals("Token expired, login again.")){
                System.out.println(e.getMessage());
                auth=false;
            }
        }
    }

    private void deleteUnpaidReservation() {
        try{
            int id = Input.readInt("Id da Reserva:");
            System.out.println(this.apiRequests.deleteUnpaidReservation(id));
        }catch (Exception e){
            if(e.getMessage().equals("Token expired, login again.")){
                System.out.println(e.getMessage());
                auth=false;
            }
        }
    }

    private void payReservation() {
        try{
            int id = Input.readInt("Id da Reserva:");
            System.out.println(this.apiRequests.payReservation(id));
        }catch (Exception e){
            if(e.getMessage().equals("Token expired, login again.")){
                System.out.println(e.getMessage());
                auth=false;
            }
        }
    }
    private void editShowVisibility(){
        try {
            consAllShow();

            int id = Input.readInt("Show id:");
            Optional<Show> foundShow = this.apiRequests.getShows().stream().filter(show -> show.getId() == id).findFirst();
            if (foundShow.isEmpty()) System.out.println("This show was deleted!");
            else {
                Show show = new Show(foundShow.get());

                show.setVisivel(show.getVisivel() == 0 ? 1 : 0);

                System.out.println(this.apiRequests.editShow(show));
            }
        }catch (Exception e){
            if(e.getMessage().equals("Token expired, login again.")){
                System.out.println(e.getMessage());
                auth=false;
            }
        }
    }

    private void editAllShowInfo(){
        try{
            String dateHourRegex = "\\d+-\\d+-\\d+ \\d+:\\d+";
            Pattern dateHourPattern = Pattern.compile(dateHourRegex);
            String dataHora;
            if(consAllShow()==0) return;
            int id =0;
            while(true) {
                id = Input.readInt("Show id:");
                int finalId = id;
                Optional<Show> foundShow = this.apiRequests.getShows().stream().filter(show -> show.getId() == finalId).findFirst();
                if (foundShow.isEmpty()) System.out.println("This show id doesn't exist.\nPlease insert one of the numbers above.");
                else break;
            };
            String descricao = Input.readString("Descrição: ", false);
            String tipo = Input.readString("Tipo: ", false);
            Integer duracao = Input.readInt("Duração: ");
            while (true) {
                dataHora = Input.readString("Data (Ex:12-12-2023 12:20): ", false);
                Matcher dateMatcher = dateHourPattern.matcher(dataHora);
                if (!dateMatcher.matches()) System.out.println("Invalid format!");
                else break;
            }
            String local = Input.readString("Local: ", false);
            String localidade = Input.readString("Localidade: ", false);
            String pais = Input.readString("País: ", false);
            String classificacao_etaria = Input.readString("Classificação Etária: ", true);
            System.out.println("1 para visível; 0 para invisível");
            Integer visivel = Input.readInt("Visibilidade: ");

            Show show = new Show(id, descricao, tipo, dataHora, duracao, local, localidade, pais, classificacao_etaria, visivel);
            String response = this.apiRequests.editShow(show);
            System.out.println(response);
        }catch (Exception e){
            if(e.getMessage().equals("Token expired, login again.")){
                System.out.println(e.getMessage());
                auth=false;
            }
        }
    }
    public String criterioConsulta(){
        switch (Input.chooseOption("======== Critério de Consulta ========",
                "Descrição","Tipo","Data/Hora","Duração","Local", "Localidade", "País", "Classificação Etária", "Todos")) {
            case 1 -> {
                return "descricao";
            }
            case 2 -> {
                return "tipo";
            }
            case 3 -> {
                return "data_hora";
            }
            case 4 -> {
                return "duracao";
            }
            case 5 -> {
                return "local";
            }
            case 6 -> {
                return "localidade";
            }
            case 7 -> {
                return "pais";
            }
            case 8 -> {
                return "classificacao_etaria";
            }
            case 9 -> {
                return "all";
            }
        }
        return "";
    }
    private void consShow() throws Exception {
        String criterio = criterioConsulta();
        if(criterio.equals("all")) consAllShow();
        else{
            String pesquisa = Input.readString("Pesquisar por: ",false);
            Search search = new Search(criterio, pesquisa);
            showShows(this.apiRequests.getCriteriaShows(search));
        }
    }
    private void adicUser() throws Exception {
        int numAlunos = Input.readInt("\nQuantos utilizadores pretende criar? ");
        for (int i = 0 ; i < numAlunos; i++) {
            System.out.println();
            String nome = Input.readString("Nome: ",false);
            String username = Input.readString("Username: ",true);
            String password = Input.readString("Password: ",true);
            System.out.println();
            System.out.println("Foi criado o utilizador " + apiRequests.adicUser(nome, username, password));
        }
    }
    private void logout() throws InterruptedException {
        auth = false;
        this.apiRequests.endThread();
    }
    private void editUser() {
        try{
            String name = Input.readString("Nome: ", false);
            String password = Input.readString("Password: ", true);
            System.out.println("Foi alterado o utilizador " + this.apiRequests.editUser(new EditUser(name, password)));
        }catch (Exception e){
            if(e.getMessage().equals("Token expired, login again.")){
                System.out.println(e.getMessage());
                auth=false;
            }
        }
    }

    private void login() throws Exception {

        String username = Input.readString("Username: ",true);
        String password = Input.readString("Password: ",true);
        String response = apiRequests.auth(username, password, apiRequests.getToken());
        System.out.println(response);
        if(response.equals("You are now authenticated!")) {
            auth = true;
            apiRequests.getTCPPort(apiRequests.getToken());
            if(apiRequests.isAdmin()) adminOptions();
            else normalOptions();
        }
    }

    private int consAllShow() throws Exception {
        System.out.println("These are the shows available:");
        return showShows(this.apiRequests.getShows());
    }
    private int showShows(ArrayList<Show> showEntities){
        if (showEntities.isEmpty()){
            System.out.println("No shows found.");
            return 0;
        }else {
            for (Show entity : showEntities) {
                if (entity.getVisivel() == 1 || apiRequests.isAdmin())
                    System.out.println(entity);
            }
            return 1;
        }
    }
    private void alterShow() {
        switch (Input.chooseOption("======== Alterar Espetáculo ========","Alterar Detalhes","Alterar Visibilidade", "Cancelar")) {
            case 1 -> editAllShowInfo();
            case 2 -> editShowVisibility();
            case 3 -> {}
        }
    }
    private void insertShow() {
        try{
            ReadFile readFile = new ReadFile(apiRequests);
            String fileName = Input.readString("Nome do Ficheiro: ", true);
            readFile.readShowInfo(fileName);
        }catch (Exception e){
            if(e.getMessage().equals("Token expired, login again.")){
                auth=false;
            }
        }
    }
    private void deleteShow(){
        try{
            if(consAllShow()==0) return;
            int id = Input.readInt("Show id:");
            System.out.println(this.apiRequests.deleteShow(id));
        }catch (Exception e){
            if(e.getMessage().equals("Token expired, login again.")){
                System.out.println(e.getMessage());
                auth=false;
            }
        }
    }
    private List<Show> checkAvailableShows() throws Exception{
        List<Show> showList = this.apiRequests.getShows();
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        List<Show> availableShows = new ArrayList<>();
        for (Show entity : showList) {
            if(entity.getVisivel()==1) {
                LocalDateTime showDateTime = LocalDateTime.parse(entity.getData_hora(), formatter);
                if (showDateTime.isAfter(currentDateTime) && showDateTime.minusHours(24).isAfter(currentDateTime))
                    availableShows.add(entity);
            }
        }
        return availableShows;
    }

}