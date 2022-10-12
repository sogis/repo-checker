package ch.so.agi.repochecker;

import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RepoCheckerApplicationTests {

    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
    }
    
    @Test
    public void index_Ok() throws Exception {        
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/", String.class))
                .contains("INTERLIS Repository Checker")
                .contains("models.kgk-cgc.ch");
    }

}
