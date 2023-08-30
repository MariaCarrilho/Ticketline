package pd.ticketline.utils;

import java.io.Serializable;
import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnbookedReservations that = (UnbookedReservations) o;
        return fila.equals(that.fila) &&
                assento.equals(that.assento) &&
                show_id.equals(that.show_id);
    }

    @Override
    public int hashCode() {
        return  Objects.hash(fila, assento, show_id);
    }
    @Override
    public String toString() {
        return "Lugar para o espetáculo " + show_id +
                " na fila " + fila +
                ", assento " + assento;
    }
}
