package ch.so.agi.repochecker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class MainController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CheckerService checker;
    
    @GetMapping("/ping")
    public ResponseEntity<String> ping()  {
        return new ResponseEntity<String>("repo-checker", HttpStatus.OK);
    }
    
    @GetMapping("/fubar")
    public ResponseEntity<String> fubar() throws IOException  {
        checker.fubar();
        return new ResponseEntity<String>("fubar", HttpStatus.OK);
    }

}
