package com.pujan.userservice.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private RestAuthenticationSuccessHandler restAuthenticationSuccessHandler;

    @Autowired
    private RestAuthenticationFailureHandler restAuthenticationFailureHandler;



    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authTokenFilter(){
        return new AuthTokenFilter();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        String hierarchy = "ROLE_SUPER_ADMIN > ROLE_ADMIN \n ROLE_ADMIN > ROLE_EDITOR \n ROLE_EDITOR > ROLE_USER ";

        return RoleHierarchyImpl.fromHierarchy(hierarchy);
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy){
        DefaultMethodSecurityExpressionHandler defaultMethodSecurityExpressionHandler = new DefaultMethodSecurityExpressionHandler();
        defaultMethodSecurityExpressionHandler.setRoleHierarchy(roleHierarchy);
        return defaultMethodSecurityExpressionHandler;
    }


    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http){
        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(restAuthenticationSuccessHandler)
                .failureHandler(restAuthenticationFailureHandler)
                .permitAll()
        );

        return http
                .authorizeHttpRequests(authorizerequests -> authorizerequests
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/register").permitAll()
//                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception .authenticationEntryPoint(unauthorizedHandler))
                .csrf(csrf-> csrf.disable())
                .addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config){

        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



}
