package com.whenmo.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CurrentUserAdvice {

    private final SecurityFilterProperties props;

    public CurrentUserAdvice(SecurityFilterProperties props) {
        this.props = props;
    }

    @ModelAttribute("currentUser")
    public String currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null)
            return null;

        Object cred = session.getAttribute(props.getSessionKey());
        return cred == null ? null : cred.toString();
    }

    @ModelAttribute("isAdmin")
    public Boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null)
            return null;

        Object isAdmin = session.getAttribute("isAdmin");
        return isAdmin instanceof Boolean ? (Boolean) isAdmin : false;
    }
}
