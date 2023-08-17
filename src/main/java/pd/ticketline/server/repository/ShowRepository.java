package pd.ticketline.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pd.ticketline.server.model.Show;

@Repository
public interface ShowRepository extends JpaRepository<Show, Integer> {

 }
