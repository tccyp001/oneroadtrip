- Prerequisites
  $ git clone https://github.com/dtrott/maven-protoc-plugin
  $ cd maven-protoc-plugin/ && mvn install && cd ..
- Checkout code
  $ git clone https://bitbucket.org/lamuguo/oneroadtrip
  $ cd oneroadtrip
- Test
  $ mvn install
- Run
  $ mvn exec:java
- Build and deploy image
  $ docker build -t xfguo-mvn-test .
  $ docker run -p 8080:8080 -v $(pwd):/tmp/source xfguo-mvn-test  // can add "-d" option to run it as a daemon  
