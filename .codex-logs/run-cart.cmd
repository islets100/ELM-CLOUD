@echo off
set "JAVA_HOME=D:\Java\JDK\jdk1.8.0_271"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d E:\CourseData\software_advanced_practice\ELM-CLOUD
java -jar E:\CourseData\software_advanced_practice\ELM-CLOUD\elm-cart-server\target\elm-cart-server-0.0.1-SNAPSHOT.jar >> E:\CourseData\software_advanced_practice\ELM-CLOUD\.codex-logs\cart.out.log 2>&1
