package org.qwertech.awsstudy;


import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {Application.class})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ApplicationTests {

  @LocalServerPort
  protected int port;
  @Autowired
  private Application application;

  @Before
  public final void initIT() {
    RestAssured.port = port;
  }

  @After
  public final void resetMocks() {
    Mockito.reset();
  }

  @Test
  public void testAppInit() {
    Assertions.assertThat(application).isNotNull();
  }
}
