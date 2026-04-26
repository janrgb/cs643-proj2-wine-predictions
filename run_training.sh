/home/ubuntu/spark-4.1.1-bin-hadoop3/bin/spark-submit \
  --class WineQualityTraining \
  --master spark://YOUR_SPARK_URL:7077 \
  --executor-memory 512M \
  --total-executor-cores 8 \
target/simple-project-1.0.jar
