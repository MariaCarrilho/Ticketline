package pd.ticketline.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pd.ticketline.server.model.User;
import pd.ticketline.server.exceptionhandler.CustomException;
import pd.ticketline.server.service.UserService;
import pd.ticketline.utils.Auth;
import pd.ticketline.utils.EditUser;
import pd.ticketline.utils.JWTUtil;
import pd.ticketline.utils.LoginUser;

@RestController
public class UserController {
     @Autowired
    private UserService userService;

    @RequestMapping(value = "/auth", method = RequestMethod.POST)
    public Auth auth(@RequestBody LoginUser auth) {
        return userService.auth(auth);
    }
    @RequestMapping(value = "/user/add", method = RequestMethod.POST)
    public String createUser(@RequestBody User user) {
            return userService.addUser(user);
    }
    @RequestMapping(value = "/user/update", method = RequestMethod.PUT)
    public String editUser(@RequestBody EditUser user, HttpServletRequest request) {
        if(JWTUtil.isTokenValid(request))
            return userService.editUser(user, request);
        throw new CustomException("Unknown Error", HttpStatus.BAD_REQUEST);

    }




}
