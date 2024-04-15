#!/bin/bash
wait-for-it db:5432

javac -cp "result/spring-boot.jar" RegistrationApplication.java

mvn package

java -jar target/ChessOnline.jar

gunicorn app.main:app --workers 1 --worker-class uvicorn.workers.UvicornWorker --bind=0.0.0.0:8000