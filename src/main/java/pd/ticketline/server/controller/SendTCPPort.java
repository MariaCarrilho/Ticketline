package pd.ticketline.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pd.ticketline.server.clientconnection.TCPServer;
import pd.ticketline.server.exceptionhandler.CustomException;
import pd.ticketline.utils.JWTUtil;


@RestController
public class SendTCPPort {
    @RequestMapping(value = "/getPort", method = RequestMethod.GET)
    public int getTCPPort(HttpServletRequest request) {
        if(JWTUtil.isTokenValid(request))
            return TCPServer.getPort();
        throw new CustomException("Unknown Error", HttpStatus.BAD_REQUEST);
    }

}
