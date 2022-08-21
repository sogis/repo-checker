package ch.so.agi.repochecker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import ch.so.agi.repochecker.model.Check;
import ch.so.agi.repochecker.model.IliRepo;
import ch.so.agi.repochecker.model.IliRepos;
import ch.so.agi.repochecker.model.Result;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
    
    @Autowired
    private XmlMapper xmlMapper;
    
    @Value("${app.workDirectory}")
    private String workDirectory;

    @Value("${app.workDirectoryPrefix}")
    private String workDirectoryPrefix;
        
    @GetMapping("/ping")
    public ResponseEntity<String> ping()  {
        return new ResponseEntity<String>("repo-checker", HttpStatus.OK);
    }
    
//    @GetMapping("/fubar")
//    public ResponseEntity<String> fubar() throws IOException  {
//        
//        checker.checkRepos();
//        return new ResponseEntity<String>("fubar", HttpStatus.OK);
//    }
    
    @GetMapping(value="/repositories", produces=MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> repositories() throws IOException  {  
        var iliRepoList = updateLogFileLocation(iliRepoRepository.findAll());
        var iliRepos = new IliRepos();
        iliRepos.setIliRepos(iliRepoList);
        var xmlString = xmlMapper.writeValueAsString(iliRepos);
        return new ResponseEntity<String>(xmlString, HttpStatus.OK);
    }
    
    @GetMapping("/logs/{key}/{filename}") 
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
    
    @GetMapping("/")
    public String show(Model model) {
//        {
//            Map<Check, Result> resultMap = Map.of(
//                    Check.ILISITE_XML,new Result(true, null), 
//                    Check.ILIMODELS_XML, new Result(false, null));
//            IliRepo iliRepo = new IliRepo("https://models.interlis.ch", resultMap);
//            iliRepoRepository.save(iliRepo);
//        }
//        
//        {
//            Map<Check, Result> resultMap = Map.of(
//                    Check.ILISITE_XML,new Result(true, null), 
//                    Check.ILIMODELS_XML, new Result(true, null));
//            IliRepo iliRepo = new IliRepo("https://geo.so.ch/models", resultMap);
//            iliRepoRepository.save(iliRepo);
//        }
        
        var iliRepoList = updateLogFileLocation(iliRepoRepository.findAll());
        Collections.sort(iliRepoList, new Comparator<IliRepo>() {
            @Override
            public int compare(IliRepo o1, IliRepo o2) {
                var string0 = o1.getIdent().toLowerCase();
                var string1 = o2.getIdent().toLowerCase();
                if (string0.contains("https")) {
                    string0 = string0.substring(8);
                } else {
                    string0 = string0.substring(7);
                }
                if (string1.contains("https")) {
                    string1 = string1.substring(8);
                } else {
                    string1 = string1.substring(7);
                }
                return string0.compareTo(string1);
            }
        });
        
        model.addAttribute("repositories", iliRepoList);
        return "gui";
    } 

    @Scheduled(cron="${app.checkCronExpression}")
    //@Scheduled(cron="0 */2 * * * *")
    private void checkRepos() {
        try {
            log.info("check repos...");
            checker.checkRepos();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e.getMessage());
        } 
    }
    
    @Scheduled(cron="0 0/2 * * * *")
    private void cleanUp() {    
        log.debug("cleaner...");
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
    
    private List<IliRepo> updateLogFileLocation(List<IliRepo> iliRepoList) {
        for (IliRepo iliRepo : iliRepoList) {
            for (Map.Entry<Check, Result> entry : iliRepo.getResults().entrySet()) {
                String logFile = entry.getValue().getLogFile();
                entry.getValue().setLogFile(getLogFileLocation(logFile));
            }
        }
        return iliRepoList;
    }
    
    private String getLogFileLocation(String logFileName) {
        String LOG_ENDPOINT = "logs";
         return fixUrl(getHost() + "/" + LOG_ENDPOINT + "/" + Utils.getLogFileUrlPathElement(logFileName));
    }

    private String fixUrl(String url) {
        return url.replaceAll("(?<=[^:\\s])(\\/+\\/)", "/");
    }

    private String getHost() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    }
}
