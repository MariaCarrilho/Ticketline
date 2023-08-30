package pd.ticketline.client.logic;

import pd.ticketline.server.model.Show;
import pd.ticketline.server.model.Sit;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ReadFile {
    private final APIRequests apiRequests;
    private Show showInfo = new Show();

    public ReadFile(APIRequests apiRequests){
        this.apiRequests = apiRequests;
    }

    public void readShowInfo(String filename) throws Exception {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))){
            String line;
            int lineNumber = 1;
            read:while ((line = reader.readLine()) != null) {
                line = line.replaceAll("”", "");
                line = line.replaceAll("“", "");
                line = line.replaceAll("\"", "");

                if (lineNumber <= 9) {
                    parseBasicInfo(showInfo, line, lineNumber);
                } else if (lineNumber == 10) {
                    int id = apiRequests.adicShow(showInfo);
                    if (id<1) break read;

                    showInfo.setId(id);
                    validateHeader(line);
                } else {
                    parseSeatingInfo(showInfo, line);
                }
                lineNumber++;
            }
            System.out.println("Show and sits inserted.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
        showInfo = new Show();
    }
    private void parseSeatingInfo(Show showInfo, String line) throws Exception {
        String[] parts = line.split(";");
        if (parts.length >= 2) {
            String row = parts[0].replaceAll("”", "");
            Sit sit = new Sit();
            sit.setFila(row);

            for (int i = 1; i < parts.length; i++) {
                String[] priceParts = parts[i].replaceAll("\"", "").trim().split(":");
                 sit.setAssento(String.valueOf(i));
                sit.setPreco(Float.valueOf(priceParts[1]));
                sit.setEspetaculo(showInfo);
                this.apiRequests.adicSit(sit);
            }
        } else {
            throw new IllegalArgumentException("Invalid format in line: " + line);
        }
    }
    private static void parseBasicInfo(Show showInfo, String line, int lineNumber) {
        String[] parts = line.split(";");
        if (parts.length != 2 && lineNumber!=2 && lineNumber!=3 && lineNumber!=4)
            throw new IllegalArgumentException("Invalid format in line " + lineNumber);
        else if(lineNumber==3 && parts.length!=4) throw new IllegalArgumentException("Invalid format in line " + lineNumber);
        else if(lineNumber==4 && parts.length!=3) throw new IllegalArgumentException("Invalid format in line " + lineNumber);

        switch (lineNumber) {
            case 1 -> showInfo.setDescricao(parts[1]);
            case 2 -> {
                parts = line.split(":");
                showInfo.setTipo(parts[1]);
            }
            case 3 -> showInfo.setData_hora(parts[1]+ "-" + parts[2]+ "-" + parts[3]);
            case 4 -> showInfo.setData_hora(showInfo.getData_hora() + " " + parts[1]+ ":" + parts[2]);
            case 5 -> showInfo.setDuracao(Integer.valueOf(parts[1]));
            case 6 -> showInfo.setLocal(parts[1]);
            case 7 -> showInfo.setLocalidade(parts[1]);
            case 8 -> showInfo.setPais(parts[1]);
            case 9 -> showInfo.setClassificacao_etaria(parts[1]);
            default -> {
            }
        }
    }
    private static void validateHeader(String line) {
        if (!line.equals("Fila;Lugar:Preço")) {
            throw new IllegalArgumentException("Invalid header format");
        }
    }
}
