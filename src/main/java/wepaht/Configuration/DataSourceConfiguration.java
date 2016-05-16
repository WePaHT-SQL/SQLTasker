package wepaht.Configuration;

// Reference: http://stackoverflow.com/questions/33633243/connecting-to-heroku-postgres-from-spring-boot

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import wepaht.Application;


@Configuration
public class DataSourceConfiguration {
    
    //Logger log = Logger.getLogger(Application.class.getName());
    
    @Bean
    @Profile("prod")
    public DataSource prodDataSource() {
        String databaseUrl= System.getenv("DATABASE_URL");
        //log.info("Initializing postgreSQL database");
        
        URI dbUri;
        
        try {
            dbUri = new URI(databaseUrl);
            
        } catch (URISyntaxException e) {
            //log.error(String.format("Invalid DATABASE_URL: %s", databaseUrl), e);
            return null;
        }
        
        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + 
                dbUri.getPort() + dbUri.getPath();
        
        org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(dbUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setTestOnBorrow(true);
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnReturn(true);
        dataSource.setValidationQuery("SELECT 1");
        
        return dataSource;
    }
}
