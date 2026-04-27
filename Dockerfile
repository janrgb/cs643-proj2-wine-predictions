FROM apache/spark:4.0.2

WORKDIR /app

COPY target/simple-project-1.0.jar /app/prediction-app.jar

COPY pipelineModel /app/pipelineModel

ENTRYPOINT ["spark-submit", "--class", "WineQualityPrediction", "--master", "local[4]", "/app/prediction-app.jar"]
