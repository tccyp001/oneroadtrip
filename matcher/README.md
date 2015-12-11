# One Road Trip

## Installation
* Prerequisites
 
  ```sh
  $ git clone https://github.com/dtrott/maven-protoc-plugin
  $ cd maven-protoc-plugin/ && mvn install && cd ..
  ```
  
* Checkout code
 
  ```sh
  $ git clone https://bitbucket.org/lamuguo/oneroadtrip
  $ cd oneroadtrip
  ```
  
* Test
 
  ```sh
  $ mvn install
  ```
 
* Run
 
  ```sh
  $ mvn exec:java
  ```
 
* Build and deploy image
 
  ```sh
  $ docker build -t xfguo-mvn-test .
  // can add "-d" option to run it as a daemon  
  $ docker run -p 8080:8080 -v $(pwd):/tmp/source xfguo-mvn-test
  ```
 
