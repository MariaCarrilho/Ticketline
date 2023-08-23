package pd.ticketline.server.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pd.ticketline.server.model.Reservation;
import pd.ticketline.server.exceptionhandler.CustomException;
import pd.ticketline.server.service.ReservationService;
import pd.ticketline.utils.BookSit;
import pd.ticketline.utils.JWTUtil;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    @Autowired
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }
    @PostMapping
    public Reservation createSit(@RequestBody BookSit booking, HttpServletRequest request) {
        if(JWTUtil.isTokenValid(request))
            return reservationService.addReservation(booking, request);
        throw new CustomException("Unknown Error", HttpStatus.BAD_REQUEST);
    }
    @GetMapping("/paid")
    public List<Reservation> getPaidReservations(HttpServletRequest request){
        if(JWTUtil.isTokenValid(request))
            return reservationService.getReservationsByPaid(1, request);
        throw new CustomException("Unknown Error", HttpStatus.BAD_REQUEST);
    }
    @GetMapping("/unpaid")
    public List<Reservation> getUnpaidReservations(HttpServletRequest request) {
        if (JWTUtil.isTokenValid(request))
            return reservationService.getReservationsByPaid(0, request);
        throw new CustomException("Unknown Error", HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("{id}")
    public void deleteUnpaidReservation(@PathVariable Integer id, HttpServletRequest request){
        if (JWTUtil.isTokenValid(request)) {
            reservationService.deleteUnpaidReservation(id, request);
        }else throw new CustomException("Unknown Error", HttpStatus.BAD_REQUEST);
    }
    @GetMapping("{id}")
    public Reservation payReservation(@PathVariable Integer id, HttpServletRequest request){
        if (JWTUtil.isTokenValid(request)) {
            return reservationService.payReservation(id, request);
        }else throw new CustomException("Unknown Error", HttpStatus.BAD_REQUEST);
    }

}
