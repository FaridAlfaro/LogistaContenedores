package com.transporte.ms_solicitudes.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        HikariConfig config = new HikariConfig();
        
        // Construir URL asegurando que use UTC
        String url = properties.getUrl();
        if (!url.contains("TimeZone")) {
            url = url + (url.contains("?") ? "&" : "?") + "TimeZone=UTC";
        }
        
        config.setJdbcUrl(url);
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());
        
        // Forzar UTC en cada nueva conexión
        config.setConnectionInitSql("SET timezone = 'UTC'");
        
        return new HikariDataSource(config);
    }
}

