
package ru.Edje_7.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.Edje_7.security.JwtAuthenticationFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Swagger UI и документация 
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api-docs/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/webjars/**", "/swagger-resources/**").permitAll()

                        // 2. H2 Console
                        .requestMatchers("/h2-console/**").permitAll()

                        // 3. Actuator endpoints
                        .requestMatchers("/actuator/**").permitAll()

                        // 4. Публичные endpoint'ы API 
                        // ВАЖНО: context-path = /api, поэтому пути БЕЗ префикса /api
                        .requestMatchers("/auth/**").permitAll()           // Регистрация/логин
                        .requestMatchers("/public/**").permitAll()         // Публичные данные

                        // 5. Публичные GET запросы 
                        .requestMatchers(HttpMethod.GET, "/posts/**").permitAll()     // Чтение постов
                        .requestMatchers(HttpMethod.GET, "/tags/**").permitAll()       // Чтение тегов
                        .requestMatchers(HttpMethod.GET, "/comments/**").permitAll()  // Чтение комментариев
                        .requestMatchers(HttpMethod.GET, "/users/**").permitAll()     // Публичная информация о пользователях

                        // 6. Защищенные endpoint'ы (требуют аутентификации)
                        // POST, PUT, DELETE для постов требуют токен
                        .requestMatchers(HttpMethod.POST, "/posts/**").authenticated()       // Создание постов
                        .requestMatchers(HttpMethod.PUT, "/posts/**").authenticated()       // Изменение постов
                        .requestMatchers(HttpMethod.DELETE, "/posts/**").authenticated()    // Удаление постов
                        .requestMatchers(HttpMethod.POST, "/comments/**").authenticated()  // Создание комментариев
                        .requestMatchers(HttpMethod.PUT, "/comments/**").authenticated()  // Изменение комментариев
                        .requestMatchers(HttpMethod.DELETE, "/comments/**").authenticated()  // Удаление комментариев
                        .requestMatchers("/users/me/**").authenticated()  // Личные данные пользователя

                        // 7. Админские endpoint'ы
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/moderator/**").hasAnyRole("ADMIN", "MODERATOR")

                        // 8. Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));  // Для H2 console

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",  // React dev server
                "http://localhost:5173",  // Vite dev server
                "http://localhost:8081"   // Другие порты если есть
        ));
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "X-Requested-With",
                "Accept", "Origin", "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 1 час кеширования preflight

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}