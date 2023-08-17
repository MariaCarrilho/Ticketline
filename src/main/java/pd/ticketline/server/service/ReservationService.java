package pd.ticketline.server.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pd.ticketline.server.clientconnection.TCPServer;
import pd.ticketline.server.model.*;
import pd.ticketline.server.exceptionhandler.CustomException;
import pd.ticketline.server.repository.*;
import pd.ticketline.utils.BookSit;
import pd.ticketline.utils.UnbookedReservations;

import java.io.IOException;
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
    private final UserService userService;
    private final SitsReservationRepository sitsReservationRepository;
    private static final long PAYMENT_TIME_LIMIT_MS = 10000; // 10 seconds
    private final ScheduledExecutorService paymentTimerExecutor = Executors.newScheduledThreadPool(1);


    public ReservationService(ReservationRepository reservationRepository, ShowRepository showRepository, SitRepository sitRepository, UserService userService, SitsReservationRepository sitsReservationRepository) {
        this.reservationRepository = reservationRepository;
        this.showRepository = showRepository;
        this.sitRepository = sitRepository;
        this.userService = userService;
        this.sitsReservationRepository = sitsReservationRepository;
    }

    public Reservation addReservation(BookSit booking, HttpServletRequest request){
        try {
            Optional<Show> show = showRepository.findById(booking.getEspetaculo_id());
            if(show.isEmpty()) throw new CustomException("This show no longer exists", HttpStatus.NOT_FOUND);
            User user = userService.getUser(request);
            if(user == null) throw new CustomException("This user was deleted", HttpStatus.NOT_FOUND);
            Sit sit = sitRepository.findSitEntityByAssentoAndFilaAndEspetaculo(booking.getAssento(), booking.getFila(), show.get());
            SitsReservation sitsReservation = sitsReservationRepository.findSitsReservationBySit(sit);
            if(sitsReservation!=null) throw new CustomException("This sit is taken.", HttpStatus.FORBIDDEN);

            Reservation reservation = reservationRepository.save(new Reservation(booking.getData_hora(), user, show.get()));
            sitsReservationRepository.save(new SitsReservation(sit, reservation));
            TCPServer.sendMessageToAllClients(new UnbookedReservations(sit.getFila(), sit.getAssento(), sit.getEspetaculo().getId()));
            TCPServer.sendMessageToAllClients("Este lugar está indisponível.");
            startPaymentTimer(reservation.getId(), sit);
            return reservation;
        } catch (Exception e){
            throw new CustomException(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        }
    }

    private void startPaymentTimer(Integer id_reserva, Sit sit) {
        paymentTimerExecutor.schedule(()-> {
            try {
                checkIfPaid(id_reserva, sit);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, PAYMENT_TIME_LIMIT_MS, TimeUnit.MILLISECONDS);
    }

    private void checkIfPaid(Integer id_reserva, Sit sit) throws IOException {
        Reservation reservation = reservationRepository.findReservationById(id_reserva);
        if(reservation.getPago() == 0){

            sitsReservationRepository.delete(new SitsReservation(sit, reservation));
            reservationRepository.delete(reservation);

            TCPServer.sendMessageToAllClients(new UnbookedReservations(sit.getFila(), sit.getAssento(), sit.getEspetaculo().getId()));
            TCPServer.sendMessageToAllClients("Este lugar está novamente disponível.");
        }

    }


    public List<Reservation> findReservations(Show show){
        return reservationRepository.findByEspetaculo(show);
    }

    public String[] getReservationsByPaid(Integer pago, HttpServletRequest request){
        User user = userService.getUser(request);
        if(user == null) throw new CustomException("This user was deleted", HttpStatus.NOT_FOUND);
        return new String[]{reservationRepository.findByPagoAndUser(pago, user).toString()};
    }

    public void deleteReservations(Show show){
        List<Reservation> reservationEntities = reservationRepository.findByEspetaculo(show);
        reservationRepository.deleteAll(reservationEntities);
    }

    public void deleteUnpaidReservation(Integer id, HttpServletRequest request) {
        User user = userService.getUser(request);
        if(user == null) throw new CustomException("This user was deleted", HttpStatus.NOT_FOUND);
        Reservation unpaidReservation = reservationRepository.findByPagoAndId(0, id);
        if(unpaidReservation==null) throw new CustomException("The reservation "+id+" was either paid or not found.", HttpStatus.NOT_FOUND);

        SitsReservation sitsReservation = sitsReservationRepository.findSitsReservationByReservation(unpaidReservation);
        sitsReservationRepository.delete(sitsReservation);
        reservationRepository.delete(unpaidReservation);
    }

    public Reservation payReservation(Integer reservation_id, HttpServletRequest request){
        User user = userService.getUser(request);
        if(user == null) throw new CustomException("This user was deleted", HttpStatus.NOT_FOUND);

        Reservation unpaidReservation = reservationRepository.findByPagoAndId(0, reservation_id);
        if(unpaidReservation==null) throw new CustomException("This reservation was deleted or paid", HttpStatus.FORBIDDEN);
        unpaidReservation.setPago(1);
        return reservationRepository.save(unpaidReservation);
    }
}
