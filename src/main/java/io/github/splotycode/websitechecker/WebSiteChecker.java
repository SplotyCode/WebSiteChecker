package io.github.splotycode.websitechecker;

import io.github.splotycode.mosaik.util.ExceptionUtil;
import io.github.splotycode.mosaik.util.Pair;
import io.github.splotycode.mosaik.util.StringUtil;
import io.github.splotycode.mosaik.util.condition.Conditions;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class WebSiteChecker {

    public static URL toUrl(String str) {
        if (StringUtil.isEmpty(str)) return null;
        try {
            return new URL(str);
        } catch (MalformedURLException e) {
            System.out.println(str);
            ExceptionUtil.throwRuntime(e);
            return null;
        }
    }

    public WebSiteChecker(String... baseUrl) {
        this(Conditions.alwaysTrue(), baseUrl);
    }

    public WebSiteChecker(Predicate<URL> filter, String... baseUrl) {
        for (String base : baseUrl) {
            unchecked.put(base, new UncheckedWebSite(base, "__base__"));
        }
        this.filter = filter;
    }

    public void printResults(PrintStream stream) {
        stream.println("Checked " + checked.size() + " websites");
        int successful = 0;
        for (Map.Entry<String, ScannedWebsite> site : checked.entrySet()) {
            ScannedWebsite website = site.getValue();
            if (website.getErrorCode() != 200) {
                stream.println(site.getKey() + " with " + website.getErrorCode() + " called from " + StringUtil.join(website.getCallers()));
            } else successful++;
        }
        stream.println("And " + successful + " other scans");
    }

    private Predicate<URL> filter;
    private ConcurrentHashMap<String, ScannedWebsite> checked = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, UncheckedWebSite> unchecked = new ConcurrentHashMap<>();

    public WebSiteChecker load() throws IOException, ClassNotFoundException {
        try {
            ObjectInputStream oos = new ObjectInputStream(new FileInputStream("data.txt"));
            checked = (ConcurrentHashMap<String, ScannedWebsite>) oos.readObject();
            unchecked = (ConcurrentHashMap<String, UncheckedWebSite>) oos.readObject();
        } catch (FileNotFoundException ignored) {

        }
        return this;
    }

    private AutoSave autoSave = new AutoSave(this);

    public WebSiteChecker disableAutosave() {
        autoSave.running = false;
        try {
            autoSave.join();
        } catch (InterruptedException ignored) {}
        return this;
    }

    public Predicate<URL> getFilter() {
        return filter;
    }

    public ConcurrentHashMap<String, ScannedWebsite> getChecked() {
        return checked;
    }

    public ConcurrentHashMap<String, UncheckedWebSite> getUnchecked() {
        return unchecked;
    }

    public WebSiteChecker check() {
        autoSave.start();
        while (!unchecked.isEmpty()) {
            Iterator<UncheckedWebSite> iterator = unchecked.values().iterator();
            UncheckedWebSite website = iterator.next();
            Pair<ScannedWebsite, HashMap<String, UncheckedWebSite>> scan = website.scan(this);
            checked.put(website.url, scan.getOne());
            iterator.remove();
            if (scan.getTwo() != null) {
                for (UncheckedWebSite subSite : scan.getTwo().values()) {
                    UncheckedWebSite current = unchecked.computeIfAbsent(subSite.url, k -> subSite);
                    current.addCaller(website.url);
                }
            }
        }
        return this;
    }

}
