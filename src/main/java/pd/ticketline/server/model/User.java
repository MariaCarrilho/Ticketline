package pd.ticketline.server.model;

import jakarta.persistence.*;


@Entity
@Table(name = "utilizador")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "administrador", nullable = false, columnDefinition = "INTEGER default 0")
    private Integer administrador;
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "password", nullable = false)
    private String password;

    public User(String username, String name, String password) {
        this.username = username;
        this.nome = name;
        this.password = password;
        this.administrador=0;

    }

    public User() {
        this.administrador=0;
    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return nome;
    }

    public void setName(String name) {
        this.nome = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getAdministrador() {
        return administrador;
    }

    @Override
    public String toString() {
        return username + " com o nome " + nome + " e password " + password;
    }
}
