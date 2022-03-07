#Always run this script from home dir
docker build -t codebase-insights-service .
docker run -p 8080:8080 codebase-insights-service