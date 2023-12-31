package pd.ticketline.utils;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import pd.ticketline.server.clientconnection.TCPServer;
import pd.ticketline.server.exceptionhandler.CustomException;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Date;

public class JWTUtil {
    private static final long EXPIRATION_MILLIS = 120000L;
    private static final SecretKey SECRET_KEY =
            Keys.secretKeyFor(SignatureAlgorithm.HS256);
    public static String generateToken(String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime()
                + EXPIRATION_MILLIS);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SECRET_KEY)
                .compact();
    }
    public static String getToken(HttpServletRequest request){
        String authorizationHeader = request.getHeader("Authorization");
        return authorizationHeader.substring(7);
    }

    public static String extractUsernameFromToken(HttpServletRequest request) {
        String token = getToken(request);
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public static boolean isTokenValid(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            return validateToken(token);
        }

        throw new CustomException("Missing or invalid Authorization header.", HttpStatus.UNAUTHORIZED);
    }

    private static boolean validateToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token);

            if(!claimsJws.getBody().getExpiration().after(new Date())) {
                TCPServer.removeClient(token);
                throw new CustomException("Token expired, login again.", HttpStatus.UNAUTHORIZED);
            }

            return true;
        } catch (Exception ex) {
            TCPServer.removeClient(token);
            throw new CustomException("Token expired, login again.", HttpStatus.UNAUTHORIZED);
        }
    }
}
