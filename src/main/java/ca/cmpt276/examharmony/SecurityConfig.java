package ca.cmpt276.examharmony;

import ca.cmpt276.examharmony.utils.CustomUserDetailService;
import ca.cmpt276.examharmony.utils.LoginSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;

import static org.springframework.security.config.Customizer.withDefaults;

//Configures Spring Security for application
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private LoginSuccessHandler successHandler;

    @Bean
    public CustomUserDetailService userDetailsService() {
        return new CustomUserDetailService();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        HeaderWriterLogoutHandler clearSiteData = new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(ClearSiteDataHeaderWriter.Directive.COOKIES));
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests( //Configure authorizations and permissions for users
                        authorize -> authorize
                        .requestMatchers("/reset-password").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")  //Users with role ADMIN can only access "/admin/.." urls
                        .requestMatchers("/instructor/**").hasRole("INSTRUCTOR")
                        .requestMatchers("/invigilator/**").hasRole("INVIGILATOR")
                        .requestMatchers("/forgot-password").permitAll()
//                                .requestMatchers("/forgot-password-form").permitAll()
                                .requestMatchers("/test/**").permitAll() //For testing
                        .anyRequest().authenticated()   //All other users must log in
                )
                .formLogin(form->form.loginPage("/login")
                        .successHandler(successHandler).permitAll())
                .logout((logout) -> logout
                        .addLogoutHandler(clearSiteData)
                        .logoutSuccessUrl("/login")
                        .permitAll())
                .httpBasic(withDefaults());
        return http.build();
    }
}
