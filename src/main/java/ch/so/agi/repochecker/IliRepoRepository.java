package ch.so.agi.repochecker;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import ch.so.agi.repochecker.model.IliRepo;

@Repository
public class IliRepoRepository {
    private Map<String, IliRepo> repositories;

    public IliRepoRepository() {
        repositories = new HashMap<>();
    }
    
    public void save(IliRepo iliRepo) {
        repositories.put(iliRepo.ident(), iliRepo);
    }
    
    public IliRepo findIliRepoByIdent(String ident) {
        return repositories.get(ident);
    }
    
    public Map<String, IliRepo> findAll() {
        return repositories;
    }
}
