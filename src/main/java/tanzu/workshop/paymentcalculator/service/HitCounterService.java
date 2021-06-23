package tanzu.workshop.paymentcalculator.service;

public interface HitCounterService {
    long incrementCounter();
    void resetCount();
}
