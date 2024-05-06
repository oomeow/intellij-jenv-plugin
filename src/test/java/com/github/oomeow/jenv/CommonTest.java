package com.github.oomeow.jenv;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class CommonTest extends TestCase {
    public void testFileListener() {
        // When using WatchService in an IntelliJ IDEA plugin, ensure to start and stop the monitoring service at the appropriate times to avoid resource leaks.
        // Directory to monitor
        String userHome = System.getProperty("user.home");
        String dir = userHome + File.separator + ".jen/versions";
        Path directory = Paths.get(dir);
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            // Register the directory for create, delete, and modify events
            directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            while (true) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    // Handle interrupted exception
                    return;
                }
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    // Process events based on their type
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        System.out.println("File created: " + event.context());
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        System.out.println("File deleted: " + event.context());
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        System.out.println("File modified: " + event.context());
                    }
                }
                key.reset();
            }
        } catch (IOException ignore) {

        }
    }

}
