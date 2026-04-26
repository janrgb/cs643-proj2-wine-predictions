/home/ubuntu/spark-4.1.1-bin-hadoop3/bin/spark-submit \
  --class WineQualityPrediction \
  --master local[4] \
target/simple-project-1.0.jar $1
