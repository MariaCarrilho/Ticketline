package pd.springboot.ticketlineserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pd.springboot.ticketlineserver.entity.UserEntity;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    @Query(value = "SELECT * FROM utilizador WHERE name = 'name'", nativeQuery = true)
    List<UserEntity> findByName(@Param("name") String name);
}
