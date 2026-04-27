FROM apache/spark:4.0.2-java21
USER root

WORKDIR /app

COPY target/simple-project-1.0.jar /app/prediction-app.jar

COPY pipelineModel /app/pipelineModel

ENTRYPOINT ["/opt/spark/bin/spark-submit", "--class", "WineQualityPrediction", "--master", "local[4]", "/app/prediction-app.jar"]
