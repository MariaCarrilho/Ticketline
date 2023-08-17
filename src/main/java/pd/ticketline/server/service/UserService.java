package pd.ticketline.server.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pd.ticketline.server.model.User;
import pd.ticketline.server.exceptionhandler.CustomException;
import pd.ticketline.server.repository.UserRepository;
import pd.ticketline.server.rmiconnection.DatabaseBackupImpl;
import pd.ticketline.utils.Auth;
import pd.ticketline.utils.EditUser;
import pd.ticketline.utils.JWTUtil;
import pd.ticketline.utils.LoginUser;

import java.rmi.RemoteException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    DatabaseBackupImpl databaseBackup;

    public UserService() throws RemoteException {
        this.databaseBackup = new DatabaseBackupImpl();
    }

    public Auth auth(LoginUser auth){
        User user = userRepository.findByUsername(auth.getUsername());
        try {
            if(auth.getPassword().equals("admin") && auth.getUsername().equals("admin")) {
                return new Auth(JWTUtil.generateToken(auth.getUsername()), true);
            } else if (user==null) {
                throw new CustomException("User not Found", HttpStatus.NOT_FOUND);

            } else if(user.getPassword().equals(auth.getPassword())) {
                return new Auth(JWTUtil.generateToken(auth.getUsername()), false);
            }
        }catch (Exception e){
            throw new CustomException(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        throw new CustomException("Bad Credentials", HttpStatus.FORBIDDEN);

    }
    public String addUser(User user){
        try {
            User newUser = userRepository.save(user);
            databaseBackup.updateDatabase("INSERT INTO utilizador VALUES (NULL, 0 '" + user.getName()+"','"+ user.getPassword()+"','"+user.getUsername() +"')");
            return newUser.toString();
        } catch (Exception e){
            throw new CustomException("User already exists or non-complete credentials.", HttpStatus.NOT_ACCEPTABLE);
        }
    }
    public String editUser(EditUser user, HttpServletRequest request){
        User userToUpdate = getUser(request);
        if (userToUpdate!=null){
            userToUpdate.setName(user.getName());
            userToUpdate.setPassword(user.getPassword());
            return  userRepository.save(userToUpdate).toString();
        } else {
            throw new CustomException("User not found", HttpStatus.NOT_FOUND);
        }
    }

    public User getUser(HttpServletRequest request){
        String username = JWTUtil.extractUsernameFromToken(request);
        return userRepository.findByUsername(username);
    }

}
