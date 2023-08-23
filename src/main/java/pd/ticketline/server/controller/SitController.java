package pd.ticketline.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pd.ticketline.server.model.Sit;
import pd.ticketline.server.exceptionhandler.CustomException;
import pd.ticketline.server.service.SitService;
import pd.ticketline.utils.JWTUtil;

import java.util.List;

@RestController
@RequestMapping("/sits")
public class SitController {
    private final SitService sitService;

    @Autowired
    public SitController(SitService sitService){
        this.sitService = sitService;
    }
    @PostMapping("/add")
    public Sit createSit(@RequestBody Sit sit, HttpServletRequest request) {
        if(JWTUtil.isTokenValid(request))
            return sitService.addSit(sit);
        throw new CustomException("Unknown Error", HttpStatus.BAD_REQUEST);
    }
    @GetMapping("/{id}")
    public List<Sit> getSits(@PathVariable Integer id, HttpServletRequest request) {
        if(JWTUtil.isTokenValid(request))
            return sitService.getSits(id);
        throw new CustomException("Unknown Error", HttpStatus.BAD_REQUEST);
    }
}
