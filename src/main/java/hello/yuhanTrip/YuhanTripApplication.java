package hello.yuhanTrip;

import hello.yuhanTrip.service.Accomodation.AccommodationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "hello.yuhanTrip.repository")
public class YuhanTripApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuhanTripApplication.class, args);
    }


}
