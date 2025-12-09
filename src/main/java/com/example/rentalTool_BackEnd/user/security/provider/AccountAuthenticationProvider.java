package com.example.rentalTool_BackEnd.user.security.provider;

import com.example.rentalTool_BackEnd.user.model.User;
import com.example.rentalTool_BackEnd.user.security.exception.EmailNotVerifiedException;
import com.example.rentalTool_BackEnd.user.security.exception.InvalidCredentialsException;
import com.example.rentalTool_BackEnd.user.security.service.CustomUserDetailsService;
import com.example.rentalTool_BackEnd.user.service.UserService;
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
    private final UserService userService;

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        final String password = userDetails.getPassword();
        final String credentials = (String) authentication.getCredentials();

        // KROK 1: Walidacja - sprawdź czy dane nie są null
        if(credentials == null || password == null){
            throw new InvalidCredentialsException("Credentials are null or empty");
        }
        
        // KROK 2: Sprawdź poprawność hasła
        if(!passwordEncoder.matches(credentials, password)){
            throw new InvalidCredentialsException("Typed password is wrong");
        }
        
        // KROK 3: Sprawdź czy email jest zweryfikowany (dopiero po sprawdzeniu hasła)
        try {
            User user = userService.getUserByEmail(userDetails.getUsername());
            if(!user.isVerified()){
                throw new EmailNotVerifiedException("Email address is not verified. Please check your email and verify your account.");
            }
        } catch (com.example.rentalTool_BackEnd.user.exception.UserNotFoundException e) {
            throw new InvalidCredentialsException("User not found");
        }
    }

    @Override
    protected UserDetails retrieveUser(String email, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        return customUserDetailsService.loadUserByUsername(email);
    }
}
