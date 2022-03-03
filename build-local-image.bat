echo Have you removed your existing local image yet?
pause
mvn clean install -DskipTests
docker build -t codebase-insights .
docker run -p 8080:8080 codebase-insights