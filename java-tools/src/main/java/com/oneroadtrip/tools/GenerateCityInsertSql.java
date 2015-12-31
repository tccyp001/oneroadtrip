package com.oneroadtrip.tools;

import java.io.File;
import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Charsets;

public class GenerateCityInsertSql {
  private static final Logger LOG = LogManager.getLogger();

  static class Config {
    @Parameter(names = "--csv_path", description = "Path of csv file", required = false)
    public String csvPath = "";

    @Parameter(names = { "-h", "--help" }, description = "print help message", required = false)
    public boolean help = false;
  }
  
  static String trimString(String origin) {
    return origin.replaceAll("\\p{Cntrl}", "").trim();
  }

  public static void main(String[] args) throws IOException {
    for (String arg : args) {
      LOG.info("arg: {}", arg);
    }
    Config config = new Config();
    JCommander jc = new JCommander(config, args);
    if (config.help) {
      jc.usage();
      return;
    }

    StringBuilder builder = new StringBuilder();
    boolean firstLine = true;
    builder.append("INSERT INTO Cities (city_id, city_name, cn_name, suggest, min) VALUES ");
    File csvData = new File(config.csvPath);
    CSVParser parser = CSVParser.parse(csvData, Charsets.UTF_8, CSVFormat.RFC4180);
    for (CSVRecord csvRecord : parser) {
      try {
        long id = Long.valueOf(csvRecord.get(0));
        String cityName = trimString(csvRecord.get(1));
        String cnName = trimString(csvRecord.get(2));
        int suggestDays = Integer.valueOf(csvRecord.get(3));
        int minDays = Integer.valueOf(csvRecord.get(4));
        String aliases = trimString(csvRecord.get(6));
        LOG.info(
            "xfguo: id = {}, city_name = '{}', cnName = '{}', suggestDays = {}, minDays = {}, aliases = '{}'",
            id, cityName, cnName, suggestDays, minDays, aliases);
        builder.append(firstLine ? "\n" : ",\n");
        builder.append(String.format("  (%d, '%s', '%s', %d, %d)", id, cityName, cnName, suggestDays, minDays));
        if (firstLine) {
          firstLine = false;
        }
      } catch (RuntimeException e) {
        LOG.info("Error in parsing the record: {}", csvRecord, e);
      }
    }
    builder.append(";");
    LOG.info("output sql: \n'{}'", builder.toString());
  }

}
