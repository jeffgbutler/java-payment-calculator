package tanzu.workshop.paymentcalculator.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import tanzu.workshop.paymentcalculator.service.CrashService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/crash")
public class CrashController {

    @Autowired
    private CrashService crashService;

    @ApiOperation("Warning! The application will crash 2 seconds after this method is called")
    @GetMapping()
    public String crashIt() {
        crashService.crashIt();
        return "OK";
    }
}
