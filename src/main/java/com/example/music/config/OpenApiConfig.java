package com.example.music.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI musicOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Music Playlist Management API")
                .version("1.0.0")
                .description("Playlists, shuffle strategies, recommendations, export and global player"));
    }
}
