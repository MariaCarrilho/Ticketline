package pd.ticketline.utils;

public class BookSit {
    private String fila;
    private String assento;

    private Integer espetaculo_id;
    private String data_hora;


    public BookSit(String fila, String assento, Integer espetaculo_id, String data_hora) {
        this.fila = fila;
        this.assento = assento;
        this.espetaculo_id = espetaculo_id;
        this.data_hora = data_hora;
    }

    public String getFila() {
        return fila;
    }

    public String getAssento() {
        return assento;
    }

    public Integer getEspetaculo_id() {
        return espetaculo_id;
    }

    public String getData_hora() {
        return data_hora;
    }
}
