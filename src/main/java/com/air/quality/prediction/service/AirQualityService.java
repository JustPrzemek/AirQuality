package com.air.quality.prediction.service;

import com.air.quality.prediction.dto.DailyStatsDTO;
import com.air.quality.prediction.model.AirQualityData;
import com.air.quality.prediction.repository.AirQualityRepository;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class AirQualityService {

    private static final Logger logger = LoggerFactory.getLogger(AirQualityService.class);

    @Value("${csv.monitor.directory}")
    private String csvDirectory;

    private final AirQualityRepository airQualityRepository;

    @Autowired
    public AirQualityService(AirQualityRepository airQualityRepository) {
        this.airQualityRepository = airQualityRepository;
    }

    @Scheduled(fixedDelay = 60000) // Check every minute
    public void checkForNewFiles() {
        logger.info("Checking for new CSV files in directory: {}", csvDirectory);

        try {
            File dir = new File(csvDirectory);
            if (!dir.exists()) {
                dir.mkdirs();
                logger.info("Created directory: {}", csvDirectory);
                return;
            }

            File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".csv"));
            if (files != null) {
                for (File file : files) {
                    processCsvFile(file);
                }
            }
        } catch (Exception e) {
            logger.error("Error checking for new files", e);
        }
    }

    @Transactional
    public void processCsvFile(File file) {
        String filename = file.getName();

        // Check if file has already been processed
        if (airQualityRepository.existsBySourceFile(filename)) {
            logger.info("File {} has already been processed, skipping", filename);
            return;
        }

        logger.info("Processing new CSV file: {}", filename);

        try (FileReader fileReader = new FileReader(file)) {
            CSVParser parser = new CSVParserBuilder().withSeparator(',').build();
            CSVReader csvReader = new CSVReaderBuilder(fileReader)
                    .withCSVParser(parser)
                    .withSkipLines(1) // Skip header row
                    .build();

            List<String[]> rows = csvReader.readAll();
            List<AirQualityData> dataList = new ArrayList<>();

            // Date and time formatters
            List<DateTimeFormatter> dateFormatters = Arrays.asList(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                    DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                    DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                    DateTimeFormatter.ofPattern("yyMMdd")
            );

            List<DateTimeFormatter> timeFormatters = Arrays.asList(
                    DateTimeFormatter.ofPattern("HH:mm:ss"),
                    DateTimeFormatter.ofPattern("HH:mm:ss.SSS"),
                    DateTimeFormatter.ofPattern("HH:mm"),
                    DateTimeFormatter.ofPattern("HHmmss")
            );

            for (String[] row : rows) {
                if (row.length < 28) { // Check if row has all expected columns
                    logger.warn("Row has insufficient columns ({}): {}", row.length, Arrays.toString(row));
                    continue;
                }

                AirQualityData data = new AirQualityData();
                data.setSourceFile(filename);

                // Parse date and time
                LocalDate date = null;
                LocalTime time = null;

                // Try to parse date
                for (DateTimeFormatter formatter : dateFormatters) {
                    try {
                        date = LocalDate.parse(row[0].trim(), formatter);
                        break;
                    } catch (DateTimeParseException e) {
                        // Continue to next formatter
                    }
                }

                // Try to parse time
                for (DateTimeFormatter formatter : timeFormatters) {
                    try {
                        time = LocalTime.parse(row[1].trim(), formatter);
                        break;
                    } catch (DateTimeParseException e) {
                        // Continue to next formatter
                    }
                }

                if (date == null || time == null) {
                    logger.warn("Could not parse date/time: {} / {}", row[0], row[1]);
                    continue;
                }

                data.setDate(date);
                data.setTime(time);

                // Parse numerical values
                try {
                    data.setPm25(parseDoubleOrNull(row[2]));
                    data.setPm10(parseDoubleOrNull(row[3]));
                    data.setIaq(parseDoubleOrNull(row[4]));
                    data.setHcho(parseDoubleOrNull(row[5]));
                    data.setCo2(parseDoubleOrNull(row[6]));
                    data.setP(parseDoubleOrNull(row[7]));
                    data.setTin(parseDoubleOrNull(row[8]));
                    data.setTout(parseDoubleOrNull(row[9]));
                    data.setRhin(parseDoubleOrNull(row[10]));
                    data.setRhout(parseDoubleOrNull(row[11]));
                    data.setLat(parseDoubleOrNull(row[12]));
                    data.setLon(parseDoubleOrNull(row[13]));
                    data.setHdg(parseDoubleOrNull(row[14]));
                    data.setAmsl(parseDoubleOrNull(row[15]));
                    data.setAgl(parseDoubleOrNull(row[16]));
                    data.setMil(parseDoubleOrNull(row[17]));
                    data.setNo2(parseDoubleOrNull(row[18]));
                    data.setNo(parseDoubleOrNull(row[19]));
                    data.setSo2(parseDoubleOrNull(row[20]));
                    data.setH2s(parseDoubleOrNull(row[21]));
                    data.setCo(parseDoubleOrNull(row[22]));
                    data.setHcn(parseDoubleOrNull(row[23]));
                    data.setHcl(parseDoubleOrNull(row[24]));
                    data.setNh3(parseDoubleOrNull(row[25]));
                    data.setEc(parseDoubleOrNull(row[26]));

                    // Handle MRK field
                    if (row.length > 27) {
                        data.setMrk(row[27].trim());
                    } else {
                        data.setMrk("");
                    }

                    dataList.add(data);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    logger.warn("Error parsing numeric values in row: {} - {}", Arrays.toString(row), e.getMessage());
                    continue;
                }
            }

            // Save all data in batches
            if (!dataList.isEmpty()) {
                logger.info("Saving {} records from file {}", dataList.size(), filename);
                for (int i = 0; i < dataList.size(); i += 500) {
                    int end = Math.min(i + 500, dataList.size());
                    airQualityRepository.saveAll(dataList.subList(i, end));
                    logger.debug("Saved batch {} to {}", i, end);
                }
                logger.info("Successfully processed file {}", filename);
            } else {
                logger.warn("No valid data found in file {}", filename);
            }

        } catch (IOException | CsvException e) {
            logger.error("Error processing CSV file: {}", filename, e);
        }
    }

    private Double parseDoubleOrNull(String value) {
        if (value == null || value.trim().isEmpty() || value.trim().equalsIgnoreCase("null")) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public List<AirQualityData> getDataByDate(LocalDate date) {
        return airQualityRepository.findByDate(date);
    }

    public List<AirQualityData> getDataByDateRange(LocalDate startDate, LocalDate endDate) {
        return airQualityRepository.findByDateRange(startDate, endDate);
    }

    public List<LocalDate> getAvailableDates() {
        return airQualityRepository.findAllDistinctDates();
    }

    public DailyStatsDTO getDailyStats(LocalDate date) {
        Map<String, Object> result = airQualityRepository.getDailyStatsByDateNative(date);

        System.out.println("DEBUG - Wynik zapytania dla daty " + date + ":");
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            System.out.println(entry.getKey() + ": " +
                    (entry.getValue() != null ?
                            entry.getValue().getClass().getName() + " = " + entry.getValue() :
                            "null"));
        }

        Map<String, Double> averages = new HashMap<>();
        Map<String, Double> minimums = new HashMap<>();
        Map<String, Double> maximums = new HashMap<>();

        if (result != null && !result.isEmpty()) {
            String[] parameters = {"pm25", "pm10", "iaq", "hcho", "co2", "p", "tin", "tout",
                    "rhin", "rhout", "no2", "no", "so2", "h2s", "co", "hcn",
                    "hcl", "nh3", "ec"};

            for (String param : parameters) {
                processParameter(result, averages, minimums, maximums, param);
            }
        } else {
            System.out.println("Brak danych dla daty: " + date);
        }

        return new DailyStatsDTO(date, averages, minimums, maximums);
    }

    private void processParameter(Map<String, Object> result,
                                  Map<String, Double> averages,
                                  Map<String, Double> minimums,
                                  Map<String, Double> maximums,
                                  String param) {
        String avgKey = "avg" + param.substring(0, 1).toUpperCase() + param.substring(1);
        String minKey = "min" + param.substring(0, 1).toUpperCase() + param.substring(1);
        String maxKey = "max" + param.substring(0, 1).toUpperCase() + param.substring(1);

        // Obsługa wartości średniej
        try {
            Object avgObj = result.get(avgKey);
            if (avgObj != null) {
                if (avgObj instanceof Number) {
                    averages.put(param, ((Number) avgObj).doubleValue());
                } else {
                    System.out.println("WARNING: " + avgKey + " nie jest liczbą: " + avgObj.getClass().getName());
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR processing " + avgKey + ": " + e.getMessage());
        }

        // Obsługa wartości minimalnej
        try {
            Object minObj = result.get(minKey);
            if (minObj != null) {
                if (minObj instanceof Number) {
                    minimums.put(param, ((Number) minObj).doubleValue());
                } else {
                    System.out.println("WARNING: " + minKey + " nie jest liczbą: " + minObj.getClass().getName());
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR processing " + minKey + ": " + e.getMessage());
        }

        // Obsługa wartości maksymalnej
        try {
            Object maxObj = result.get(maxKey);
            if (maxObj != null) {
                if (maxObj instanceof Number) {
                    maximums.put(param, ((Number) maxObj).doubleValue());
                } else {
                    System.out.println("WARNING: " + maxKey + " nie jest liczbą: " + maxObj.getClass().getName());
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR processing " + maxKey + ": " + e.getMessage());
        }
    }

    public void initCsvDirectory() {
        File dir = new File(csvDirectory);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                logger.info("Created CSV monitoring directory: {}", csvDirectory);
            } else {
                logger.error("Failed to create CSV monitoring directory: {}", csvDirectory);
            }
        } else {
            logger.info("CSV monitoring directory exists: {}", csvDirectory);
        }
    }
}