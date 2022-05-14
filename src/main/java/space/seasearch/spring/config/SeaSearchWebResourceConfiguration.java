package space.seasearch.spring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import space.seasearch.telegram.photo.PhotoPath;

@Configuration
public class SeaSearchWebResourceConfiguration implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(final ResourceHandlerRegistry registry) {
    // Для того, чтобы скачивать фоточки.......(не нюдсы)
    registry.addResourceHandler(PhotoPath.PATH_TO_DATABASE + "**")
        .addResourceLocations("file:///" + System.getProperty("user.dir") + PhotoPath.PATH_TO_DATABASE);
  }
}
