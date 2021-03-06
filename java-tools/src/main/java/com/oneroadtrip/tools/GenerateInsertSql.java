package com.oneroadtrip.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Lists;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/*
 * Command to run the tool:
 *   mvn exec:java -Dexec.mainClass="com.oneroadtrip.tools.GenerateCityInsertSql" \
 *     -Dexec.args="--csv_path /Users/xiaofengguo/Downloads/city-oneroadtrip.csv"
 */
public class GenerateInsertSql {
  private static final Logger LOG = LogManager.getLogger();

  static class Config {
    @Parameter(names = "--city_csv_path", description = "Path of city csv file", required = false)
    public String csvCityPath = "";

    @Parameter(names = "--spot_csv_path", description = "Path of spot csv file", required = false)
    public String csvSpotPath = "";

    @Parameter(names = "--guide_info_csv_path", description = "Path of guide info csv file", required = false)
    public String csvGuideInfoPath = "";

    @Parameter(names = "--output", description = "Path of output file", required = false)
    public String outputPath = "";

    @Parameter(names = { "-h", "--help" }, description = "print help message", required = false)
    public boolean help = false;
  }

  static String trimString(String origin) {
    return origin.replaceAll("\\p{Cntrl}", "").trim();
  }

  public static Long getLongByDigitOnly(String str) {
    str = str.replaceAll("[^0-9]", "");
    return Long.valueOf(str);
  }

  private static String mergeStrings(List<String> strings) {
    StringBuilder builder = new StringBuilder();
    boolean isFirst = true;
    for (String s : strings) {
      if (isFirst) {
        isFirst = false;
      } else {
        builder.append("|");
      }
      builder.append(s);
    }
    return builder.toString();
  }

  private static List<String> splitString(String origin) {
    List<String> result = Lists.newArrayList();
    for (String part : origin.split("[ /|｜]")) {
      String s = trimString(part);
      if (s.isEmpty()) {
        continue;
      }
      result.add(s);
    }
    return result;
  }

  public static class SqlBuilder {
    private final Config config;
    PrintWriter writer;

    Set<String> topicSet = Sets.newTreeSet(Lists.newArrayList("浪漫", "商务", "家庭"));
    Map<String, Long> cityNameToId = Maps.newTreeMap();

    public SqlBuilder(PrintWriter writer, Config config) {
      this.writer = writer;
      this.config = config;
    }

    public void build() throws IOException {
      getCityInfo(config.csvCityPath);
      getGuideInfoSql(cityNameToId, config.csvGuideInfoPath);
      getSpotInfoSql(cityNameToId, config.csvSpotPath);
      generateTopicSql(topicSet);
    }

    private void generateTopicSql(Set<String> topicSet2) {
      StringJoiner joiner = new StringJoiner(",");
      int index = 0;
      for (String topic : topicSet) {
        joiner.add(String.format("\n  (%d, '%s')", ++index, topic));
      }
      String sql = "INSERT INTO Interests (interest_id, interest_name) VALUES " + joiner.toString();
      writer.print(sql + ";\n\n");
      LOG.info("output sql: \n'{}'", sql);
    }

    private void getCityInfo(String csvCityPath) throws IOException {
      StringBuilder builder = new StringBuilder();
      StringJoiner aliasBuilder = new StringJoiner(",");
      boolean firstLine = true;
      builder.append("INSERT INTO Cities (city_id, city_name, cn_name, suggest, min) VALUES ");
      File csvData = new File(csvCityPath);
      CSVParser parser;
      parser = CSVParser.parse(csvData, Charsets.UTF_8, CSVFormat.RFC4180);
      for (CSVRecord csvRecord : parser) {
        try {
          long id = Long.valueOf(csvRecord.get(0));
          String cityName = trimString(csvRecord.get(1));
          String cnName = trimString(csvRecord.get(2));
          int suggestDays = Integer.valueOf(csvRecord.get(3));
          int minDays = Integer.valueOf(csvRecord.get(4));
          List<String> aliases = splitString(csvRecord.get(6));
          LOG.info(
              "xfguo: id = {}, city_name = '{}', cnName = '{}', suggestDays = {}, minDays = {}, aliases = '{}'",
              id, cityName, cnName, suggestDays, minDays, aliases);
          builder.append(firstLine ? "\n" : ",\n");
          builder.append(String.format("  (%d, '%s', '%s', %d, %d)", id, escapeSql(cityName),
              escapeSql(cnName), suggestDays, minDays));
          if (firstLine) {
            firstLine = false;
          }

          cityNameToId.put(cityName.toLowerCase(), id);
          cityNameToId.put(cnName.toLowerCase(), id);
          aliasBuilder.add(String.format("\n  (%d, '%s')", id, escapeSql(cityName)));
          aliasBuilder.add(String.format("\n  (%d, '%s')", id, escapeSql(cnName)));
          for (String alias : aliases) {
            String t = trimString(alias);
            cityNameToId.put(t.toLowerCase(), id);
            aliasBuilder.add(String.format("\n  (%d, '%s')", id, escapeSql(t)));
          }
        } catch (RuntimeException e) {
          LOG.info("Error in parsing the record:", e);
        }
      }
      writer.print(builder.toString() + ";\n\n");
      writer.print("INSERT INTO CityAliases (city_id, alias) VALUES " + aliasBuilder.toString()
          + ";\n\n");
      LOG.info("output sql: \n'{}'", builder.toString());
      LOG.info("city alias: \n{}", "INSERT INTO CityAliases (city_id, alias) VALUES "
          + aliasBuilder.toString());
    }

    private void getSpotInfoSql(Map<String, Long> cityNameToId, String csvSpotPath)
        throws IOException {
      StringJoiner spotJoiner = new StringJoiner(",");
      CSVParser parser = CSVParser.parse(new File(csvSpotPath), Charsets.UTF_8, CSVFormat.RFC4180);
      for (CSVRecord record : parser) {
        try {
          long spotId = Long.valueOf(record.get(0));
          String name = trimString(record.get(1));
          long cityId = cityNameToId.get(record.get(2).toLowerCase());
          String description = trimString(record.get(3));
          float hours = Float.valueOf(record.get(4));
          float score = Float.valueOf(record.get(5));
          List<String> topics = splitString(record.get(6));
          topicSet.addAll(topics);

          spotJoiner.add(String.format("\n  (%d, %d, '%s', '%s', %.2f, %.2f, '%s')", spotId,
              cityId, escapeSql(name), escapeSql(description), hours, score, mergeStrings(topics)));
        } catch (RuntimeException e) {
          LOG.info("Error in parsing the spot record", e);
        }
      }
      String spotSql = "INSERT INTO Spots "
          + "(spot_id, city_id, name, description, hours, score, interests) VALUES "
          + spotJoiner.toString() + ";";
      LOG.info("spotSql:\n{}", spotSql);
      writer.print(spotSql + "\n\n");
    }

    private void getGuideInfoSql(Map<String, Long> cityNameToId, String csvGuideInfoPath)
        throws IOException {
      boolean firstLine = true;
      StringBuilder builder = new StringBuilder();
      StringBuilder userBuilder = new StringBuilder();
      StringBuilder guideCityBuidler = new StringBuilder();
      builder.append("INSERT INTO Guides ("
          + "guide_id, user_id, description, max_persons, has_car, "
          + "score, host_city_id, interests, phone) VALUES ");
      userBuilder.append("INSERT INTO Users (user_id, user_name) VALUES ");
      guideCityBuidler.append("INSERT INTO GuideCities (guide_id, city_id) VALUES ");
      StringJoiner guideCityJoiner = new StringJoiner(",");

      CSVParser parser = CSVParser.parse(new File(csvGuideInfoPath), Charsets.UTF_8,
          CSVFormat.RFC4180);
      for (CSVRecord record : parser) {
        try {
          long id = Long.valueOf(record.get(0));
          long userId = id; // 导游的userId跟guideId保持一致，其它用户从100000开始给。
          String name = trimString(record.get(1));
          String hostCity = record.get(2);
          long hostCityId = cityNameToId.get(hostCity.toLowerCase());
          List<String> topics = splitString(record.get(3));
          float score = Float.valueOf(record.get(4));
          int numPeople = Integer.valueOf(record.get(5));
          String hasCar = (trimString(record.get(6)).toLowerCase().equals("y") ? "True" : "False");
          Long phone = getLongByDigitOnly(record.get(7));
          List<String> cities = splitString(record.get(8));
          topicSet.addAll(topics);

          if (firstLine) {
            firstLine = false;
          } else {
            builder.append(",");
            userBuilder.append(",");
          }
          // TODO(xfguo): 我们现在没有description，暂时用hostCity代替。
          builder.append(String.format("\n  (%d, %d, '%s', %d, %s, %.2f, %d, '%s', %d)", id, userId,
              escapeSql(hostCity), numPeople, escapeSql(hasCar), score, hostCityId, mergeStrings(topics),
              phone));
          userBuilder.append(String.format("\n  (%d, '%s')", userId, escapeSql(name)));

          for (String city : cities) {
            Long cityId = cityNameToId.get(city.toLowerCase());
            if (cityId != null) {
              guideCityJoiner.add(String.format("\n (%d, %d)", id, cityId));
            }
          }
        } catch (RuntimeException e) {
          LOG.info("Error in parsing the guide record ", e);
        }
      }

      guideCityBuidler.append(guideCityJoiner.toString());
      writer.print(userBuilder.toString() + ";\n\n");
      writer.print(builder.toString() + ";\n\n");
      writer.print(guideCityBuidler.toString() + ";\n\n");
      LOG.info("user sql:\n{}", userBuilder.toString());
      LOG.info("guide sql:\n{}", builder.toString());
      LOG.info("guide city sql:\n{}", guideCityBuidler.toString());
    }
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

    try (PrintWriter writer = new PrintWriter(new FileWriter(new File(config.outputPath)))) {
      new SqlBuilder(writer, config).build();
    }
  }

  private static String escapeSql(String name) {
    return name.replace("'", "\"");
  }
}
