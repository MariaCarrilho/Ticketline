package pd.springboot.ticketlineserver.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import pd.springboot.ticketlineserver.repository.UserRepository;

@RestController
public class UserController {
    @Autowired
    private UserRepository userRepository;
}
