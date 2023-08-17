package pd.ticketline.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pd.ticketline.server.model.Reservation;
import pd.ticketline.server.model.Show;
import pd.ticketline.server.model.Sit;
import pd.ticketline.server.model.SitsReservation;
import pd.ticketline.server.exceptionhandler.CustomException;
import pd.ticketline.server.repository.ReservationRepository;
import pd.ticketline.server.repository.ShowRepository;
import pd.ticketline.server.repository.SitRepository;
import pd.ticketline.server.repository.SitsReservationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SitService {
    @Autowired
    private SitRepository sitRepository;
    @Autowired
    private ShowRepository showRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private SitsReservationRepository sitsReservationRepository;
    public Sit addSit(Sit sit){
        try {
            return sitRepository.save(sit);
        } catch (Exception e){
            throw new CustomException("Unknown Error", HttpStatus.NOT_ACCEPTABLE);
        }
    }

    public List<Sit> getSits (Integer id){
        Optional<Show> optionalShow = showRepository.findById(id);
        if(optionalShow.isPresent()) {
            List<Sit> allSits = sitRepository.findByEspetaculo(optionalShow.get());
            List<Reservation> reservationsShows = reservationRepository.findByEspetaculo(optionalShow.get());
            List<Sit> availableSits = new ArrayList<>();
            for(Sit sit: allSits){
                SitsReservation isSitReserved =null;
                for(Reservation reservation:reservationsShows) {
                    isSitReserved = sitsReservationRepository.findSitsReservationEntitiesBySitAndReservation(sit, reservation);
                }
                if(isSitReserved==null) availableSits.add(sit);
            }
            return availableSits;
        }else
            throw new CustomException("Show with id " + id + " not found", HttpStatus.NOT_FOUND);
    }

    public void deleteSits(Show espetaculo){
        List<Sit> sitEntities = sitRepository.findByEspetaculo(espetaculo);
        sitRepository.deleteAll(sitEntities);
    }
}
