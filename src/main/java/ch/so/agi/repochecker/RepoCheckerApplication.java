package ch.so.agi.repochecker;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.filter.ForwardedHeaderFilter;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@Configuration
@EnableScheduling
@SpringBootApplication
public class RepoCheckerApplication {

    @Value("${app.httpProxyHost}")
    private String httpProxyHost;

    @Value("${app.httpProxyPort}")
    private String httpProxyPort;

    @Value("${app.httpProxyUser}")
    private String httpProxyUser;

    @Value("${app.httpProxyPassword}")
    private String httpProxyPassword;

    @Value("${app.httpNonProxyHosts}")
    private String httpNonProxyHosts;

	public static void main(String[] args) {
		SpringApplication.run(RepoCheckerApplication.class, args);
	}

    @Bean
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    @Bean 
    XmlMapper xmlMapper() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return xmlMapper; 
    }

    // Anwendung ist fertig gestartet.
    @Bean
    CommandLineRunner init(CheckService checkService) {
        return args -> {
            if (httpProxyHost != null && httpProxyHost.trim().length() != 0) {
                System.setProperty("http.proxyHost", httpProxyHost);
                System.setProperty("https.proxyHost", httpProxyHost);
            }
            
            if (httpProxyPort != null && httpProxyPort.trim().length() != 0) {
                System.setProperty("http.proxyPort", httpProxyPort);
                System.setProperty("https.proxyPort", httpProxyPort);
            }
            
            if ((httpProxyUser != null && httpProxyUser.trim().length() != 0) && (httpProxyPassword != null && httpProxyPassword.trim().length() != 0)) {
                System.setProperty("http.proxyUser", httpProxyUser);
                System.setProperty("https.proxyUser", httpProxyUser);
                
                System.setProperty("http.proxyPassword", httpProxyPassword);
                System.setProperty("https.proxyPassword", httpProxyPassword);
                
                Authenticator.setDefault(new Authenticator() {
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(httpProxyUser, httpProxyPassword.toCharArray());
                    }
                });
            }
            
            if (httpNonProxyHosts != null && httpNonProxyHosts.trim().length() != 0) {
                System.setProperty("http.nonProxyHosts", httpNonProxyHosts);
                System.setProperty("https.nonProxyHosts", httpNonProxyHosts);
            }

            checkService.checkRepos();
        };
    }

}
