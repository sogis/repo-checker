package ch.so.agi.repochecker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.interlis.ili2c.CheckReposIlis;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CheckService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("#{'${app.repositories}'.split(',')}")
    private List<String> repositories;
    
    @Value("${app.workDirectory}")
    private String workDirectory;
    
    @Value("${app.workDirectoryPrefix}")
    private String workDirectoryPrefix;

    private String ILICACHE_FOLDER_PREFIX = ".ilicache_";

    public void checkRepos() throws IOException {
        for (String repository : repositories) {
            log.info("Checking: " + repository);
            checkRepo(repository.trim());
        } 
        
        // Resultate ins XML serialisieren. temp -> definitiv.
    }

    
    public synchronized void checkRepo(String repository) throws IOException { // TODO

        // EhiLogger (siehe ili2db wegen file listener).
        
        
        
        //new CheckReposIlis().checkRepoIlis(null, null);
        
        
        // Kann ich nach einer Prüfung den ilicache einfach löschen? Dann würde jedes Repo auf grüner Wiese beginnen.
    }
}
