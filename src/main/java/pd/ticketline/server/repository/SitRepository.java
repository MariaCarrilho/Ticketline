package pd.ticketline.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pd.ticketline.server.model.Show;
import pd.ticketline.server.model.Sit;

import java.util.List;

@Repository
public interface SitRepository extends JpaRepository<Sit, Integer> {

    List<Sit> findByEspetaculo(Show show);
    Sit findSitEntityByAssentoAndFilaAndEspetaculo(String assento, String fila, Show show);
}
