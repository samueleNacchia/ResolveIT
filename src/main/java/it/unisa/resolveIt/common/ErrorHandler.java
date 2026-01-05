package it.unisa.resolveIt.common;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.Set;


@ControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(Exception.class)
    public String handleError(HttpServletRequest request, Exception ex) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());

            if (roles.contains("GESTORE")) {
                return "redirect:/gestore";
            } else if (roles.contains("OPERATORE")) {
                return "redirect:/ticket/operatore-home";
            } else if (roles.contains("CLIENTE")) {
                return "redirect:/ticket/home";
            }
        }

        return "redirect:/login-form";
    }
}

