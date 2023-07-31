package pd.springboot.ticketlineserver.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "lugar")
public class SitEntity {
    @Column(nullable = false)
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String fila;
    @Column(nullable = false)
    private String assento;
    @Column(nullable = false)
    private Float preco;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "espetaculo_id")
    private ShowEntity espetaculo;
}
