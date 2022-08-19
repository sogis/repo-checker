package ch.so.agi.repochecker.model;

import java.io.File;

public record Result(boolean valid, File logFile) {
    
    // TODO more...
    
//    private boolean valid;
//
//    public boolean isValid() {
//        return valid;
//    }
//
//    public void setValid(boolean valid) {
//        this.valid = valid;
//    }
}
