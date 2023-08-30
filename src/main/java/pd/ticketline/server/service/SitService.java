package pd.ticketline.server.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pd.ticketline.server.clientconnection.TCPServer;
import pd.ticketline.server.model.Reservation;
import pd.ticketline.server.model.Show;
import pd.ticketline.server.model.Sit;
import pd.ticketline.server.model.SitsReservation;
import pd.ticketline.server.exceptionhandler.CustomException;
import pd.ticketline.server.repository.ReservationRepository;
import pd.ticketline.server.repository.ShowRepository;
import pd.ticketline.server.repository.SitRepository;
import pd.ticketline.server.repository.SitsReservationRepository;
import pd.ticketline.server.rmiconnection.DatabaseBackupImpl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SitService {
    private final SitRepository sitRepository;
    private final ShowRepository showRepository;
    private final ReservationRepository reservationRepository;
    private final SitsReservationRepository sitsReservationRepository;
    private final DatabaseBackupImpl databaseBackup;
    private final TCPServer tcpServer;

    @Autowired
    public SitService(SitRepository sitRepository, ShowRepository showRepository, ReservationRepository reservationRepository, SitsReservationRepository sitsReservationRepository) throws RemoteException {
        this.sitRepository = sitRepository;
        this.showRepository = showRepository;
        this.reservationRepository = reservationRepository;
        this.sitsReservationRepository = sitsReservationRepository;
        this.databaseBackup = new DatabaseBackupImpl();
        this.tcpServer = new TCPServer();
    }
    @Transactional
    public Sit addSit(Sit sit){
        try {
            Sit sit1 = sitRepository.save(sit);
            databaseBackup.notifyListeners("INSERT INTO lugar (id, assento, fila, preco, espetaculo_id) VALUES " +
                    "(NULL, '" + sit1.getAssento() +"', '" + sit1.getFila() +"', '" + sit1.getPreco()+"', '"
                    +sit1.getEspetaculo().getId()+"')" );
            return sit1;
        } catch (Exception e){
            throw new CustomException("Error inserting sits", HttpStatus.NOT_ACCEPTABLE);
        }
    }

    public List<Sit> getSits (Integer id){
        Optional<Show> optionalShow = showRepository.findById(id);
        if(optionalShow.isPresent()) {
            List<Sit> allSits = sitRepository.findByEspetaculo(optionalShow.get());
            List<Reservation> reservationsShows = reservationRepository.findByEspetaculo(optionalShow.get());
            List<Sit> availableSits = new ArrayList<>(allSits);
            for(Sit sit: allSits){
                SitsReservation isSitReserved =null;
                for(Reservation reservation:reservationsShows) {
                    isSitReserved = sitsReservationRepository.findSitsReservationEntitiesBySitAndReservation(sit, reservation);
                    if(isSitReserved!=null) availableSits.remove(sit);

                }
            }
            return availableSits;
        }else
            throw new CustomException("Show with id " + id + " not found", HttpStatus.NOT_FOUND);
    }

    @Transactional
    public void deleteSits(Show espetaculo){
        try {
            List<Sit> sitEntities = sitRepository.findByEspetaculo(espetaculo);
            sitRepository.deleteAll(sitEntities);
            databaseBackup.notifyListeners("DELETE FROM lugar WHERE espetaculo_id="+espetaculo.getId());
        }catch (Exception e){
            throw new CustomException("Error deleting sits", HttpStatus.NOT_ACCEPTABLE);
        }

    }
}
