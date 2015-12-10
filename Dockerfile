FROM maven:3.3.3-jdk-8

EXPOSE 8080
# Serve using jetty
CMD ["mvn", "exec:java"]
