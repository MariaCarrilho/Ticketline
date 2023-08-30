package pd.ticketline.server.model;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "lugar")
public class Sit {

    @Column(nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String fila;
    @Column(nullable = false)
    private String assento;
    @Column(nullable = false)
    private Float preco;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "espetaculo_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Show espetaculo;

    public Sit() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFila() {
        return fila;
    }

    public void setFila(String fila) {
        this.fila = fila;
    }

    public String getAssento() {
        return assento;
    }

    public void setAssento(String assento) {
        this.assento = assento;
    }

    public Float getPreco() {
        return preco;
    }

    public void setPreco(Float preco) {
        this.preco = preco;
    }

    public Show getEspetaculo() {
        return espetaculo;
    }

    public void setEspetaculo(Show show) {
        this.espetaculo= show;
    }

    @Override
    public String toString() {
        return  " | Assento "+assento +
                " custa " + preco + " | ";
    }
}
