package hello.yuhanTrip;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "hello.yuhanTrip.repository")
@EnableJpaAuditing
public class YuhanTripApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuhanTripApplication.class, args);
    }


}
