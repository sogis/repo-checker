package ch.so.agi.repochecker;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
public class MainController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${app.workDirectory}")
    private String workDirectory;
    
    @Value("${app.workDirectoryPrefix}")
    private String workDirectoryPrefix;

    @Autowired
    CheckService checkService;
        
    @GetMapping("/ping")
    public ResponseEntity<String> ping()  {
        return new ResponseEntity<String>("interlis-repo-checker", HttpStatus.OK);
    }
        
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String index() throws IOException {
        String content = checkService.getHtmlString();
        
        // Man kennt die Url der Anwendung erst hier (in einem Controller).
        // Andere Variante wäre eine Env-Variable und bereits in der Transformation korrekt
        // setzen.
        String html = content.replace("href=\"/results", "href=\""+getHost()+"/results");
        return html;
    }
    
    @GetMapping("/results/{key}/{filename}") 
    public ResponseEntity<?> getLog(@PathVariable String key, @PathVariable String filename) {        
        var mediaType = new MediaType("text", "plain", StandardCharsets.UTF_8);

        try {
            var logFile = Paths.get(workDirectory, key, filename).toFile();
            var is = new FileInputStream(logFile);

            return ResponseEntity.ok().header("Content-Type", "charset=utf-8")
                    .contentLength(logFile.length())
                    .contentType(mediaType)
                    .body(new InputStreamResource(is));

        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);  
        }
    }

    @Scheduled(cron="${app.checkCronExpression}")
    //@Scheduled(cron="0 */4 * * * *")
    private void checkRepos() {
        log.info("Check repositories...");
        checkService.checkRepos();
    }

    @Scheduled(cron="0 0/2 * * * *")
    private void cleanUp() {    
        log.debug("Deleting old files...");
        java.io.File[] tmpDirs = new java.io.File(workDirectory).listFiles();
        if(tmpDirs!=null) {
            for (java.io.File tmpDir : tmpDirs) {
                if (tmpDir.getName().startsWith(workDirectoryPrefix)) {
                    try {
                        FileTime creationTime = (FileTime) Files.getAttribute(Paths.get(tmpDir.getAbsolutePath()), "creationTime");                    
                        Instant now = Instant.now();
                        
                        long fileAge = now.getEpochSecond() - creationTime.toInstant().getEpochSecond();
                        if (fileAge > 60*60*4) {
                            log.info("deleting {}", tmpDir.getAbsolutePath());
                            FileSystemUtils.deleteRecursively(tmpDir);
                        }
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
    }
    
    private String getHost() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    }
}
