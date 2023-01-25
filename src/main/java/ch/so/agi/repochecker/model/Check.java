package ch.so.agi.repochecker.model;

import java.util.Date;

public record Check(CheckType type, boolean success, String logfile, Date lastUpdate) {}
