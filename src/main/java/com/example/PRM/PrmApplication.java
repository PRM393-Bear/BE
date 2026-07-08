package com.example.PRM;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableKafka
@SpringBootApplication(exclude = {
		org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration.class
})
public class PrmApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrmApplication.class, args);
	}

    @Bean
    public org.springframework.web.client.RestClient.Builder restClientBuilder() {
        return org.springframework.web.client.RestClient.builder();
    }

    @Value("${MAIL_HOST:NOT_FOUND}")
    private String mailHost;

    @PostConstruct
    public void test() {
        System.out.println("MAIL_HOST = " + mailHost);
    }

}
