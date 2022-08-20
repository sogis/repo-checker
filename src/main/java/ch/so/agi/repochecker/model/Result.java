package ch.so.agi.repochecker.model;

public class Result {
    private boolean valid;
    
    private String logFile;
    
    public Result(boolean valid, String logFile) {
        this.valid = valid;
        this.logFile = logFile;
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
}
