package org.qwertech.awsstudy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Slf4j
@SpringBootApplication
@RestController
@RequiredArgsConstructor
public class Application {

  private static final Map<Integer, Product> products = new ConcurrentHashMap<>();

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public CommonsRequestLoggingFilter requestLoggingFilter() {
    CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
    loggingFilter.setIncludeClientInfo(true);
    loggingFilter.setIncludeQueryString(true);
    loggingFilter.setIncludePayload(true);
    loggingFilter.setMaxPayloadLength(64000);
    loggingFilter.setIncludeHeaders(true);
    return loggingFilter;
  }

  @GetMapping("/product/{id}")
  public ResponseEntity<Product> testGet(@PathVariable Integer id) {
    Product product = products.get(id);
    if (product == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(product);
  }

  @PutMapping("/product/{id}/add")
  public ResponseEntity<Void> add(@PathVariable Integer id, @RequestBody Product body) {
    products.put(id, body);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/product/{id}")
  public ResponseEntity<Void> delete(@PathVariable Integer id) {
    Product product = products.remove(id);
    if (product == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.noContent().build();
  }


  @Data
  public static class Product {

    private String name;
  }
}
