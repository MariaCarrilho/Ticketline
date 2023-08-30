package pd.ticketline.server.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pd.ticketline.server.clientconnection.TCPServer;
import pd.ticketline.server.model.Reservation;
import pd.ticketline.server.model.Show;
import pd.ticketline.server.exceptionhandler.CustomException;
import pd.ticketline.server.repository.ShowRepository;
import pd.ticketline.server.rmiconnection.DatabaseBackupImpl;
import pd.ticketline.utils.JWTUtil;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;

@Service
public class ShowService {
    private final ShowRepository showRepository;
    private final SitService sitService;
    private final ReservationService reservationService;
    private final DatabaseBackupImpl databaseBackup;
    private final TCPServer tcpServer;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    public ShowService(ShowRepository showRepository, SitService sitService, ReservationService reservationService, EntityManager entityManager) throws RemoteException {
        this.showRepository = showRepository;
        this.sitService = sitService;
        this.reservationService = reservationService;
        this.entityManager = entityManager;
        this.tcpServer = new TCPServer();
        this.databaseBackup = new DatabaseBackupImpl();
    }
    @Transactional
    public Show addShow(Show show, HttpServletRequest request) {
        try {
            String token = JWTUtil.getToken(request);
            Show show1 = showRepository.save(show);
            databaseBackup.notifyListeners("INSERT INTO espetaculo (id, descricao, tipo, data_hora, duracao, local, localidade, pais, classificacao_etaria, visivel) VALUES (NULL, '" +
                   show1.getDescricao()+ "', '" + show1.getTipo()+ "', '" + show1.getData_hora()+ "', '"+ show1.getDuracao()+ "', '" + show1.getLocal()+ "', '"+ show1.getLocalidade() + "', '" + show1.getPais()+ "', '" + show1.getClassificacao_etaria() + "', 0 )");
            tcpServer.sendMessageToAllClients("Foi inserido um novo espetáculo.", token);
            return show1;
        } catch (Exception e){
            throw new CustomException(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        }
    }

    public List<Show> getAllShows() {
        return showRepository.findAll();
    }

    public List<Show> getSomeShows(String criteria,
                                   String search)  {
        String queryString = "SELECT s FROM Show s " +
                "WHERE s." + criteria + " LIKE :search";
        return  entityManager.createQuery(queryString, Show.class)
                .setParameter("search",
                        "%"+search+"%").getResultList();
    }

    @Transactional
    public void deleteShow(Integer id, HttpServletRequest request){
        String token = JWTUtil.getToken(request);
        Optional<Show> optionalShow = showRepository.findById(id);
        if (optionalShow.isPresent()) {
            Show show = optionalShow.get();
            List<Reservation> reservationEntities = reservationService.findReservations(show);
            if(!reservationEntities.isEmpty())
                throw new CustomException("You cant delete a show with reservations.", HttpStatus.FORBIDDEN);
            sitService.deleteSits(show);
            showRepository.delete(show);
            databaseBackup.notifyListeners("DELETE FROM espetaculo WHERE id = " + id);
            tcpServer.sendMessageToAllClients("Foi eliminado o espetáculo com o id " + id + ".", token);

        } else {
            throw new CustomException("Show with id " + id + " not found", HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public Show editShow(Show show, HttpServletRequest request) {
        String token = JWTUtil.getToken(request);
        Optional<Show> optionalShow = showRepository.findById(show.getId());
        if(optionalShow.isPresent()) {
            Show show1 = showRepository.save(show);
            databaseBackup.notifyListeners("UPDATE espetaculo SET descricao='" + show1.getDescricao()+"', tipo='" + show1.getTipo()+ "', " +
                    "data_hora='"  + show1.getData_hora()+ "', duracao='" + show1.getDuracao()+ "', local='" + show1.getLocal()
                    + "', localidade='" + show1.getLocalidade() +  "',pais='" + show1.getPais()+ "', classificacao_etaria='"
                    + show1.getClassificacao_etaria() +  "', visivel=" +show1.getVisivel() + " WHERE id =" + show1.getId() +";");
            tcpServer.sendMessageToAllClients("O espetáculo com o id " + show.getId() + " foi alterado.", token);

            return show1;

        }else
            throw new CustomException("Show with id " + show.getId() + " not found", HttpStatus.NOT_FOUND);

    }
}
