package es.lavanda.filebot.executor.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.context.ContextInstanceDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import es.lavanda.lib.common.config.CommonConfigurator;

@Configuration
@EnableMongoAuditing
@Import(CommonConfigurator.class)
@EnableAutoConfiguration(exclude = { ContextInstanceDataAutoConfiguration.class })
public class AppBeans {

    @Bean
    public ExecutorService executorServiceBean() {
        return Executors.newFixedThreadPool(4);
    }
}
