# Java Payment Calculator

This is a Spring Boot microservice that calculates a loan payment. It will run locally, on Cloud Foundry, or Kubernetes.

The default page will show the Swagger UI. Spring Boot actuators are also available at `/actuator`.

In any deployment option, the application can be exercised with the traffic simulator here:
https://jeffgbutler.github.io/payment-calculator-client/ (Kubernetes deployments need an exposed IP address or name)

## Spring Profiles and Redis
If the "cloud" profile is enabled the application requires, and will connect to, a Redis cache.
When running without the "cloud" profile enabled, the application will use an in-memory cache to simulate Redis.

When the "cloud" profile is enabled, the application will use Spring Boot's auto configuration properties to connect
to Redis. Important properties are these:

- `spring.redis.host`
- `spring.redis.port`
- `spring.redis.password`

All possible Redis properties are documented here:
https://github.com/spring-projects/spring-boot/blob/main/spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/data/redis/RedisProperties.java

The properties are configured differently depending on the deployment target (see below).

## Cloud Foundry

The application expects to be bound to Redis service instance named "java-calculator-redis" on Cloud Foundry. You can
change this name by modifying [manifest.yml](manifest.yml)

On application startup, the [CFEnv](https://github.com/pivotal-cf/java-cfenv)
library will automatically introspect the Cloud Foundry environment variables and
translate Redis credentials into the proper environment variables for Spring Boot auto-configuration. Magic!

If the application is not deployed to Cloud Foundry, no environment variables will be set automatically.

## Cloud Native Runtimes (Knative)

(These directions assume that Tanzu Cloud Native Runtimes is installed and configured on your cluster)

For Kubernetes, the application is configured to create an image named "jeffgbutler/payment-calculator". You can change
this by modifying [pom.xml](pom.xml).

1. Create a Redis instance on your Kubernetes cluster. One simple way to do this is to deploy a single pod with Redis
   and expose it with a ClusterIP service:
   
   ```shell
   kubectl run redis --image redis
   
   kubectl expose pod redis --type=ClusterIP --port=6379 --target-port=6379 
   ```
   
   This will make a Redis instance available in the cluster at DNS name "redis" and port "6397". Note that
   this Redis instance will not persist any data and has no password - so use this for testing only!

1. Build the image with the following:

   ```shell
   ./mvnw clean spring-boot:build-image
   ```

1. Push image to Docker Hub (login to Docker Hub first with `docker login`):

   ```shell
   docker push jeffgbutler/payment-calculator
   ```

1. Install with Cloud Native Runtimes (Knative):

   Note: change the values for "spring.redis.host" and "spring.redis.port" as appropriate for your cluster.
   The values below work with the simple Redis pod and service created above.

   ```shell
   kn service create payment-calculator \
      --image jeffgbutler/payment-calculator --port 8080 \
      --env spring.redis.host=redis \
      --env spring.redis.port=6379 \
      --env spring.profiles.active=cloud
   ```

Once the service is created, the output of the `kn` command will tell you the URL. You can also retrieve the URL
with the following command:

```shell
kn service describe payment-calculator
```

After testing, cleanup you cluster with the following commands:

```shell
kn service delete payment-calculator

kubectl delete service redis

kubectl delete pod redis
```

## Kubernetes Native

For Kubernetes, the application is configured to create an image named "jeffgbutler/payment-calculator". You can change
this by modifying [pom.xml](pom.xml).

1. Create a Redis instance on your Kubernetes cluster. One simple way to do this is to deploy a single pod with Redis
   and expose it with a ClusterIP service:

   ```shell
   kubectl run redis --image redis
   
   kubectl expose pod redis --type=ClusterIP --port=6379 --target-port=6379 
   ```

   This will make a Redis instance available in the cluster at DNS name "redis" and port "6397". Note that
   this Redis instance will not persist any data and has no password - so use this for testing only!

1. Build the image with the following:

   ```shell
   ./mvnw clean spring-boot:build-image
   ```

1. Push image to Docker Hub (login to Docker Hub first with `docker login`):

   ```shell
   docker push jeffgbutler/payment-calculator
   ```

1. Create Deployment:

   Note: change the values in [paymentCalculatorDeployment.yml](./kubernetes/paymentCalculatorDeployment.yml)
   for "spring.redis.host" and "spring.redis.port" as appropriate for your cluster.
   The values in that configuration work with the simple Redis pod and service created above.

   ```shell
   kubectl create -f ./kubernetes/paymentCalculatorDeployment.yml
   ```

1. Create Service

   Note: the service configuration assumes you have a load balancer available in your cluster. If not,
   you can change the service type to "NodePort" in [paymentCalculatorService.yml](./kubernetes/paymentCalculatorService.yml)

   ```shell
   kubectl create -f ./kubernetes/paymentCalculatorService.yml
   ```

Once the service is created, you can retrieve the IP address with the following command:

```shell
kubectl get service payment-calculator
```

After testing, cleanup you cluster with the following commands:

```shell
kubectl delete -f ./kubernetes/paymentCalculatorService.yml

kubectl delete -f ./kubernetes/paymentCalculatorDeployment.yml

kubectl delete service redis

kubectl delete pod redis
```
