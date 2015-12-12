# docker build -t lamuguo/oneroadtrip .
# docker run -p 8080:8080 lamuguo/oneroadtrip

FROM lamuguo/protoc

WORKDIR /code

ENV PATH /usr/local/bin:$PATH
ENV LD_LIBRARY_PATH /usr/local/lib:$LD_LIBRARY_PATH

ADD pom.xml /code/pom.xml
ADD src /code/src

RUN git clone https://github.com/dtrott/maven-protoc-plugin && \
    cd maven-protoc-plugin && mvn install && cd .. && \
    mvn package

EXPOSE 8080
CMD ["/usr/lib/jvm/java-8-openjdk-amd64/bin/java", "-jar", \
     "target/oneroadtrip-jar-with-dependencies.jar"]
