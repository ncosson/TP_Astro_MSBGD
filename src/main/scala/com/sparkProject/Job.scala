package com.sparkProject

import scala.util.{Success, Failure}

import org.apache.spark.sql.SparkSession
import org.apache.spark.ml.feature.{VectorAssembler, StringIndexer}
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.tuning.{TrainValidationSplit, ParamGridBuilder}
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.Pipeline

import com.sparkProject.Util._

object Job {

  def main(args: Array[String]): Unit = {

    // SparkSession configuration
    val spark = SparkSession
      .builder
      .appName("TP_correction")
      .getOrCreate()

    val sc = spark.sparkContext

    sc.setLogLevel("ERROR")

    import spark.implicits._

    val tryConf = getConfig(args)

    tryConf match {
      case Failure(exception) => throw exception

      case Success(conf) =>

        // Read the data
        val data = spark
          .read
          .parquet(conf.inputPath)

        // Columns used as features
        val features:Array[String] = data
          .columns
          .filterNot(elem => elem == "koi_disposition" || elem == "rowid")

        // Tranforming the labels (Strings) into numbers
        val indexer = new StringIndexer()
          .setInputCol("koi_disposition")
          .setOutputCol("label")
          .fit(data)

        val data_ready = indexer.transform(data)


        // Split between training and test sets
        val Array(training, test) = data_ready.randomSplit(Array(0.9, 0.1))

        // Parameters used in the grid search
        val regGrid: Array[Double] = (-6.0 to 0.0 by 0.5).toArray
          .map(p => math.pow(10.0, p))


        // Construction of the Pipeline
        val assembler = new VectorAssembler()
          .setInputCols(features)
          .setOutputCol("features")

        val lr = new LogisticRegression()
          .setElasticNetParam(1.0) // L1-norm regularization : LASSO -> automatic feature selection
          .setFeaturesCol("features")
          .setLabelCol("label")
          .setStandardization(true) // Used to normalize the data
          .setFitIntercept(true)
          .setPredictionCol("prediction")
          .setTol(1.0e-5)
          .setMaxIter(300)


        // Usually the transformations applied to the labels are not put in the Pipeline
        // because when the resulting best model will be used on new data, the label column
        // will not be present. So we do not put indexer in the pipeline.
        val pipeline = new Pipeline()
          .setStages(Array(assembler, lr))

        // Create the grid search
        val grid = new ParamGridBuilder()
          .addGrid(lr.regParam, regGrid)
          .build()

        // Define the metric used to evaluate the model for each points on the grid
        val eval = new BinaryClassificationEvaluator()
          .setLabelCol("label")
          .setRawPredictionCol("prediction")
          .setMetricName("areaUnderROC")


        // Set the grid search procedure :
        val trainValidationSplit = new TrainValidationSplit()
          .setEstimator(pipeline)
          .setEvaluator(eval)
          .setEstimatorParamMaps(grid)
          .setTrainRatio(0.7)

        // Run the gridsearch, and choose the best set of parameters (here the best regularisation).
        // The dataset will be used multiple times during the grid search and each training, so we persist it.
        training.persist
        val model = trainValidationSplit.fit(training)


        // Make predictions on test data. model is the model with combination of parameters
        // that performed best :
        val predict = model.transform(test)

        predict.groupBy("label", "prediction").count.show

        // Evaluate the model on data it has never seen during its training :
        val testScore = eval.evaluate(predict)

        println(s"model's areaUnderROC on the test-set: $testScore")

        // Save the trained model for future use
        model.write.overwrite.save(conf.bestModelPath)

        println(s"Best model saved here: ${conf.bestModelPath}")

        spark.stop()
    }
  }
}

