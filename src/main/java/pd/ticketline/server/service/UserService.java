package pd.ticketline.server.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pd.ticketline.server.clientconnection.TCPServer;
import pd.ticketline.server.model.User;
import pd.ticketline.server.exceptionhandler.CustomException;
import pd.ticketline.server.repository.UserRepository;
import pd.ticketline.server.rmiconnection.DatabaseBackupImpl;
import pd.ticketline.utils.Auth;
import pd.ticketline.utils.EditUser;
import pd.ticketline.utils.JWTUtil;
import pd.ticketline.utils.LoginUser;

import java.rmi.RemoteException;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DatabaseBackupImpl databaseBackup;
    private final ReservationService reservationService;
    private final TCPServer tcpServer;

    @Autowired
    public UserService(UserRepository userRepository, ReservationService reservationService) throws RemoteException {
        this.databaseBackup = new DatabaseBackupImpl();
        this.userRepository = userRepository;
        this.reservationService = reservationService;
        this.tcpServer = new TCPServer();
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
    @Transactional
    public User addUser(User user){
        try {
            User newUser = userRepository.save(user);
            databaseBackup.notifyListeners("INSERT INTO utilizador " +
                    "(id, administrador, nome, password, username) " +
                    "VALUES (NULL, 0, '" + user.getName()+"','"+
                    user.getPassword()+"','"+user.getUsername() +"')");
            return newUser;
        } catch (Exception e){
            throw new CustomException("User already exists or " +
                    "non-complete credentials.", HttpStatus.NOT_ACCEPTABLE);
        }
    }
    @Transactional
    public User editUser(EditUser user, HttpServletRequest request){
        User userToUpdate = getUser(request);
        if (userToUpdate!=null){
            userToUpdate.setName(user.getName());
            userToUpdate.setPassword(user.getPassword());
            User user1 = userRepository.save(userToUpdate);
            databaseBackup.notifyListeners("UPDATE utilizador SET nome = '" + user.getName()+"', password = '"+ user.getPassword()+"' WHERE username ='"+ user1.getUsername()+"'");
            return user1;
        } else {
            throw new CustomException("This user was deleted.", HttpStatus.NOT_FOUND);
        }
    }
    @Transactional
    public void deleteUser(String username){
        try{
            User user = userRepository.findByUsername(username);
            if (user == null) throw new CustomException("This user was deleted.", HttpStatus.NOT_FOUND);
            reservationService.deleteReservationsByUser(user);
            userRepository.delete(user);
            databaseBackup.notifyListeners("DELETE FROM utilizador WHERE username = '" + username + "'");
        }catch (Exception e){
            throw new CustomException("Couldn't delete the user.", HttpStatus.NOT_ACCEPTABLE);
        }
    }

    public List<User> getAllUsers(){
        return userRepository.findAllByAdministrador(0);
    }



    public User getUser(HttpServletRequest request){
        String username = JWTUtil.extractUsernameFromToken(request);
        return userRepository.findByUsername(username);
    }

}
