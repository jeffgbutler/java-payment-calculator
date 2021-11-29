package tanzu.workshop.paymentcalculator.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/greeting")
public class GreetingController {

    @Value("${greeting}")
    private String greeting;

    @GetMapping
    public String greeting() {
        return greeting;
    }
}
