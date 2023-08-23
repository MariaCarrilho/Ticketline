package pd.ticketline.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pd.ticketline.server.model.Reservation;
import pd.ticketline.server.model.Show;
import pd.ticketline.server.model.User;

import java.util.List;


@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    List<Reservation> findByEspetaculo(Show espetaculo);

    List<Reservation> findByPagoAndUser(Integer pago,User user);

    Reservation findByPagoAndId(Integer pago, Integer id);

    Reservation findReservationById(Integer id);
}
