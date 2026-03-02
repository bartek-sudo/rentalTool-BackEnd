package com.example.rentalTool_BackEnd.user.security.service;

import com.example.rentalTool_BackEnd.user.model.User;
import com.example.rentalTool_BackEnd.user.model.enums.UserType;
import com.example.rentalTool_BackEnd.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        final User user = userService.getUserByEmail(email);

        // Weryfikację emaila sprawdzamy ręcznie w AccountAuthenticationProvider po sprawdzeniu hasła
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                true, // enabled = true (weryfikację sprawdzamy później)
                true, // accountNonExpired
                true, // credentialsNonExpired
                !user.isBlocked(), // accountNonLocked
                getAuthorities(user.getUserType())
        );
    }
    private Collection<? extends GrantedAuthority> getAuthorities(UserType userRole){
        return userRole.getAuthorities().stream()
                .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
}
