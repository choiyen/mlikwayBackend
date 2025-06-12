package project.MilkyWay.Config;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class WebSecurityConfig {

    @Value("${released.URL}")
    String releasesURL;

    @Value("${released.URL2}")
    String releaseURL2;

    @Value("${released.URL3}")
    String releaseURL3s;

    @Value("${released.URL4}")
    String releaseURL4s;

    @Value("${released.URL5}")
    String releaseURL5s;

    @PostConstruct
    public void printUrls() {
        System.out.println("releasesURL = " + releasesURL);
        System.out.println("releaseURL2 = " + releaseURL2);
        System.out.println("releaseURL3s = " + releaseURL3s);
        System.out.println("releaseURL4s = " + releaseURL4s);
        System.out.println("releaseURL5s = " + releaseURL5s);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                // 폼 로그인 비활성화(필요시 api 로그인 구현 필요)
                .formLogin(form -> form.disable())
                // 세션 정책 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                // 인증 실패 시 리다이렉트 대신 401 에러 응답
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"error\":\"Unauthorized\"}");
                        })
                )
                // 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/api/auth/**",
                                "/api/board/**",
                                "/api/time/**",
                                "/api/comment/**",
                                "/api/inqurie/**",
                                "/api/notice/**",
                                "/api/question/**",
                                "/api/reserve/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList(
                releasesURL,
                releaseURL2,
                releaseURL3s,
                releaseURL4s,
                releaseURL5s
        ));
        config.setAllowedMethods(Arrays.asList("HEAD", "POST", "GET", "DELETE", "PUT", "PATCH", "OPTIONS"));
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
