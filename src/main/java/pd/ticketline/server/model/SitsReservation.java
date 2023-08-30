package pd.ticketline.server.model;

import jakarta.persistence.*;

@Entity(name = "reserva_lugar")
@IdClass(RelationId.class)
public class SitsReservation {
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_lugar", foreignKey = @ForeignKey(name = "fk_reservation_sit_id"))
    private Sit sit;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_reserva", foreignKey = @ForeignKey(name = "fk_reservation_sit"))
    private Reservation reservation;

    public SitsReservation(Sit sit, Reservation reservation) {
        this.sit = sit;
        this.reservation = reservation;
    }

    public SitsReservation() {
    }

    @Override
    public String toString() {
        return "Reserva " + reservation.getId() + " feita às " + reservation.getData_hora()
                + " para o espetáculo " + reservation.getEspetaculo().getDescricao() + " no lugar " +
                sit.getAssento() + " na fila " + sit.getFila() +".";
                }
    public Sit getSit() {
        return sit;
    }

    public Reservation getReservation() {
        return reservation;
    }
}
