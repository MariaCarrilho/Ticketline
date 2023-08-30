package pd.ticketline.server.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pd.ticketline.server.clientconnection.TCPServer;
import pd.ticketline.server.model.*;
import pd.ticketline.server.exceptionhandler.CustomException;
import pd.ticketline.server.repository.*;
import pd.ticketline.server.rmiconnection.DatabaseBackupImpl;
import pd.ticketline.utils.BookSit;
import pd.ticketline.utils.JWTUtil;
import pd.ticketline.utils.UnbookedReservations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ShowRepository showRepository;
    private final SitRepository sitRepository;
    private final UserRepository userRepository;
    private final SitsReservationService sitsReservationService;
    private final TCPServer tcpServer;

    private final DatabaseBackupImpl databaseBackup;
    private static final
    long PAYMENT_TIME_LIMIT_MS = 10000;
    private final
    ScheduledExecutorService paymentTimerExecutor
            = Executors.newScheduledThreadPool(1);
    @Autowired
    public ReservationService(ReservationRepository reservationRepository, ShowRepository showRepository, SitRepository sitRepository, UserRepository userRepository, SitsReservationService service) throws IOException {
        this.reservationRepository = reservationRepository;
        this.showRepository = showRepository;
        this.sitRepository = sitRepository;
        this.userRepository = userRepository;
        this.sitsReservationService = service;
        this.tcpServer = new TCPServer();
        this.databaseBackup = new DatabaseBackupImpl();
    }
    private void startPaymentTimer(Integer id_reserva, Sit sit, String token) {
        paymentTimerExecutor.schedule(()-> {
            try {
                checkIfPaid(id_reserva, sit, token);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, PAYMENT_TIME_LIMIT_MS, TimeUnit.MILLISECONDS);
    }
    @Transactional
    public Reservation addReservation(BookSit booking, HttpServletRequest request){
        try {
            String token = JWTUtil.getToken(request);
            Optional<Show> show = showRepository.findById(booking.getEspetaculo_id());
            if(show.isEmpty()) throw new CustomException("This show no longer exists.", HttpStatus.NOT_FOUND);
            User user = this.userRepository.findByUsername(JWTUtil.extractUsernameFromToken(request));
            if(user == null) throw new CustomException("This user was deleted.", HttpStatus.NOT_FOUND);
            Sit sit = sitRepository.findSitEntityByAssentoAndFilaAndEspetaculo(booking.getAssento(), booking.getFila(), show.get());
            SitsReservation sitsReservation = this.sitsReservationService.getSitsReservationBySit(sit);
            if(sitsReservation!=null) throw new CustomException("This sit is taken.", HttpStatus.FORBIDDEN);

            Reservation reservation = reservationRepository.save(new Reservation(booking.getData_hora(), user, show.get()));

            databaseBackup.notifyListeners("INSERT INTO reserva (id, data_hora, pago, id_espetaculo, id_utilizador) " +
                    "VALUES (NULL, '" + reservation.getData_hora() +"', '" + reservation.getPago() + "', '" + reservation.getEspetaculo().getId() + "', '" + reservation.getUser().getId() + "')");

            sitsReservationService.addSitsReservation(sit, reservation);
            tcpServer.sendMessageToAllClients(new UnbookedReservations(sit.getFila(), sit.getAssento(), sit.getEspetaculo().getId()), token);
            tcpServer.sendMessageToAllClients("O lugar acima está indisponível.", token);
            startPaymentTimer(reservation.getId(), sit, token);
            return reservation;
        } catch (Exception e){
            throw new CustomException(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        }
    }

    private void checkIfPaid(Integer id_reserva, Sit sit, String token) throws IOException {

        Reservation reservation = reservationRepository.findReservationById(id_reserva);
        if(reservation.getPago() == 0){
            this.sitsReservationService.deleteSitsReservation(new SitsReservation(sit, reservation));
            reservationRepository.delete(reservation);

            databaseBackup.notifyListeners("DELETE from reserva where id=" + id_reserva);

            tcpServer.sendMessageToAllClients(new UnbookedReservations(sit.getFila(), sit.getAssento(), sit.getEspetaculo().getId()), token);
            tcpServer.sendMessageToAllClients("O lugar acima está novamente disponível.", token);
        }

    }


    public List<Reservation> findReservations(Show show){
        return reservationRepository.findByEspetaculo(show);
    }

    public List<SitsReservation> getReservationsByPaid(Integer pago, HttpServletRequest request){
        List<SitsReservation> sitsReservationsList = new ArrayList<>();
        User user = this.userRepository.findByUsername(JWTUtil.extractUsernameFromToken(request));
        if(user == null) throw new CustomException("This user was deleted.", HttpStatus.NOT_FOUND);
        List<Reservation> reservations = reservationRepository.findByPagoAndUser(pago, user);
        for(Reservation reservation: reservations){
            sitsReservationsList.add(sitsReservationService.getSitsReservationByUser(reservation));
        }
        return sitsReservationsList;
    }

    @Transactional
    public void deleteReservationsByUser(User user){
        List<Reservation> reservationEntities = reservationRepository.findReservationsByUser(user);
        sitsReservationService.deleteAllByReservation(reservationEntities);
        reservationRepository.deleteAll(reservationEntities);
    }
    @Transactional
    public void deleteUnpaidReservation(Integer id, HttpServletRequest request) {
        User user = this.userRepository.findByUsername(JWTUtil.extractUsernameFromToken(request));
        if(user == null) throw new CustomException("This user was deleted", HttpStatus.NOT_FOUND);
        Reservation unpaidReservation = reservationRepository.findByPagoAndId(0, id);
        if(unpaidReservation==null) throw new CustomException("The reservation "+id+" was either paid or not found.", HttpStatus.NOT_FOUND);
        sitsReservationService.deleteByReservation(unpaidReservation);

        reservationRepository.delete(unpaidReservation);
        databaseBackup.notifyListeners("DELETE from reserva where id=" + unpaidReservation.getId());

    }
    @Transactional
    public Reservation payReservation(Integer reservation_id, HttpServletRequest request){
        User user = this.userRepository.findByUsername(JWTUtil.extractUsernameFromToken(request));
        if(user == null) throw new CustomException("This user was deleted.", HttpStatus.NOT_FOUND);

        Reservation unpaidReservation = reservationRepository.findByPagoAndId(0, reservation_id);
        if(unpaidReservation==null) throw new CustomException("This reservation was deleted or paid", HttpStatus.FORBIDDEN);
        unpaidReservation.setPago(1);
        databaseBackup.notifyListeners("UPDATE reserva SET pago=1 WHERE id="+reservation_id);
        return reservationRepository.save(unpaidReservation);
    }
}
