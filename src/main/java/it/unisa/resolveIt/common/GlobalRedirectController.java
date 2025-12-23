package it.unisa.resolveIt.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

@Controller
public class GlobalRedirectController {

    // Questo metodo cattura qualsiasi rotta non mappata dagli altri controller
    @GetMapping("/**")
    public String handleUnknownRoutes(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        if (roles.contains("GESTORE")) {
            return "redirect:/gestore";
        } else if(roles.contains("OPERATORE")) {
            return "redirect:/home";
        } else {
            return "redirect:/home";
        }
    }
}
