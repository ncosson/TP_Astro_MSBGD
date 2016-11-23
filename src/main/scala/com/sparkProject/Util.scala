package com.sparkProject

import scala.util.{Success, Failure, Try}


object Util {

  def getConfig(args : Array[String]): Try[Config] =
    JobArgs.parser.parse(args, Config()) match {
      case None => Failure(new IllegalArgumentException(JobArgs.parser.usage))
      case Some(conf) => Success(conf)
    }
}
