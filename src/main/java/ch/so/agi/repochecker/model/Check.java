package ch.so.agi.repochecker.model;

import java.util.Date;

public record Check(CheckType type, Boolean success, String logfile, Date lastUpdate) {}
