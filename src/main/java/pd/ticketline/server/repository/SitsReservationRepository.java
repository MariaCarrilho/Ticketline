package pd.ticketline.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pd.ticketline.server.model.Reservation;
import pd.ticketline.server.model.Sit;
import pd.ticketline.server.model.SitsReservation;

@Repository
public interface SitsReservationRepository extends JpaRepository<SitsReservation, Integer> {

    SitsReservation findSitsReservationEntitiesBySitAndReservation(Sit sit, Reservation reservation);
    SitsReservation findSitsReservationByReservation(Reservation reservation);

    SitsReservation findSitsReservationBySit(Sit sit);
}
