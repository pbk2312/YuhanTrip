package hello.yuhanTrip.config;


import com.siot.IamportRestClient.IamportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class Appconfig {

    String apiKey = "2203230868760025";
    String secretKey = "Secret:4ORW20rni1J3ViPwIByqLBbKqQtk25ofjkyIAYmz9tgd3v8ANrHq1MUO62HaSQM8GBcQgfEZiJa5P1uW";

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @Bean
    public IamportClient iamportClient() {
        return new IamportClient(apiKey, secretKey);
    }
}
