package ch.so.agi.repochecker.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Result {
    private boolean valid;
    private String logFile;
    private LocalDateTime lastUpdate;
    
    public Result(boolean valid, String logFile) {
        this.valid = valid;
        this.logFile = logFile;
    }

    public Result(boolean valid, String logFile, Date lastUpdate) {
        this.valid = valid;
        this.logFile = logFile;
        this.lastUpdate = lastUpdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }    
}
