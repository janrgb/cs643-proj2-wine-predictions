/* WineQualityPrediction.java */
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.*;
import static org.apache.spark.sql.functions.*;

import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.classification.LogisticRegressionTrainingSummary;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.feature.StandardScaler;
import org.apache.spark.ml.feature.StandardScalerModel;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.StringIndexerModel;
import org.apache.spark.ml.feature.IndexToString;

public class WineQualityPrediction {
	public static void main(String[] args) {

		if (args.length < 1) {
			System.err.println("ERROR: Please provide the path to the testing CSV file.");
			System.err.println("Local Usage: ./run_predictor <csv_path>\nDocker Usage: docker run wine-app <csv_path>");
			System.exit(1);
		}
		
		String testingCSVPath = args[0];

		SparkSession spark = SparkSession.builder()
			.appName("Simple Application")
			.getOrCreate();

		spark.sparkContext().setLogLevel("ERROR");

		/* We need to read our testing CSV data. */
		Dataset<Row> wineTestingDF = spark.read().option("delimiter", ";").option("header", "true").option("inferSchema", "true").csv(testingCSVPath);

		wineTestingDF = wineTestingDF.toDF("fixed acidity", "volatile acidity", "citric acid", "residual sugar", "chlorides", "free sulfur dioxide", "total sulfur dioxide", "density", "pH", "sulphates", "alcohol", "quality");

		/* Loading Models. */
		PipelineModel pipelineModel = PipelineModel.load("pipelineModel");

		/* Predict on the VALIDATION SET. */
		Dataset<Row> predictions = pipelineModel.transform(wineTestingDF);

		MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
			.setMetricName("f1");

		double F1 = evaluator.evaluate(predictions);
		System.out.println("F1 Score: " + F1);

	}
}
