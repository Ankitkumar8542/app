package com.musicPlayer.app.config;


// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// import org.springframework.web.filter.CorsFilter;

// import java.util.List;

// // @Configuration
// // public class CorsConfig {

// //     @Bean
// //     public CorsFilter corsFilter() {
// //         CorsConfiguration config = new CorsConfiguration();
// //         config.setAllowCredentials(true);
// //         config.setAllowedOriginPatterns(List.of("*"));
// //         config.setAllowedHeaders(List.of("*"));
// //         config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
// //         config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
// //         config.setMaxAge(3600L);

// //         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
// //         source.registerCorsConfiguration("/**", config);
// //         return new CorsFilter(source);
// //     }
// // }




// @Configuration
// @EnableWebSecurity
// public class SecurityConfig {

//     @Bean
//     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//         http
//             .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ← MUST be first
//             .csrf(csrf -> csrf.disable())
//             .authorizeHttpRequests(auth -> auth
//                 .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // ← OPTIONS preflight allow
//                 .requestMatchers("/api/auth/**").permitAll()
//                 .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
//                 .anyRequest().authenticated()
//             )
//             // ... JWT filter etc
//         return http.build();
//     }

//     @Bean
//     public CorsConfigurationSource corsConfigurationSource() {
//         CorsConfiguration config = new CorsConfiguration();
//         config.setAllowCredentials(true);
//         config.setAllowedOriginPatterns(List.of("*"));
//         config.setAllowedHeaders(List.of("*"));
//         config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
//         config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
//         config.setMaxAge(3600L);

//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         source.registerCorsConfiguration("/**", config);
//         return source;
//     }
// }
