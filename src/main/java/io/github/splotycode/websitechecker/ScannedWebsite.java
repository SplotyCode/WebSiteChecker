package io.github.splotycode.websitechecker;

import javafx.scene.effect.SepiaTone;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

public class ScannedWebsite extends UncheckedWebSite implements Serializable {

    private int errorCode;

    public int getErrorCode() {
        return errorCode;
    }

    public ScannedWebsite(int errorCode, String url, String caller) {
        super(url, caller);
        this.errorCode = errorCode;
    }

    public ScannedWebsite(int errorCode, String url, Set<String> caller) {
        super(url, caller);
        this.errorCode = errorCode;
    }
}
