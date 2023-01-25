package ch.so.agi.repochecker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/ping")
    public ResponseEntity<String> ping()  {
        return new ResponseEntity<String>("interlis-repo-checker", HttpStatus.OK);
    }
    
    
    /*
     * 
     * <repository>
     *   <endpoint>https://geo.so.ch/models/</endpoint>
     *   <check>
     *      <type>ilisite</type>
     *      <success>false</success>
     *      <logfile>/workdir/....</logfile>
     *   </check>
     * </repository>
     * 
     * 
     * 
     */

}
