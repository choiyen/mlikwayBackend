package project.MilkyWay.Config;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionLister implements HttpSessionListener
{
    @Autowired
    private SessionManager sessionManager;

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        // sessionMap에서 해당 세션이 사라지도록 username 찾아서 제거
        // username은 세션 속성에 저장해둔 상태여야 함
        String username = (String) session.getAttribute("userId");
        if (username != null) {
            sessionManager.removeSession(username);
        }
    }

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        // 세션 생성 시 별도 처리 필요 없으면 비워둠
    }
}
