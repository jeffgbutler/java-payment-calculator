package tanzu.workshop.paymentcalculator.http;

import java.math.BigDecimal;

import io.opentelemetry.api.trace.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tanzu.workshop.paymentcalculator.model.CalculatedPayment;
import tanzu.workshop.paymentcalculator.service.HitCounterService;
import tanzu.workshop.paymentcalculator.service.PaymentService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final String runtimeInstance;
    private final HitCounterService hitCounterService;
    private final PaymentService paymentService;

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    public PaymentController(HitCounterService hitCounterService, PaymentService paymentService,
                             @Qualifier("runtimeInstance") String runtimeInstance) {
        this.hitCounterService = hitCounterService;
        this.paymentService = paymentService;
        this.runtimeInstance = runtimeInstance;
    }

    @GetMapping()
    public CalculatedPayment calculatePayment(@RequestParam("amount") double amount, @RequestParam("rate") double rate,
            @RequestParam("years") int years, @RequestParam(value = "chaotic", required = false) boolean chaotic) {

        if (chaotic) {
            chaos();
        }

        BigDecimal payment = paymentService.calculate(amount, rate, years);

        logger.debug("Calculated payment of {} for input amount: {}, rate: {}, years: {}",
            payment, amount, rate, years);

        long count = hitCounterService.incrementCounter();

        // Enrich the auto-instrumented HTTP span with loan business context
        Span.current()
            .setAttribute("loan.amount", amount)
            .setAttribute("loan.rate", rate)
            .setAttribute("loan.years", years)
            .setAttribute("loan.payment", payment.doubleValue())
            .setAttribute("app.instance", runtimeInstance)
            .setAttribute("app.hit_count", count)
            .setAttribute("app.is_chaotic", chaotic);

        CalculatedPayment calculatedPayment = new CalculatedPayment();
        calculatedPayment.setAmount(amount);
        calculatedPayment.setRate(rate);
        calculatedPayment.setYears(years);
        calculatedPayment.setPayment(payment);
        calculatedPayment.setInstance(runtimeInstance);
        calculatedPayment.setCount(count);

        return calculatedPayment;
    }

    private void chaos() {
        double chaosFactor = Math.random();

        if ("Kubernetes".equals(runtimeInstance) && chaosFactor > 0.9) {
            // on Kubernetes, crash the app about 10% of the time. We only do this on Kubernetes
            // because we'll configure the deployment to restart the app
            logger.debug("Chaotic Crash!");
            System.exit(-1);
            return;
        }

        if (chaosFactor < 0.5) {
            logger.debug("Chaotic Delay!");
            // about half the requests should have some random delay
            try {
                Thread.sleep((long) (1000 * chaosFactor));
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted!", e);
            }
        }
    }
}
