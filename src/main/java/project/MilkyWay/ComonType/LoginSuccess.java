package project.MilkyWay.ComonType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.SecureRandom;

public class LoginSuccess
{
    public boolean isSessionExist(HttpServletRequest request) {
        // 세션이 존재하는지 확인 (세션이 없으면 null 반환)
        String userId = (String) request.getSession().getAttribute("userId");

        // 세션이 존재하면 true, 존재하지 않으면 false
        return userId != null;
    }
    public String generateRandomId(int length) {
        String chars = "ABCDEFGHIJKLNMOPQRSTUVWXYZ";
        String chars2 = chars.toLowerCase() + "0123456789";
        chars += chars2;
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder st = new StringBuilder();
        for (int i = 0; i < length; i++)
        {
            st.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }

        return st.toString();
    }
}
