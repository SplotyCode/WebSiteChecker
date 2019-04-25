package io.github.splotycode.websitechecker;

import io.github.splotycode.mosaik.util.ThreadUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ConcurrentModificationException;

public class AutoSave extends Thread {

    boolean running = true;
    private WebSiteChecker checker;

    public AutoSave(WebSiteChecker checker) {
        this.checker = checker;
    }

    @Override
    public void run() {
        while (running) {
            try {
                checker.printResults(new PrintStream(new FileOutputStream("results.txt", false)));
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("data.txt"));
                oos.writeObject(checker.getChecked());
                oos.writeObject(checker.getUnchecked());
                oos.flush();
                oos.close();
                ThreadUtil.sleep(2000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ConcurrentModificationException e) {
                ThreadUtil.sleep(200);
            }
        }
    }
}
