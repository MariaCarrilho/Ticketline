package pd.ticketline.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pd.ticketline.server.model.Show;
import pd.ticketline.server.model.Sit;

import java.util.List;

@Repository
public interface SitRepository extends JpaRepository<Sit, Integer> {
    List<Sit> findByEspetaculo(@Param("show") Show show);
    Sit findSitEntityByAssentoAndFilaAndEspetaculo(
            @Param("assento") String assento,
            @Param("fila") String fila,@Param("show") Show show);
}
