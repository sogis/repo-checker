package ch.so.agi.repochecker;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.filter.ForwardedHeaderFilter;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@Configuration
@EnableScheduling
@ServletComponentScan
@SpringBootApplication
public class RepoCheckerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RepoCheckerApplication.class, args);
	}

    @Bean
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
    
    @Bean 
    XmlMapper xmlMapper() {
        return new XmlMapper(); 
    }
    
    // Anwendung ist fertig gestartet.
    @Bean
    CommandLineRunner init(CheckerService checker) {
        return args -> {
            checker.checkRepos();
        };
    }
}
