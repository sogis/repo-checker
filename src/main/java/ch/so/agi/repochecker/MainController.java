package ch.so.agi.repochecker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@Controller
public class MainController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private XmlMapper xmlMapper;

    @Autowired
    CheckService checkService;
    
    @GetMapping("/ping")
    public ResponseEntity<String> ping()  {
        return new ResponseEntity<String>("interlis-repo-checker", HttpStatus.OK);
    }
    
    @GetMapping(value="/foo", produces=MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> foo() throws IOException {
        

        var xmlString = xmlMapper.writeValueAsString(checkService.getCheckedRepositories());
        return new ResponseEntity<String>(xmlString, HttpStatus.OK);

    }
    
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String index() throws IOException {
        Path filePath = Path.of("/Users/stefan/tmp/results.html");
        String content = Files.readString(filePath);
        return content;
    }

    
    // TODO
    // cleaner
}
