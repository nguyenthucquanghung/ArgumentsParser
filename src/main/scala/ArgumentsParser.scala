import scala.collection.mutable

object ArgumentsParser {
  val usage = """
    You must input all parameters in usage to run this application
    Usage: vep-spark  [--input_file hdfs_absolute_dir]
                      [--output_file hdfs_absolute_dir]
                      [--vep_dir absolute_dir]
                      [--cache_dir absolute_dir]

    Optional arguments:
      All ensembl-vep arguments except "--cache", "--cache_dir", "--input_file", "--output_file"
      NOTE: If you use any ensembl-vep's plugin, please add [--dir_plugins absolute_dir] option
  """
  def main(args: Array[String]): Unit = {
    if (args.length == 0) println(usage)
    val options: mutable.MutableList[Option] = new mutable.MutableList[Option]
    val arglist = args.toList
//    val arglist = "--input_file" :: "hoho" :: "--output_file" :: "hihi" :: "--vep_dir" :: "hihi" :: "--cache_dir" :: "hihi" :: "--cache_dir" :: "--cache_dir1" :: Nil
    type OptionMap = Map[String, Any]
    @scala.annotation.tailrec
    def nextOption(map : OptionMap, list: List[String]) : OptionMap = {
      def isValue(s : String) = (s(0) != '-')
      list match {
        case Nil => map
        case "--input_file" :: value :: tail =>
          nextOption(map ++ Map("input_file" -> value), tail)
        case "--output_file" :: value :: tail =>
          nextOption(map ++ Map("output_file" -> value), tail)
        case "--vep_dir" :: value :: tail =>
          nextOption(map ++ Map("vep_dir" -> value), tail)
        case "--cache_dir" :: value :: tail =>
          nextOption(map ++ Map("cache_dir" -> value), tail)
        case currentParam :: nextParam :: tail if isValue(nextParam) =>
          options += new Option(currentParam, nextParam, true)
          nextOption(map, tail)
        case currentParam :: nextParam :: tail if !isValue(nextParam) =>
          options += new Option(currentParam, null, false)
          nextOption(map, list.tail)
        case param :: Nil =>
          options += new Option(param, null, false)
          nextOption(map, list.tail)
        case option :: tail => println("Unknown option " + option)
          sys.exit(1)
      }
    }
    val defaultOptions = nextOption(Map(),arglist)
    if (
      !defaultOptions.contains("input_file") ||
      !defaultOptions.contains("output_file") ||
      !defaultOptions.contains("vep_dir") ||
      !defaultOptions.contains("cache_dir")
    ) {
      print(usage)
    } else {
      val builder = new CommandBuilder(Tool.VEP, defaultOptions.get("vep_dir").mkString, options)
      val cmd = builder.addOption("--cache", null, hasParameter = false)
        .addOption("--dir_cache", defaultOptions.get("cache_dir").mkString, hasParameter = true)
        .addOption("-o", "STDOUT", hasParameter = true)
        .build()
        .generate
      println(cmd)
    }
  }
}
