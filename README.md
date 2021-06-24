# Java Payment Calculator

This is a Spring Boot microservice that calculates a loan payment. It will run locally or on Cloud Foundry. When deployed
to Cloud Foundry, it requires a Redis service named `java-calculator-redis`.

The default page will show the Swagger UI. Spring Boot actuators are also available at `/actuator`.

In either case, it can be exercised with the traffic simulator here: https://jeffgbutler.github.io/payment-calculator-client/

## Kubernetes

Build the image with the following:

```shell
./mvnw clean spring-boot:build-image
```

Push image to Docker Hub:

```shell
docker push jeffgbutler/payment-calculator
```

Install with CNR:

```shell
kn service create payment-calculator \
   --image jeffgbutler/payment-calculator --port 8080 --env spring.redis.host=redis --env spring.redis.port=6379 \
   --env spring.profiles.active=cloud
```

Uninstall CNR:

```shell
kn service delete payment-calculator
```

Note: this will install the payment calculator at an HTTP address. The traffic simulator is HTTPS. So we need to 
configure Chrome to allow insecure content for the traffic simulator site.
