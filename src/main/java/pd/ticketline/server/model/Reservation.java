package pd.ticketline.server.model;

import jakarta.persistence.*;

import java.util.Collection;

@Entity
@Table(name = "reserva")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String data_hora;
    @Column(nullable = false)
    private Integer pago;
    @ManyToOne
    @JoinColumn(name = "id_espetaculo", referencedColumnName = "id")
    private Show espetaculo;
    @ManyToOne
    @JoinColumn(name = "id_utilizador", referencedColumnName = "id")
    private User user;

    @OneToMany(mappedBy = "reservation")
    private Collection<SitsReservation> sits;

    public Reservation(String data_hora, User user, Show show) {
        this.data_hora = data_hora;
        this.user = user;
        this.espetaculo = show;
        this.pago =0;
    }

    public Reservation() {}

    public Integer getId() {
        return id;
    }

    public Integer getPago() {
        return pago;
    }

    public void setPago(Integer pago) {
        this.pago = pago;
    }

    @Override
    public String toString() {
        return  "Reserva " + id +
                " feita às " +  data_hora + " para o espetáculo " + espetaculo.getDescricao();
    }
}
