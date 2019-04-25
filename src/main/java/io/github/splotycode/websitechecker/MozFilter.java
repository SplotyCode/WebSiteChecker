package io.github.splotycode.websitechecker;

import java.net.URL;
import java.util.function.Predicate;

public class MozFilter implements Predicate<URL> {
    @Override
    public boolean test(URL url) {
        if (url == null) return false;
        if (!url.getHost().endsWith("mozilla.org")) return false;
        if (url.getHost().equals("bugzilla.mozilla.org")) return false;
        if (url.getHost().equals("hg.mozilla.org")) return false;
        String path = url.getPath();
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String[] split = path.split("/");
        if (url.getHost().equals("developer.mozilla.org") && split.length == 3 && split[1].equals("profiles")) return false;
        if (url.getHost().equals("support.mozilla.org") && split.length == 3 && split[1].equals("questions")) return false;
        if (url.getHost().equals("addons.mozilla.org")
                && split.length == 4
                && (split[2].equals("addon") || split[2].equals("user"))) return false;
        return true;
    }
}
