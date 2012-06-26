@ECHO OFF
mvn install:install-file -Dfile=EWSAPI-1.1.5.jar -DgroupId=com.microsoft -DartifactId=ews-java -Dversion=1.1.5 -Dpackaging=jar
