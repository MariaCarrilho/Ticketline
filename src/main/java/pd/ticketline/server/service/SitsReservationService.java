package pd.ticketline.server.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pd.ticketline.server.clientconnection.TCPServer;
import pd.ticketline.server.exceptionhandler.CustomException;
import pd.ticketline.server.model.Reservation;
import pd.ticketline.server.model.Sit;
import pd.ticketline.server.model.SitsReservation;
import pd.ticketline.server.repository.ReservationRepository;
import pd.ticketline.server.repository.SitsReservationRepository;
import pd.ticketline.server.repository.UserRepository;
import pd.ticketline.server.rmiconnection.DatabaseBackupImpl;

import java.rmi.RemoteException;
import java.util.List;


@Service
public class SitsReservationService {
    private final SitsReservationRepository sitsReservationRepository;
    private final DatabaseBackupImpl databaseBackup;
    private final TCPServer tcpServer;


    @Autowired
    public SitsReservationService(SitsReservationRepository sitsReservationRepository) throws RemoteException {
        this.sitsReservationRepository = sitsReservationRepository;
        this.databaseBackup = new DatabaseBackupImpl();
        this.tcpServer = new TCPServer();

    }

    public SitsReservation getSitsReservationBySit(Sit sit){
        return sitsReservationRepository.findSitsReservationBySit(sit);
    }

    public SitsReservation getSitsReservationByUser(Reservation reservation){
        return this.sitsReservationRepository.findSitsReservationByReservation(reservation);
    }
    @Transactional
    public void addSitsReservation(Sit sit, Reservation reservation){
        SitsReservation sitsReservation1 = sitsReservationRepository.save(new SitsReservation(sit, reservation));
        databaseBackup.notifyListeners("INSERT INTO reserva_lugar (id_reserva, id_lugar) " +
                "VALUES ('" + sitsReservation1.getReservation().getId() +"', '" + sitsReservation1.getSit().getId() + "')");

    }
    @Transactional
    public void deleteByReservation(Reservation reservation){
        SitsReservation sitsReservation = this.sitsReservationRepository.findSitsReservationByReservation(reservation);
        deleteSitsReservation(sitsReservation);
        databaseBackup.notifyListeners("DELETE from reserva_lugar where (id_reserva=" + sitsReservation.getReservation().getId()+" AND id_lugar= "+ sitsReservation.getSit().getId()+");" );
    }
    @Transactional
    public void deleteSitsReservation(SitsReservation sitsReservation){
        sitsReservationRepository.delete(sitsReservation);
        databaseBackup.notifyListeners("DELETE from reserva_lugar where (id_reserva=" + sitsReservation.getReservation().getId()+" AND id_lugar= "+ sitsReservation.getSit().getId()+");" );
    }
    @Transactional
    public void deleteAllByReservation(List<Reservation> reservationEntities) {
        for(Reservation reservation: reservationEntities){
            deleteByReservation(reservation);
        }

    }
}
