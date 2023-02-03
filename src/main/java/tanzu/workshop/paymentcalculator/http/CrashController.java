package tanzu.workshop.paymentcalculator.http;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tanzu.workshop.paymentcalculator.service.CrashService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/crash")
public class CrashController {

    private final CrashService crashService;

    public CrashController(CrashService crashService) {
        this.crashService = crashService;
    }

    @Operation(summary = "Warning! The application will crash 2 seconds after this method is called")
    @GetMapping()
    public String crashIt() {
        crashService.crashIt();
        return "OK";
    }
}
