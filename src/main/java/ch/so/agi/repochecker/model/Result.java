package ch.so.agi.repochecker.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

public class Result {
    private boolean valid;
    private String logFile;
    private Date lastUpdate;
    
    public Result(boolean valid, String logFile) {
        this.valid = valid;
        this.logFile = logFile;
    }

    public Result(boolean valid, String logFile, Date lastUpdate) {
        this.valid = valid;
        this.logFile = logFile;
        this.lastUpdate = lastUpdate;
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

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }    
}
