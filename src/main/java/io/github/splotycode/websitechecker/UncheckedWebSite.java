package io.github.splotycode.websitechecker;

import io.github.splotycode.mosaik.util.Pair;
import lombok.Getter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static io.github.splotycode.websitechecker.WebSiteChecker.toUrl;

@Getter
public class UncheckedWebSite implements Serializable {

    protected Set<String> callers = new HashSet<>();
    protected String url;

    private UncheckedWebSite(String url) {
        this.url = url;
    }

    UncheckedWebSite(String url, String caller) {
        addCaller(caller);
        this.url = url;
    }

    UncheckedWebSite(String url, Set<String> caller) {
        callers.addAll(caller);
        this.url = url;
    }

    private HashMap<String, UncheckedWebSite> getNewSites(InputStream inputStream, WebSiteChecker checker) throws IOException {
        HashMap<String, UncheckedWebSite> nowUnchecked = new HashMap<>();
        Document document = Jsoup.parse(inputStream, "UTF-8", url);
        for (Element element : document.select("link[rel=alternate]")) {
            String sUrl = inspect(element, checker);
            if (sUrl != null) {
                nowUnchecked.put(sUrl, new UncheckedWebSite(sUrl));
            }
        }
        for (Element element : document.select("a[href]")) {
            String sUrl = inspect(element, checker);
            if (sUrl != null) {
                nowUnchecked.put(sUrl, new UncheckedWebSite(sUrl));
            }
        }
        return nowUnchecked;
    }

    Pair<ScannedWebsite, HashMap<String, UncheckedWebSite>> scan(WebSiteChecker checker) {
        HttpURLConnection http = null;
        HashMap<String, UncheckedWebSite> nowUnchecked = null;
        try {
            http = (HttpURLConnection) toUrl(url).openConnection();
            ScannedWebsite scan = new ScannedWebsite(http.getResponseCode(), url, callers);
            if (http.getResponseCode() == 200) {
                if (http.getContentType().contains("html")) {
                    nowUnchecked = getNewSites(http.getInputStream(), checker);
                } else {
                    System.out.println("Skipped " + url + " because it is not html");
                    return new Pair<>(scan, null);
                }
            }
            System.out.println("Checked " + url + " with " + http.getResponseCode());
            return new Pair<>(scan, nowUnchecked);
        } catch (IOException e) {
            System.out.println(url + " encountered and error " + e.getMessage());
            return new Pair<>(new ScannedWebsite(-1, url, callers), nowUnchecked);
        } finally {
            if (http != null) {
                http.disconnect();
            }
        }
    }

    private String inspect(Element element, WebSiteChecker checker) {
        String rawUrl = element.absUrl("href");
        if (rawUrl.equals(url)) return null;
        URL url = toUrl(rawUrl);
        if (checker.getFilter().test(url)) {
            ScannedWebsite website = checker.getChecked().get(rawUrl);
            if (website == null) {
                return rawUrl;
            } else {
                website.addCaller(rawUrl);
            }
        }
        return null;
    }

    void addCaller(String url) {
        callers.add(url);
    }

}
