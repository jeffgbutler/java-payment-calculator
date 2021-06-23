package tanzu.workshop.paymentcalculator.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tanzu.workshop.paymentcalculator.service.HitCounterService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/resetCount")
public class ResetHitCounterController {

    @Autowired
    private HitCounterService hitCounterService;

    @GetMapping
    public void reset() {
        hitCounterService.resetCount();
    }
}
