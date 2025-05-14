CREATE TABLE air_quality_data (
                                  id SERIAL PRIMARY KEY,
                                  date DATE NOT NULL,
                                  time TIME NOT NULL,
                                  pm25 DOUBLE PRECISION,
                                  pm10 DOUBLE PRECISION,
                                  iaq DOUBLE PRECISION,
                                  hcho DOUBLE PRECISION,
                                  co2 DOUBLE PRECISION,
                                  p DOUBLE PRECISION,
                                  tin DOUBLE PRECISION,
                                  tout DOUBLE PRECISION,
                                  rhin DOUBLE PRECISION,
                                  rhout DOUBLE PRECISION,
                                  lat DOUBLE PRECISION,
                                  lon DOUBLE PRECISION,
                                  hdg DOUBLE PRECISION,
                                  amsl DOUBLE PRECISION,
                                  agl DOUBLE PRECISION,
                                  mil DOUBLE PRECISION,
                                  no2 DOUBLE PRECISION,
                                  no DOUBLE PRECISION,
                                  so2 DOUBLE PRECISION,
                                  h2s DOUBLE PRECISION,
                                  co DOUBLE PRECISION,
                                  hcn DOUBLE PRECISION,
                                  hcl DOUBLE PRECISION,
                                  nh3 DOUBLE PRECISION,
                                  ec DOUBLE PRECISION,
                                  mrk VARCHAR(255),
                                  source_file VARCHAR(255) NOT NULL
);

-- Create an index on date for faster queries
CREATE INDEX idx_air_quality_date ON air_quality_data (date);

-- Create an index on source_file to quickly check if file was already processed
CREATE INDEX idx_air_quality_source_file ON air_quality_data (source_file);

-- Create a composite index on date and time for range queries
CREATE INDEX idx_air_quality_date_time ON air_quality_data (date, time);