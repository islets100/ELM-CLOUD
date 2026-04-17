package team.tjusw.elm.feign;
import org.springframework.stereotype.Component;
@Component
public class UserClientFallback implements UserClient {
    @Override
    public boolean validateUser(String userId) {
        return true; 
    }
}