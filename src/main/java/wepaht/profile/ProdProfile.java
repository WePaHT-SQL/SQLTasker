package wepaht.profile;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import wepaht.domain.User;
import wepaht.repository.UserRepository;

import javax.annotation.PostConstruct;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.annotation.Bean;

@Configuration
@Profile(value = "prod")
public class ProdProfile {

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void init() {
        User admin = new User();
        admin.setRole("ADMIN");
        admin.setUsername("admin");
        admin.setPassword("admin");
        userRepository.save(admin);
    }

    @Bean
    public BasicDataSource dataSource() throws URISyntaxException {
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(dbUrl);
        basicDataSource.setUsername(username);
        basicDataSource.setPassword(password);

        return basicDataSource;
    }
}
