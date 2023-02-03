package tanzu.workshop.paymentcalculator;

import io.pivotal.cfenv.core.CfEnv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@SpringBootApplication
@Controller
public class PaymentCalculatorApplication {
	public static void main(String[] args) {
		SpringApplication.run(PaymentCalculatorApplication.class, args);
	}

	@Bean
	public CfEnv cfEnv() {
		return new CfEnv();
	}

	@Bean(name = "runtimeInstance")
	public String runtimeInstance(CfEnv cfEnv) {
		// try to determine where we are running. If on Cloud Foundry,
		// we can figure out which instance too
		String instance;

		if (cfEnv.isInCf()) {
			instance = "CF Instance: " + cfEnv.getApp().getInstanceIndex();
		} else if (System.getenv("KUBERNETES_SERVICE_HOST") != null) {
			instance = "Kubernetes";
		} else {
			instance = "Docker or Local";
		}

		return instance;
	}

	@RequestMapping("/")
	public RedirectView redirectToSwagger() {
		return new RedirectView("swagger-ui.html");
	}
}
