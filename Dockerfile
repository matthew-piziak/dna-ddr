FROM openjdk:8-alpine

COPY target/uberjar/ginkgo-lum.jar /ginkgo-lum/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/ginkgo-lum/app.jar"]
