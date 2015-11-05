package wepaht;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import profile.DevProfile;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@Import({DevProfile.class})
        public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
