package wepaht.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import wepaht.domain.User;
import wepaht.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Component
public class JpaAuthenticationProvider implements AuthenticationProvider{

    @Autowired
    private UserRepository userRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getPrincipal().toString();
        String password = authentication.getCredentials().toString();

        User user = userRepository.findByUsername(username);

        if (user == null || !BCrypt.hashpw(password, user.getSalt()).equals(user.getPassword())) {
            throw new AuthenticationException("Unable to authenticate user " + username){};
        }

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("STUDENT"));
        if (user.getRole().toUpperCase() != "STUDENT") grantedAuthorities.add(new SimpleGrantedAuthority(user.getRole()));

        return new UsernamePasswordAuthenticationToken(user.getUsername(), password, grantedAuthorities);
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
