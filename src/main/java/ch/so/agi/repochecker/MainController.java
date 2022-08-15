package ch.so.agi.repochecker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ch.so.agi.repochecker.model.Check;
import ch.so.agi.repochecker.model.IliRepo;
import ch.so.agi.repochecker.model.Result;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class MainController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CheckerService checker;
    
    @Autowired
    private IliRepoRepository iliRepoRepository;
    
    @GetMapping("/ping")
    public ResponseEntity<String> ping()  {
        return new ResponseEntity<String>("repo-checker", HttpStatus.OK);
    }
    
    @GetMapping("/fubar")
    public ResponseEntity<String> fubar() throws IOException  {
        checker.fubar();
        return new ResponseEntity<String>("fubar", HttpStatus.OK);
    }
    
    @GetMapping("/gui")
    public String show(Model model) {
        {
            Map<Check, Result> resultMap = Map.of(
                    Check.ILISITE_XML,new Result(true), 
                    Check.ILIMODELS_XML, new Result(false));
            IliRepo iliRepo = new IliRepo("https://models.interlis.ch", resultMap);
            iliRepoRepository.save(iliRepo);
        }
        
        {
            Map<Check, Result> resultMap = Map.of(
                    Check.ILISITE_XML,new Result(true), 
                    Check.ILIMODELS_XML, new Result(true));
            IliRepo iliRepo = new IliRepo("https://geo.so.ch/models", resultMap);
            iliRepoRepository.save(iliRepo);
        }
        
        
        
        
        model.addAttribute("repositories", iliRepoRepository.findAll());
        return "gui";
    } 

}
