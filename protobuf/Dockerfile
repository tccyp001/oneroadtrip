# docker build -t lamuguo/protoc .

FROM maven:3.3.3-jdk-8

WORKDIR /code

RUN apt-get update && \
    apt-get install -y git build-essential autoconf libtool

# Install protoc
RUN git clone https://github.com/google/protobuf && \
     cd protobuf && \
    ./autogen.sh && ./configure && make && make check && make install && \
    export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH && \
    export PATH=/usr/local/bin:$PATH && \
    cd ..
