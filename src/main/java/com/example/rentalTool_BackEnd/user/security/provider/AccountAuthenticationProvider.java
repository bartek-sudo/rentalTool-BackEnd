package com.example.rentalTool_BackEnd.user.security.provider;

import com.example.rentalTool_BackEnd.user.security.exception.InvalidCredentialsException;
import com.example.rentalTool_BackEnd.user.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        final String password = userDetails.getPassword();

        final String credentials = (String) authentication.getCredentials();

        if(credentials == null || password == null){
            throw new InvalidCredentialsException("Credentials are null or empty");
        }
        if(!passwordEncoder.matches(credentials,password)){
            throw new InvalidCredentialsException("Typed password is wrong");
        }
    }

    @Override
    protected UserDetails retrieveUser(String email, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        return customUserDetailsService.loadUserByUsername(email);
    }
}
