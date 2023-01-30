package ch.so.agi.repochecker;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@Controller
public class MainController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private XmlMapper xmlMapper;

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
        return content;
    }
    
    @GetMapping("results/{key}/{filename}") 
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

    // TODO
    // check cron
    
    // TODO
    // cleaner
}
