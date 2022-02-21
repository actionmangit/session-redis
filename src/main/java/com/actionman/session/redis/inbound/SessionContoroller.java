package com.actionman.session.redis.inbound;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessionContoroller {
    
    /**
     *
     */
    private static final String SESSION_KEY = "SESSION_KEY";

    @PostMapping("/session")
    public void addSession(
        HttpServletRequest request,
        @RequestBody SessionDto dto
    ) {
        HttpSession session = request.getSession();
        session.setAttribute(SESSION_KEY, dto.getValue());
    }

    @GetMapping("/session")
    public String findSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (String) session.getAttribute(SESSION_KEY);
    }
}
