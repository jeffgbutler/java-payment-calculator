# Java Payment Calculator

This is a Spring Boot microservice that calculates a loan payment. It will run locally or on Cloud Foundry. When deployed
to Cloud Foundry, it requires a Redis service named `java-calculator-redis`.

The default page will show the Swagger UI. Spring Boot actuators are also available at `/actuator`.

In either case, it can be exercised with the traffic simulator here: https://jeffgbutler.github.io/payment-calculator-client/
