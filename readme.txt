Note: This is undergoing a total rewrite here : https://github.com/vgrazi/JavaConcurrentAnimatedReboot
New features: CompletableFuture
Improved code cleanliness

1. To build the executable jar:
mvn clean package -DskipTests=true

2. To run coverage report, right click ConcurrentExampleLauncher/run with coverage.
Then choose "generate coverage report" icon from Coverage summary (left/bottom)


was
1. mvn assembly:assembly
2. To run Cobertura coverage tests:
mvn site
This will launch the app. Now click around the animation you want to test
Find coverage report at/Users/vgrazi/dev/JavaConcurrentAnimatedMavenSVN/target/surefire-reports
