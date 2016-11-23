package com.sparkProject


case class Config( inputPath: String = "",
                   bestModelPath: String = "")


object JobArgs {
  val usage = """
    ./bin/spark-submit \
      --class com.sparkProject.Job \
      --master spark://yourMasterHostname:7077 \
      [--num-executors 6] \
      [--executor-memory 4g] \
      [--other-spark-submit-option option] \
      correction-assembly-[version].jar"""

  val parser = new scopt.OptionParser[Config](usage) {
    opt[String]('i', "inputPath") required() action { (value, conf) =>
      conf.copy(inputPath = value)
    } text "Location of the data file/directory (Parquet format required)"

    opt[String]('m', "bestModelPath") required() action { (value, conf) =>
      conf.copy(bestModelPath = value)
    } text "Path used to store the best model found for future use"
  }

}
