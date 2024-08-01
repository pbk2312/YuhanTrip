package hello.yuhanTrip.config;


import com.siot.IamportRestClient.IamportClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@Log4j2
public class Appconfig {

    String apiKey = "2203230868760025";
    String secretKey = "4ORW20rni1J3ViPwIByqLBbKqQtk25ofjkyIAYmz9tgd3v8ANrHq1MUO62HaSQM8GBcQgfEZiJa5P1uW";

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @Bean
    public IamportClient iamportClient() {
        log.info("API Key: {} " ,apiKey);
        log.info("Secret Key: {} " ,secretKey);
        return new IamportClient(apiKey, secretKey);
    }
}
