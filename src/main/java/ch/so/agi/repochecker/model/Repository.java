package ch.so.agi.repochecker.model;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

public record Repository(String endpoint, @JacksonXmlElementWrapper(localName = "checks") @JacksonXmlProperty(localName = "check") List<Check> checks) {}
