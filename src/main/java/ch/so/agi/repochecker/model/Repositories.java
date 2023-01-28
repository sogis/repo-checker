package ch.so.agi.repochecker.model;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "repositories")
public record Repositories(@JacksonXmlElementWrapper(useWrapping = false) @JacksonXmlProperty(localName = "repository")  List<Repository> repositories) {}
