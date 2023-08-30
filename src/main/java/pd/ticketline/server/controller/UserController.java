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

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/auth")
    public Auth auth(@RequestBody LoginUser auth) {
        return userService.auth(auth);
    }
    @PostMapping("/add")
    public User createUser(@RequestBody User user) {
        return userService.addUser(user);
    }
    @PutMapping
    public User editUser(@RequestBody EditUser user,
                         HttpServletRequest request) {
        if(JWTUtil.isTokenValid(request))
            return userService.editUser(user, request);
        throw new CustomException("Unknown Error", HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("{username}")
    public void deleteUser(@PathVariable String username, HttpServletRequest request) {
        if(JWTUtil.isTokenValid(request))
             userService.deleteUser(username);
        else throw new CustomException("Unknown Error", HttpStatus.BAD_REQUEST);
    }

    @GetMapping
    public List<User> getAllUsers(HttpServletRequest request){
        if(JWTUtil.isTokenValid(request))
            return userService.getAllUsers();
        else throw new CustomException("Unknown Error", HttpStatus.BAD_REQUEST);
    }



}
