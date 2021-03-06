package io.mediachain.transactor

import io.mediachain.util.Properties
import io.mediachain.copycat.{S3BackingStore, S3Restore}

object S3RestoreClient {
  def main(args: Array[String]) {
    if (args.length < 3) {
      println("Expected arguments: config s3key cluster-ip ...")
      System.exit(1)
    }
    
    val (conf :: s3key :: cluster) = args.toList
    val props = Properties.load(conf)
    val config = S3BackingStore.Config.fromProperties(props)
    val s3r = new S3Restore(config)
    s3r.restore(cluster, s3key)
  }
}
