package io.github.splotycode.websitechecker;

import lombok.Getter;

import java.io.Serializable;
import java.util.Set;

@Getter
class ScannedWebsite extends UncheckedWebSite implements Serializable {

    private int errorCode;

    ScannedWebsite(int errorCode, String url, Set<String> caller) {
        super(url, caller);
        this.errorCode = errorCode;
    }

}
