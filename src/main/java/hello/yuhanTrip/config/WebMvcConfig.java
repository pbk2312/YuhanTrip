package hello.yuhanTrip.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {


    //application.properties에 설정한 "uploadPath" 프로퍼티 값을 읽어옴
    @Value("${itemImgLocation}")
    String itemImgLocation;


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + itemImgLocation + "/");
    }
}
