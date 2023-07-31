package pd.springboot.ticketlineserver.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "espetaculo")
public class ShowEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;
    @Column(nullable = false)
    private String descricao;
    @Column(nullable = false)
    private String tipo;
    @Column(nullable = false)
    private String data_hora;
    @Column(nullable = false)
    private Integer duracao;
    @Column(nullable = false)
    private String local;
    @Column(nullable = false)
    private String localidade;
    @Column(nullable = false)
    private String pais;
    @Column(nullable = false)
    private String classificacao_etaria;
    @Column(nullable = false)
    private Integer visivel;
}
