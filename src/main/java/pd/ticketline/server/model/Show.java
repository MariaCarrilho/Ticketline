package pd.ticketline.server.model;

import jakarta.persistence.*;

@Entity
@Table(name = "espetaculo")
public class Show {

    public Show() {
        this.visivel = 0;
    }

    public Show(Integer id, String descricao, String tipo, String data_hora, Integer duracao, String local, String localidade, String pais, String classificacao_etaria, Integer visivel) {
        this.descricao = descricao;
        this.tipo = tipo;
        this.data_hora = data_hora;
        this.duracao = duracao;
        this.local = local;
        this.localidade = localidade;
        this.pais = pais;
        this.classificacao_etaria = classificacao_etaria;
        this.visivel = visivel;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public Show(Show show) {
        this.id = show.getId();
        this.descricao = show.getDescricao();
        this.tipo = show.getTipo();
        this.data_hora = show.getData_hora();
        this.duracao = show.getDuracao();
        this.local = show.getLocal();
        this.localidade = show.getLocalidade();
        this.pais = show.getPais();
        this.classificacao_etaria = show.getClassificacao_etaria();
        this.visivel = show.getVisivel();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getData_hora() {
        return data_hora;
    }

    public void setData_hora(String data_hora) {
        this.data_hora = data_hora;
    }

    public Integer getDuracao() {
        return duracao;
    }

    public void setDuracao(Integer duracao) {
        this.duracao = duracao;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getLocalidade() {
        return localidade;
    }

    public void setLocalidade(String localidade) {
        this.localidade = localidade;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getClassificacao_etaria() {
        return classificacao_etaria;
    }

    public void setClassificacao_etaria(String classificacao_etaria) {
        this.classificacao_etaria = classificacao_etaria;
    }

    public Integer getVisivel() {
        return visivel;
    }

    public void setVisivel(Integer visivel) {
        this.visivel = visivel;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Show otherShow = (Show) obj;
        return id == otherShow.id;
    }

    @Override
    public String toString() {
        return "Espetáculo " + id +
                ": " + descricao + " é do tipo " +
                tipo + " no horário " + data_hora +  " no " +
                local + " localizado em " + localidade + ", " + pais
                +" com duração de " + duracao+" minutos. Classificação Etária de "+ classificacao_etaria + '\n' +
                (visivel == 1 ? "Visível para todos" : "Visível apenas para administradores.") ;
    }
}
