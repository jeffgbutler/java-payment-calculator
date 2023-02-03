package tanzu.workshop.paymentcalculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.HashMap;
import java.util.Map;

/**
 * CfEnv publishes redis connection properties for Springboot2. The property names
 * were changed in Springboot3. We can remove this class (and the spring.factories file in META-INF)
 * when CfEnv supports Springboot3.
 */
public class Springboot3Hack implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources sources = environment.getPropertySources();

        Map<String, Object> props = new HashMap<>();
        String propertyValue;

        if ((propertyValue = environment.getProperty("spring.redis.host")) != null) {
            props.put("spring.data.redis.host", propertyValue);
        }

        if ((propertyValue = environment.getProperty("spring.redis.password")) != null) {
            props.put("spring.data.redis.password", propertyValue);
        }

        if ((propertyValue = environment.getProperty("spring.redis.port")) != null) {
            props.put("spring.data.redis.port", propertyValue);
        }

        if ((propertyValue = environment.getProperty("spring.redis.ssl")) != null) {
            props.put("spring.data.redis.ssl", propertyValue);
        }

        sources.addLast(new MapPropertySource("Springboot3Hack", props));
    }
}
