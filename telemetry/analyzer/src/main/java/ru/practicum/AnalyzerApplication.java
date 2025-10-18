package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.processor.HubEventProcessor;
import ru.practicum.processor.SnapshotProcessor;

@SpringBootApplication
public class AnalyzerApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AnalyzerApplication.class, args);

        final HubEventProcessor hubEventProcessor =
                context.getBean(HubEventProcessor.class);
        final SnapshotProcessor snapshotProcessor =
                context.getBean(SnapshotProcessor.class);

        Thread hubEventsThread = new Thread(hubEventProcessor);
        hubEventsThread.setName("HubEventHandlerThread");
        hubEventsThread.start();

        Thread hubSnapshotsThread = new Thread(snapshotProcessor);
        hubSnapshotsThread.setName("HubSnapshotsHandlerThread");
        hubSnapshotsThread.start();

        //snapshotProcessor.run();
    }
}
