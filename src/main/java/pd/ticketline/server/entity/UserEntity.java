package pd.springboot.ticketlineserver.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "utilizador")
public class UserEntity {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(unique = true, nullable = false)
    private String name;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private Integer administrador;

}
