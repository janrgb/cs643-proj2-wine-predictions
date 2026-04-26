/* WineQualityTraining.java */
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

public class WineQualityTraining {
	public static void main(String[] args) throws java.io.IOException {
		SparkSession spark = SparkSession.builder().appName("Simple Application").getOrCreate();
		spark.sparkContext().setLogLevel("ERROR");

		String trainingCSVPath = "/home/ubuntu/TrainingDataset.csv";

		/* We need to read our training CSV data. */
		Dataset<Row> wineTrainingDF = spark.read().option("delimiter", ";").option("header", "true").option("inferSchema", "true").csv(trainingCSVPath);

		/* We need to strip out the quotes in the column headers. */
		wineTrainingDF = wineTrainingDF.toDF("fixed acidity", "volatile acidity", "citric acid", "residual sugar", "chlorides", "free sulfur dioxide", "total sulfur dioxide", "density", "pH", "sulphates", "alcohol", "quality");

		String[] features = new String[]{ "fixed acidity", "volatile acidity", "citric acid", "residual sugar", "chlorides", "free sulfur dioxide", "total sulfur dioxide", "density", "pH", "sulphates", "alcohol" };

		/* 0-Index the 'quality' label -- our prediction target. */
		StringIndexer labelIndexer = new StringIndexer()
			.setInputCol("quality")
			.setOutputCol("label")
			.setHandleInvalid("keep");
		
		/* We need to assemble a vector with the appropriate input and output cols. */
		VectorAssembler assembler = new VectorAssembler()
			.setInputCols(features)
			.setOutputCol("rawFeatures");

		/* Standardize the data. */
		StandardScaler scaler = new StandardScaler()
			.setInputCol("rawFeatures")
			.setOutputCol("features")
			.setWithStd(true)
			.setWithMean(true);

		/* Train a Logistic Regression model. */
		LogisticRegression lr = new LogisticRegression()
			.setRegParam(0.0001)
			.setElasticNetParam(0.0)
			.setMaxIter(500);

		/* Pipeline it. */
		Pipeline pipeline = new Pipeline()
			.setStages(new PipelineStage[] {labelIndexer, assembler, scaler, lr});

		/* Fit on the TRAINING SET. */
		PipelineModel pipelineModel = pipeline.fit(wineTrainingDF);

		/* Save models to home directory for copying later. */
		pipelineModel.write().overwrite().save("file:///home/ubuntu/pipelineModel");
	}
}
