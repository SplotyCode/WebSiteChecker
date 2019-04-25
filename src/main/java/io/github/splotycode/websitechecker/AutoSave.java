package io.github.splotycode.websitechecker;

import io.github.splotycode.mosaik.util.ThreadUtil;
import lombok.RequiredArgsConstructor;

import java.io.*;
import java.util.ConcurrentModificationException;

public class AutoSave extends Thread {

    boolean running = true;
    private WebSiteChecker checker;

    AutoSave(WebSiteChecker checker) {
        this.checker = checker;
    }

    private volatile File dataFile = new File("data.txt");
    private volatile boolean summary = true;
    private volatile int delay = 2000;

    void applySettings(File dataFile, boolean summary, int delay) {
        this.delay = delay;
        this.summary = summary;
        this.dataFile = dataFile;
    }

    @Override
    public void run() {
        while (running) {
            try {
                if (summary) {
                    checker.printResults(new PrintStream(new FileOutputStream("results.txt", false)));
                }
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile));
                oos.writeObject(checker.getChecked());
                oos.writeObject(checker.getUnchecked());
                oos.flush();
                oos.close();
                ThreadUtil.sleep(delay);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ConcurrentModificationException e) {
                ThreadUtil.sleep(200);
            }
        }
    }
}
