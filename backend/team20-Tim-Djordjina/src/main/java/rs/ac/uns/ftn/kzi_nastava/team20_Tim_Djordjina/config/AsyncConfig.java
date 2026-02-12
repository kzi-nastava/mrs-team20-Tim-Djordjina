package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // Enables @Async annotation for asynchronous email sending
}
