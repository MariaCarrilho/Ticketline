package pd.ticketline.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pd.ticketline.server.model.Reservation;
import pd.ticketline.server.model.Sit;
import pd.ticketline.server.model.SitsReservation;

@Repository
public interface SitsReservationRepository extends JpaRepository<SitsReservation, Integer> {
    SitsReservation findSitsReservationEntitiesBySitAndReservation(@Param("sit") Sit sit, @Param("reservation") Reservation reservation);
    SitsReservation findSitsReservationByReservation(@Param("reservation") Reservation reservation);

    SitsReservation findSitsReservationBySit(@Param("sit") Sit sit);
}
