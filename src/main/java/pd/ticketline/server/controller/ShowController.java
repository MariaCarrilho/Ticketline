package pd.ticketline.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pd.ticketline.server.clientconnection.TCPServer;
import pd.ticketline.server.model.Show;
import pd.ticketline.server.exceptionhandler.CustomException;
import pd.ticketline.server.service.ShowService;
import pd.ticketline.utils.JWTUtil;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/shows")

public class ShowController {
    private final ShowService showService;
    private final TCPServer tcpServer;
    @Autowired
    public ShowController(ShowService showService) {
        this.showService = showService;
        this.tcpServer = new TCPServer();
    }
    @PostMapping("add")
    public Show createShow(@RequestBody Show show, HttpServletRequest request) throws IOException {
        if(JWTUtil.isTokenValid(request)) {
            Show show1 =  showService.addShow(show);
            tcpServer.sendMessageToAllClients("A show was inserted.");
            return show1;
        }
        throw new CustomException("Unknown Error", HttpStatus.BAD_REQUEST);
    }
    @GetMapping
    public List<Show> getAllShows() {
        return showService.getAllShows();
    }
    @GetMapping("{criteria}/{search}")
    public List<Show> getSomeShows(@PathVariable String criteria, @PathVariable String search) {
        return showService.getSomeShows(criteria, search);
    }

    @DeleteMapping("{id}")
    public void deleteShow(@PathVariable Integer id, HttpServletRequest request) throws IOException {
        if(JWTUtil.isTokenValid(request)) {
            showService.deleteShow(id);
            tcpServer.sendMessageToAllClients("A show was deleted.");
        }else
            throw new CustomException("Unknown Error", HttpStatus.BAD_REQUEST);

    }
    @PutMapping
    public Show editShow(@RequestBody Show showEntity, HttpServletRequest request) throws IOException {
        if(JWTUtil.isTokenValid(request)) {
            Show show = showService.editShow(showEntity);
            tcpServer.sendMessageToAllClients("A show was altered.");
            return show;
        }else
            throw new CustomException("Unknown Error", HttpStatus.BAD_REQUEST);
    }
}
