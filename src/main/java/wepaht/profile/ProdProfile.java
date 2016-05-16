package wepaht.profile;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import wepaht.domain.User;
import wepaht.repository.UserRepository;

import javax.annotation.PostConstruct;
import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.context.annotation.Bean;
import wepaht.Application;

@Configuration
@Profile(value = "prod")
public class ProdProfile {

    @Autowired
    private UserRepository userRepository;

    Logger log = Logger.getLogger(Application.class.getName());

    @PostConstruct
    public void init() {
        User admin = new User();
        admin.setRole("ADMIN");
        admin.setUsername("admin");
        admin.setPassword("admin");
        userRepository.save(admin);
    }

    @Bean
    public DataSource prodDataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        log.info("Initializing postgreSQL database");

        URI dbUri;

        try {
            dbUri = new URI(databaseUrl);

        } catch (URISyntaxException e) {
            log.error(String.format("Invalid DATABASE_URL: %s", databaseUrl), e);
            return null;
        }

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ":"
                + dbUri.getPort() + dbUri.getPath();

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
