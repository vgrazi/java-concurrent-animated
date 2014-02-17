1. To build the executable jar:
mvn clean package -DskipTests=true
was mvn assembly:assembly

2. To run Cobertura coverage tests:
mvn site
This will launch the app. Now click around the animation you want to test
Find coverage report at/Users/vgrazi/dev/JavaConcurrentAnimatedMavenSVN/target/surefire-reports