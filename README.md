# Java Payment Calculator

This is a Spring Boot microservice that calculates a loan payment. It will run locally, on Cloud Foundry, or
containerized in Docker or Kubernetes.

The default page will show the Swagger UI. Spring Boot actuators are also available at `/actuator`.

In any deployment option, the application can be exercised with the traffic simulator here:
https://jeffgbutler.github.io/payment-calculator-client/ (Kubernetes deployments need an exposed IP address or name)

## Spring Profiles and Redis
The application includes configuration for Redis when the Spring profile "redis" is enabled.

When running with the "redis" profile enabled the application requires, and will connect to, a Redis cache.
When running without the "redis" profile enabled, the application will use an in-memory cache to simulate Redis.

The application uses Spring Boot's auto-configuration for Redis which is based on properties.
Important properties are these:

- `spring.redis.host`
- `spring.redis.port`
- `spring.redis.password`

The documentation to all possible Redis properties is here:
https://github.com/spring-projects/spring-boot/blob/main/spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/data/redis/RedisProperties.java

Property based configuration is different depending on the deployment target (see below).

## Deploying to Cloud Foundry

The application expects to be bound to Redis service instance named "java-calculator-redis" on Cloud Foundry. You can
change this name by modifying [manifest.yml](manifest.yml)

On application startup, the [CFEnv](https://github.com/pivotal-cf/java-cfenv)
library will automatically introspect the Cloud Foundry environment variables and
translate Redis credentials into the proper environment variables for Spring Boot auto-configuration. Magic!

When not deployed on Cloud Foundry, no environment variables will be set automatically.

Deploy the application to Cloud Foundry with the following commands:

```shell
./mvnw clean package

cf push
```

## Deploying to Docker

The application is configured to create an image named "jeffgbutler/payment-calculator". You can change
this by modifying [pom.xml](pom.xml).

1. Build the image with the following:

   ```shell
   ./mvnw clean spring-boot:build-image
   ```

1. Push image to Docker Hub (login to Docker Hub first with `docker login`):

   ```shell
   docker push jeffgbutler/payment-calculator
   ```

1. Create a network in Docker so the payment calculatro can find Redis by DNS name:
   
   ```shell
   docker network create payment-calculator-network 
   ```
   
1. Start Redis in Docker:

   ```shell
   docker run --name redis --detach --network payment-calculator-network redis
   ```

   This will make a Redis instance available in the `payment-calculator-network` at DNS name "redis" and port "6397".
   Note that this Redis instance will not persist any data and has no password - so use this for testing only!

1. Start the Payment Calculator in Docker:

   ```shell
   docker run --detach --publish 8080:8080 \
     --env spring.redis.host=redis \
     --env spring.redis.port=6379 \
     --env spring.profiles.active=redis \
     --network payment-calculator-network \
     jeffgbutler/payment-calculator
   ```

The application will be available at http://localhost:8080

After testing, cleanup Docker by following these steps:

1. Run `docker ps` to get the container IDs for Redis and the payment calculator
1. Run `docker stop <image_id>` for each image ID
1. Run `docker system prune` to remove the stopped images, and the network (you can also use `docker system prune -a` to
   remove all unused images from your local image cache)

## Deploying to Tanzu Cloud Native Runtimes (Knative)

(These directions assume that Tanzu Cloud Native Runtimes is installed and configured on your cluster)

The application is configured to create an image named "jeffgbutler/payment-calculator". You can change
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
      --env spring.profiles.active=redis
   ```

The output of the `kn` command will tell you the URL for the service. You can also retrieve the URL
with the following command:

```shell
kn service describe payment-calculator
```

After testing, cleanup your cluster with the following commands:

```shell
kn service delete payment-calculator

kubectl delete service redis

kubectl delete pod redis
```

## Deploying to Kubernetes Directly

the application is configured to create an image named "jeffgbutler/payment-calculator". You can change
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

   Note: change the values in [paymentCalculatorDeployment.yml](kubernetes/paymentCalculatorDeployment.yml)
   for "spring.redis.host" and "spring.redis.port" as appropriate for your cluster.
   The values in that configuration work with the simple Redis pod and service created above.

   ```shell
   kubectl create -f ./kubernetes/paymentCalculatorDeployment.yml
   ```

1. Create Service

   Note: the service configuration assumes you have a load balancer available in your cluster. If not,
   you can change the service type to "NodePort" in [paymentCalculatorService.yml](kubernetes/paymentCalculatorService.yml)

   ```shell
   kubectl create -f ./kubernetes/paymentCalculatorService.yml
   ```

You can retrieve the IP address of the service with the following command:

```shell
kubectl get service payment-calculator
```

After testing, cleanup your cluster with the following commands:

```shell
kubectl delete -f ./kubernetes/paymentCalculatorService.yml

kubectl delete -f ./kubernetes/paymentCalculatorDeployment.yml

kubectl delete service redis

kubectl delete pod redis
```
