package com.simplechat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@EnableJpaRepositories(basePackages = "com.simplechat.repository")
@EnableTransactionManagement
public class DataSourceConfig {

    @Value("${spring.datasource.url:}")
    private String databaseUrl;

    @Value("${spring.datasource.username:}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Value("${spring.datasource.driver-class-name:}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        String dbUrl = databaseUrl;
        String dbUser = username;
        String dbPass = password;
        
        // Render or Railway provides DATABASE_URL in postgres://... format
        String envDbUrl = System.getenv("DATABASE_URL");
        if (envDbUrl != null && (envDbUrl.startsWith("postgres://") || envDbUrl.startsWith("postgresql://"))) {
            try {
                URI dbUri = new URI(envDbUrl);
                String userPass = dbUri.getUserInfo();
                if (userPass != null) {
                    dbUser = userPass.split(":")[0];
                    if (userPass.split(":").length > 1) {
                        dbPass = userPass.split(":")[1];
                    }
                }
                dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + (dbUri.getPort() > 0 ? dbUri.getPort() : 5432) + dbUri.getPath();
                if (driverClassName == null || driverClassName.isEmpty()) {
                    driverClassName = "org.postgresql.Driver";
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        
        return DataSourceBuilder.create()
                .url(dbUrl)
                .username(dbUser)
                .password(dbPass)
                .driverClassName(driverClassName)
                .build();
    }
}
