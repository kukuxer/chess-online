FROM maven:3.8.5-openjdk-21







#  version 2
#RUN apt-get update &&  \
#    apt-get install -y wait-for-it
#
#RUN mkdir /chessonline
#
#WORKDIR /chessonline
#
##ARG JAR_FILE=target/*.jar
#
#COPY ./target/registration-0.0.1-SNAPSHOT.jar app.jar
#
#ENTRYPOINT["java","-jar","/app.jar"]


#version 1
#COPY pom.xml .
#
#RUN mvn install
#
#COPY . .
#
#RUN chmod a+x /chessOnline/docker/*.sh

