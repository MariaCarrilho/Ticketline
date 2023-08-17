package pd.ticketline.utils;

import java.io.Serializable;

public class UnbookedReservations implements Serializable {

    private final String fila;
    private final String assento;
    private final Integer show_id;

    public UnbookedReservations(String fila, String assento, Integer show_id) {
        this.fila = fila;
        this.assento = assento;
        this.show_id = show_id;
    }

    public String getFila() {
        return fila;
    }

    public String getAssento() {
        return assento;
    }

    public Integer getShow_id() {
        return show_id;
    }

    @Override
    public String toString() {
        return "Lugar para o espetáculo " + show_id +
                " na fila " + fila +
                ", assento " + assento;
    }
}
