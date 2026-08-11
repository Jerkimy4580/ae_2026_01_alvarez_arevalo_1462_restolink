package com.restaurant.clientpreferencesservice.config

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/preferences", "/api/v1/preferences/**").authenticated()
                    .requestMatchers("/actuator/health").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }
            .exceptionHandling { handler ->
                handler.authenticationEntryPoint { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required")
                }
            }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOriginPatterns = listOf(
                "http://localhost",
                "http://localhost:*",
                "https://localhost",
                "capacitor://localhost",
                "http://107.20.92.91",
                "http://107.20.92.91:*"
            )
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin")
            exposedHeaders = listOf("Authorization")
            allowCredentials = true
            maxAge = 3600L
        }
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter { jwt: Jwt ->
            extractAuthorities(jwt)
        }
        return converter
    }

    private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
        val rawRoles = mutableListOf<String>()

        (jwt.claims["cognito:groups"] as? Collection<*>)?.filterIsInstance<String>()?.let { rawRoles.addAll(it) }
        (jwt.claims["groups"] as? Collection<*>)?.filterIsInstance<String>()?.let { rawRoles.addAll(it) }
        (jwt.claims["roles"] as? Collection<*>)?.filterIsInstance<String>()?.let { rawRoles.addAll(it) }

        val scope = jwt.claims["scope"] as? String
        if (!scope.isNullOrBlank()) {
            rawRoles.addAll(scope.split(Regex("\\s+")))
        }

        return rawRoles
            .distinct()
            .filter { it.isNotBlank() }
            .map { SimpleGrantedAuthority("ROLE_${it.uppercase()}") }
    }
}