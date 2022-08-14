package ch.so.agi.repochecker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RepoCheckerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RepoCheckerApplication.class, args);
	}

}
