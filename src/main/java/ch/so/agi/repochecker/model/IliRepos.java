package ch.so.agi.repochecker.model;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "IliRepos")
public class IliRepos {
    private List<IliRepo> iliRepos;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "IliRepo")
    public List<IliRepo> getIliRepos() {
        return iliRepos;
    }

    public void setIliRepos(List<IliRepo> iliRepos) {
        this.iliRepos = iliRepos;
    }
}
