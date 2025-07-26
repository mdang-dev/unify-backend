package com.unify.app;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableCaching
public class UnifyBackendApplication {

  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.configure().directory("./").ignoreIfMissing().load();
    if (dotenv != null) {
      dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    }
    SpringApplication.run(UnifyBackendApplication.class, args);
  }
}
