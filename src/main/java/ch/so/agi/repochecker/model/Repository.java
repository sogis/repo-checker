package ch.so.agi.repochecker.model;

import java.util.List;

public record Repository(String endpoint, List<Check> checks) {}
