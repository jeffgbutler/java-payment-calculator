package tanzu.workshop.paymentcalculator.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

@Service
public class CrashService {
    private ScheduledExecutorService executer = Executors.newScheduledThreadPool(1);

    // calls System.exit after a 2 second delay
    public void crashIt() {
        executer.schedule(() -> System.exit(22), 2000, TimeUnit.MILLISECONDS);
    }
}
