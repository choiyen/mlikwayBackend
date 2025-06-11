package project.MilkyWay.ComonType.Swagger;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "MikyWay Project With OpenAPI",
                description = "밀키웨이 프로젝트의 API 생성",
                version = "1.0"
        )
)
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info().title("MikyWay Project API")
                        .version("1.0")
                        .description("밀키웨이 프로젝트의 API 문서"));
    }
}
