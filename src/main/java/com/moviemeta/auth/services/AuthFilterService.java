package com.moviemeta.auth.services;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.validation.constraints.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Service
public class AuthFilterService extends OncePerRequestFilter {

    private final JwtService jwtService; // for dealing with access token

    private final UserDetailsService userDetailsService; // to load user specific data

    public AuthFilterService(JwtService jwtService, UserDetailsService userDetailsService){
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }


    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain)
    throws ServletException, IOException {

        // extract the Http authorization Header
        final String authHeader = request.getHeader("Authorization");

        //industry convention to start jwt with "Bearer " and then the token starts
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            //if no jwt or auth header then simply pass on to next filter (which will eventually reject the request as no token)
            filterChain.doFilter(request,response);
            //do nothing on returning back
            return;
        }

        //if jwt is present, extract it
        String jwt = authHeader.substring(7);//ignoring "Bearer "

        //extract username from jwt
        String username = jwtService.extractUsername(jwt);

        //if user is not null then check if user is not authenticated already
        //SecurityContextHolder holds the context of the authentication of the user
        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){

            //get user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            //check validity of token
            if(jwtService.isTokenValid(jwt,userDetails)){

                //Creates a UsernamePasswordAuthenticationToken, which is a Spring Security authentication object.
                //Spring Security requires an authentication object in its SecurityContext to authorize users for secured endpoints.
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        //The authenticated user
                        userDetails,
                        //The credentials (password is not needed here because authentication is based on the JWT, not credentials).
                        null,
                        //The user's roles/permissions, used for authorization later.
                        userDetails.getAuthorities()
                );

                //Adds additional details about the request to the authentication object
                //These details can be used for logging, auditing, or additional security checks.
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                //Store the authentication object (authenticationToken) in the SecurityContext
                //the SecurityContext is a thread-local storage that holds authentication information for the current request.
                //Spring Security uses the SecurityContext to check if a user is authenticated and authorized to access resources.
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
