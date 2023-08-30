package pd.ticketline.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pd.ticketline.server.model.Reservation;
import pd.ticketline.server.model.Show;
import pd.ticketline.server.model.User;

import java.util.List;


@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    List<Reservation> findByEspetaculo(@Param("espetaculo") Show espetaculo);

    List<Reservation> findByPagoAndUser(@Param("pago") Integer pago,@Param("user") User user);

    Reservation findByPagoAndId(@Param("pago") Integer pago, @Param("id") Integer id);

    Reservation findReservationById(@Param("id") Integer id);

    List<Reservation> findReservationsByUser(@Param("user") User user);
}
