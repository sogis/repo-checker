package ch.so.agi.repochecker.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

public class IliRepo {
    private String ident; 
    private Map<Check, Result> results;
    
    public IliRepo(String ident, Map<Check, Result> results) {
        super();
        this.ident = ident;
        this.results = results;
    }

    public IliRepo() {
        results = new HashMap<>();
    }
    
    @JacksonXmlProperty(localName = "identifier")
    public String getIdent() {
        return ident;
    }
    public void setIdent(String ident) {
        this.ident = ident;
    }
    public Map<Check, Result> getResults() {
        return results;
    }
    public void setResults(Map<Check, Result> results) {
        this.results = results;
    }
}
