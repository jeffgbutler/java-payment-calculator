package tanzu.workshop.paymentcalculator.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public BigDecimal calculate(double amount, double rate, int years) {
        Tracer tracer = GlobalOpenTelemetry.getTracer("payment-calculator");
        Span span = tracer.spanBuilder("payment.calculate").startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("loan.amount", amount);
            span.setAttribute("loan.rate", rate);
            span.setAttribute("loan.years", years);

            BigDecimal payment;
            if (rate == 0.0) {
                span.setAttribute("loan.type", "zero-interest");
                payment = calculateWithoutInterest(amount, years);
            } else {
                span.setAttribute("loan.type", "interest");
                payment = calculateWithInterest(amount, rate, years);
            }

            span.setAttribute("loan.payment", payment.doubleValue());
            return payment;
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    private BigDecimal calculateWithInterest(double amount, double rate, int years) {
        double monthlyRate = rate / 100.0 / 12.0;
        int numberOfPayments = years * 12;
        double payment = (monthlyRate * amount) / (1.0 - Math.pow(1.0 + monthlyRate, -numberOfPayments));
        return toMoney(payment);
    }

    private BigDecimal calculateWithoutInterest(double amount, int years) {
        int numberOfPayments = years * 12;
        return toMoney(amount / numberOfPayments);
    }

    private BigDecimal toMoney(double d) {
        BigDecimal bd = new BigDecimal(d);
        return bd.setScale(2, RoundingMode.HALF_UP);
    }
}
