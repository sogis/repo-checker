package ch.so.agi.repochecker.model;

import java.util.HashMap;
import java.util.Map;

public record IliRepo(String ident, Map<Check, Result> results) {
//    private String ident;
//    private Map<Check, Result> results;
//    
//    public IliRepo() {
//        results = new HashMap<>();
//    }
//    
//    public String getIdent() {
//        return ident;
//    }
//    public void setIdent(String ident) {
//        this.ident = ident;
//    }
//    public Map<Check, Result> getResults() {
//        return results;
//    }
//    public void setResults(Map<Check, Result> results) {
//        this.results = results;
//    }
}
