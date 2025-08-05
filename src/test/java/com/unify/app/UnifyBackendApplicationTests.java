package com.unify.app;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UnifyBackendApplicationTests {

  @BeforeAll
  public static void setUpEnv() {
    Dotenv dotenv = Dotenv.configure().directory("./").ignoreIfMissing().load();
    if (dotenv != null) {
      dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    }
  }

  @Test
  void contextLoads() {}
}
