package pd.ticketline.server.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pd.ticketline.server.model.Reservation;
import pd.ticketline.server.model.Show;
import pd.ticketline.server.exceptionhandler.CustomException;
import pd.ticketline.server.repository.ShowRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ShowService {
    private final ShowRepository showRepository;
    private final SitService sitService;
    private final ReservationService reservationService;

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    public ShowService(ShowRepository showRepository, SitService sitService, ReservationService reservationService, EntityManager entityManager) {
        this.showRepository = showRepository;
        this.sitService = sitService;
        this.reservationService = reservationService;
        this.entityManager = entityManager;
    }

    public Show addShow(Show show) {
        try {
            return showRepository.save(show);
        } catch (Exception e){
            throw new CustomException(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        }
    }

    public List<Show> getAllShows() {
        return showRepository.findAll();
    }

    public List<Show> getSomeShows(String criteria, String search)  {
        String queryString = "SELECT s FROM Show s WHERE s."+ criteria + " LIKE '%" + search + "%'";
        return entityManager.createQuery(queryString, Show.class).getResultList();

    }

    public void deleteShow(Integer id){
        Optional<Show> optionalShow = showRepository.findById(id);
        if (optionalShow.isPresent()) {
            Show show = optionalShow.get();
            List<Reservation> reservationEntities = reservationService.findReservations(show);
            if(!reservationEntities.isEmpty())
                throw new CustomException("You cant delete a show with reservations", HttpStatus.FORBIDDEN);
            sitService.deleteSits(show);
            showRepository.delete(show);
        } else {
            throw new CustomException("Show with id " + id + " not found", HttpStatus.NOT_FOUND);
        }
    }


    public Show editShow(Show show) {
        Optional<Show> optionalShow = showRepository.findById(show.getId());
        if(optionalShow.isPresent()) {
            return showRepository.save(show);
        }else
            throw new CustomException("Show with id " + show.getId() + " not found", HttpStatus.NOT_FOUND);

    }
}
