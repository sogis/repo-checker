package ch.so.agi.repochecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import ch.so.agi.repochecker.model.IliRepo;
import ch.so.agi.repochecker.model.IliRepos;

@Repository
public class IliRepoRepository {
    private Map<String, IliRepo> repositories;

    public IliRepoRepository() {
        repositories = new HashMap<>();
    }
    
    public void save(IliRepo iliRepo) {
        repositories.put(iliRepo.getIdent(), iliRepo);
    }
    
    public IliRepo findIliRepoByIdent(String ident) {
        return repositories.get(ident);
    }
    
    public List<IliRepo> findAll() {
        // Momentan benötigten wir keine Map.
        return new ArrayList<IliRepo>(repositories.values());
    }
}
