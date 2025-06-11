package project.MilkyWay.Config;


import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class SessionManager
{
    private final Map<String, HttpSession> sessionMap = new ConcurrentHashMap<>();

    public boolean registerSession(String username, HttpSession session) {
        if(sessionMap.containsKey(username))
        {
            return false;
        }
        sessionMap.put(username, session);
        return true;
    }

    public void removeSession(String username) {
        sessionMap.remove(username);
    }

    public boolean isLogginedIn(String username) {
        return sessionMap.containsKey(username);
    }
}
