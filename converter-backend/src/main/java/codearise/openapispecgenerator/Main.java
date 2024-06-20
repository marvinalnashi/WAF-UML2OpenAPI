package codearise.openapispecgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * The main class of the application.
 */
@SpringBootApplication(scanBasePackages = "codearise.openapispecgenerator")
@ImportResource("classpath:SpringConfig.xml")
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
