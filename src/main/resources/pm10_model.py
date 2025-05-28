# pm10_model_cli.py - Zmodyfikowany skrypt do uruchamiania z Spring Boot
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
from sklearn.preprocessing import StandardScaler
import glob
import os
import json
import sys
import argparse
from datetime import datetime, timedelta
import warnings

warnings.filterwarnings('ignore')

# Ustawienia polskie dla wykresów
plt.rcParams['font.size'] = 10
plt.rcParams['figure.max_open_warning'] = 0
sns.set_style("whitegrid")

# Fix dla PyCharm matplotlib
import matplotlib
matplotlib.use('Agg')  # Backend bez GUI dla serwera

class PM10Predictor:
    def __init__(self, data_folder_path):
        self.data_folder_path = data_folder_path
        self.df = None
        self.model = None
        self.scaler = StandardScaler()
        self.feature_columns = []

    def load_and_combine_data(self):
        """Wczytuje i łaczy wszystkie pliki CSV z folderu"""
        print("[INFO] Wczytywanie plikow CSV...")

        csv_files = glob.glob(os.path.join(self.data_folder_path, "*.csv"))

        if not csv_files:
            print(f"[ERROR] Nie znaleziono plikow CSV w folderze: {self.data_folder_path}")
            return False

        print(f"[INFO] Znaleziono {len(csv_files)} plikow CSV")

        dataframes = []

        for file in csv_files:
            try:
                df_temp = None
                separators = ['\t', ',', ';', '|']

                for sep in separators:
                    try:
                        df_temp = pd.read_csv(file, sep=sep)
                        if len(df_temp.columns) > 1:
                            break
                    except:
                        continue

                if df_temp is None or len(df_temp.columns) <= 1:
                    print(f"[WARN] Nie mozna odczytac struktury pliku: {os.path.basename(file)}")
                    continue

                dataframes.append(df_temp)
                print(f"[OK] Wczytano: {os.path.basename(file)} - {len(df_temp)} rekordow")

            except Exception as e:
                print(f"[ERROR] Błąd wczytywania {file}: {e}")

        if not dataframes:
            print("[ERROR] Nie udało się wczytać żadnych danych")
            return False

        self.df = pd.concat(dataframes, ignore_index=True)
        print(f"[SUCCESS] Łącznie wczytano {len(self.df)} rekordów")

        return True

    def preprocess_data(self):
        """Przetwarzanie i czyszczenie danych"""
        print("[INFO] Przetwarzanie danych...")

        # Automatyczne wykrywanie kolumn czasowych
        time_columns = [col for col in self.df.columns if 'time' in col.lower()]
        date_columns = [col for col in self.df.columns if 'date' in col.lower()]

        datetime_created = False

        # Opcja 1: Date + Time
        if date_columns and time_columns:
            try:
                self.df['datetime'] = pd.to_datetime(self.df[date_columns[0]] + ' ' + self.df[time_columns[0]])
                datetime_created = True
                print(f"[OK] Utworzono datetime z kolumn: {date_columns[0]} + {time_columns[0]}")
            except Exception as e:
                print(f"[ERROR] Błąd łączenia Date+Time: {e}")

        # Sprawdzenie kolumny PM10
        pm10_columns = [col for col in self.df.columns if 'PM10' in col or 'pm10' in col]
        if not pm10_columns:
            print("[ERROR] Nie znaleziono kolumny PM10!")
            return False

        pm10_col = pm10_columns[0]
        if pm10_col != 'PM10':
            self.df['PM10'] = self.df[pm10_col]

        # Filtrowanie nieprawidłowych dat
        current_year = datetime.now().year
        future_mask = self.df['datetime'].dt.year > current_year + 1
        future_count = future_mask.sum()

        if future_count > 0:
            print(f"[WARN] Usuwam {future_count} rekordów z przyszłymi datami")
            self.df = self.df[~future_mask]

        # Sortowanie według czasu
        self.df = self.df.sort_values('datetime').reset_index(drop=True)

        # Usunięcie duplikatów czasowych
        before_duplicates = len(self.df)
        self.df = self.df.drop_duplicates(subset=['datetime']).reset_index(drop=True)
        duplicates_removed = before_duplicates - len(self.df)
        if duplicates_removed > 0:
            print(f"[INFO] Usunięto {duplicates_removed} duplikatów czasowych")

        # Filtrowanie outlierów PM10
        self.df['PM10'] = pd.to_numeric(self.df['PM10'], errors='coerce')

        # Usuń wartości NaN i ujemne
        nan_count = self.df['PM10'].isna().sum()
        if nan_count > 0:
            print(f"[INFO] Usuwam {nan_count} wartości NaN PM10")
            self.df = self.df.dropna(subset=['PM10'])

        negative_count = (self.df['PM10'] < 0).sum()
        if negative_count > 0:
            print(f"[INFO] Usuwam {negative_count} ujemnych wartości PM10")
            self.df = self.df[self.df['PM10'] >= 0]

        # Usuń ekstremalne outliers
        extreme_mask = self.df['PM10'] > 1000
        extreme_count = extreme_mask.sum()
        if extreme_count > 0:
            print(f"[INFO] Usuwam {extreme_count} ekstremalnych outlierów PM10 (>1000)")
            self.df = self.df[~extreme_mask]

        # Tworzenie cech czasowych
        self.df['hour'] = self.df['datetime'].dt.hour
        self.df['day_of_week'] = self.df['datetime'].dt.dayofweek
        self.df['day_of_year'] = self.df['datetime'].dt.dayofyear
        self.df['month'] = self.df['datetime'].dt.month

        # Tworzenie cech opóźnionych
        for lag in [1, 2, 3, 6, 12, 24]:
            self.df[f'PM10_lag_{lag}'] = self.df['PM10'].shift(lag)

        # Średnie kroczące
        for window in [5, 10, 30, 60]:
            self.df[f'PM10_ma_{window}'] = self.df['PM10'].rolling(window=window, min_periods=1).mean()

        # Automatyczne wykrywanie dostępnych cech
        available_features = []
        potential_features = ['PM25', 'IAQ', 'HCHO', 'CO2', 'TIN', 'TOUT', 'RHIN', 'RHOUT',
                              'P', 'NO2', 'NO', 'SO2', 'H2S', 'CO', 'HCN', 'HCL', 'NH3']

        for feature in potential_features:
            matching_cols = [col for col in self.df.columns if feature in col]
            if matching_cols:
                col_name = matching_cols[0]
                if col_name != feature:
                    self.df[feature] = self.df[col_name]
                available_features.append(feature)

        # Wybór cech do modelu
        self.feature_columns = available_features + [
            'hour', 'day_of_week', 'day_of_year', 'month'
        ]

        # Dodanie cech opóźnionych i średnich kroczących
        lag_features = [col for col in self.df.columns if 'PM10_lag_' in col or 'PM10_ma_' in col]
        self.feature_columns.extend(lag_features)

        # Konwersja wszystkich cech do numerycznych
        for col in self.feature_columns:
            if col in self.df.columns:
                self.df[col] = pd.to_numeric(self.df[col], errors='coerce')

        # Usunięcie wierszy z NaN w cechach
        before_cleanup = len(self.df)
        self.df = self.df.dropna(subset=self.feature_columns + ['PM10']).reset_index(drop=True)
        cleanup_removed = before_cleanup - len(self.df)

        if cleanup_removed > 0:
            print(f"[INFO] Usunięto {cleanup_removed} wierszy z brakującymi danymi")

        print(f"[SUCCESS] Przygotowano {len(self.df)} rekordów z {len(self.feature_columns)} cechami")

        return True

    def create_features_and_target(self):
        """Przygotowanie cech i zmiennej docelowej"""
        X = self.df[self.feature_columns].copy()
        y = self.df['PM10'].shift(-1)
        X = X[:-1]
        y = y[:-1]
        return X, y

    def train_model(self, X, y):
        """Trenowanie modelu Random Forest"""
        print("[INFO] Trenowanie modelu...")

        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.2, random_state=42, shuffle=False
        )

        X_train_scaled = self.scaler.fit_transform(X_train)
        X_test_scaled = self.scaler.transform(X_test)

        self.model = RandomForestRegressor(
            n_estimators=200,
            max_depth=15,
            min_samples_split=10,
            min_samples_leaf=5,
            max_features='sqrt',
            random_state=42,
            n_jobs=-1
        )

        self.model.fit(X_train_scaled, y_train)

        y_pred = self.model.predict(X_test_scaled)

        mae = mean_absolute_error(y_test, y_pred)
        rmse = np.sqrt(mean_squared_error(y_test, y_pred))
        r2 = r2_score(y_test, y_pred)

        print(f"[METRICS] MAE: {mae:.3f}, RMSE: {rmse:.3f}, R^2: {r2:.3f}")

        return X_train, X_test, y_train, y_test, y_pred

    def predict_future(self, hours_ahead=72):
        """Predykcja PM10 na zadaną liczbę godzin do przodu"""
        last_row = self.df.iloc[-1].copy()
        predictions = []
        timestamps = []

        current_time = last_row['datetime']

        for hour in range(1, hours_ahead + 1):
            future_time = current_time + timedelta(hours=hour)

            last_row['hour'] = future_time.hour
            last_row['day_of_week'] = future_time.dayofweek
            last_row['day_of_year'] = future_time.dayofyear
            last_row['month'] = future_time.month

            features = last_row[self.feature_columns].values.reshape(1, -1)
            features_scaled = self.scaler.transform(features)

            pred = self.model.predict(features_scaled)[0]
            predictions.append(max(0, pred))
            timestamps.append(future_time)

            if 'PM10_lag_1' in self.feature_columns:
                last_row['PM10_lag_1'] = pred

        return timestamps, predictions

    def get_prediction_data_for_api(self, hours_ahead=72):
        """Zwraca dane predykcji w formacie JSON dla Spring Boot"""
        future_times, future_predictions = self.predict_future(hours_ahead=hours_ahead)

        current_pm10 = float(self.df['PM10'].iloc[-100:].mean())

        pred_stats = {
            'mean': float(np.mean(future_predictions)),
            'min': float(np.min(future_predictions)),
            'max': float(np.max(future_predictions)),
            'std': float(np.std(future_predictions))
        }

        def get_air_quality(pm10_value):
            if pm10_value <= 15:
                return {"level": "Bardzo dobra", "color": "green", "code": 1}
            elif pm10_value <= 25:
                return {"level": "Dobra", "color": "yellow", "code": 2}
            elif pm10_value <= 50:
                return {"level": "Umiarkowana", "color": "orange", "code": 3}
            elif pm10_value <= 75:
                return {"level": "Zła", "color": "red", "code": 4}
            else:
                return {"level": "Bardzo zła", "color": "black", "code": 5}

        # Predykcje dzienne
        pred_series = pd.Series(future_predictions, index=future_times)
        daily_means = pred_series.groupby(pred_series.index.date).mean()

        daily_predictions = []
        for i, (date, mean_val) in enumerate(daily_means.items()):
            daily_predictions.append({
                "day": i + 1,
                "date": date.isoformat(),
                "averagePm10": float(mean_val),
                "airQuality": get_air_quality(mean_val)
            })

        # Predykcje godzinowe
        hourly_predictions = []
        for time, pred in zip(future_times, future_predictions):
            hourly_predictions.append({
                "datetime": time.isoformat(),
                "pm10Value": float(pred),
                "hour": time.hour,
                "day": time.day
            })

        return {
            "success": True,
            "currentStatus": {
                "currentPm10": current_pm10,
                "airQuality": get_air_quality(current_pm10)
            },
            "predictionSummary": {
                "hoursAhead": hours_ahead,
                "averagePm10": pred_stats['mean'],
                "minPm10": pred_stats['min'],
                "maxPm10": pred_stats['max'],
                "stdPm10": pred_stats['std'],
                "overallAirQuality": get_air_quality(pred_stats['mean'])
            },
            "dailyPredictions": daily_predictions,
            "hourlyPredictions": hourly_predictions,
            "generatedAt": datetime.now().isoformat()
        }

def main():
    parser = argparse.ArgumentParser(description='PM10 Prediction Model')
    parser.add_argument('--action', choices=['train', 'predict'], required=True)
    parser.add_argument('--hours', type=int, default=72)
    parser.add_argument('--data-folder', default='folder')
    parser.add_argument('--output-format', choices=['json', 'text'], default='json')

    args = parser.parse_args()

    predictor = PM10Predictor(args.data_folder)

    try:
        # Wczytanie i przetworzenie danych
        if not predictor.load_and_combine_data():
            if args.output_format == 'json':
                print(json.dumps({"success": False, "error": "Błąd wczytywania danych"}))
            else:
                print("[ERROR] Błąd wczytywania danych")
            return 1

        if not predictor.preprocess_data():
            if args.output_format == 'json':
                print(json.dumps({"success": False, "error": "Błąd przetwarzania danych"}))
            return 1

        if len(predictor.df) < 100:
            if args.output_format == 'json':
                print(json.dumps({"success": False, "error": "Za mało danych do trenowania"}))
            return 1

        # Przygotowanie danych
        X, y = predictor.create_features_and_target()

        if args.action == 'train':
            # Trenowanie modelu
            X_train, X_test, y_train, y_test, y_pred = predictor.train_model(X, y)

            if args.output_format == 'json':
                mae = float(mean_absolute_error(y_test, y_pred))
                rmse = float(np.sqrt(mean_squared_error(y_test, y_pred)))
                r2 = float(r2_score(y_test, y_pred))

                result = {
                    "success": True,
                    "message": "Model wytrenowany pomyślnie",
                    "metrics": {"mae": mae, "rmse": rmse, "r2_score": r2}
                }
                print(json.dumps(result))
            else:
                print("[SUCCESS] Model został pomyślnie wytrenowany!")

        elif args.action == 'predict':
            # Najpierw wytrenuj model
            X_train, X_test, y_train, y_test, y_pred = predictor.train_model(X, y)

            # Wykonaj predykcje
            prediction_data = predictor.get_prediction_data_for_api(args.hours)

            if args.output_format == 'json':
                print(json.dumps(prediction_data))
            else:
                print("[SUCCESS] Predykcje wykonane pomyślnie!")
                print(f"Średnia PM10: {prediction_data['predictionSummary']['averagePm10']:.2f}")

        return 0

    except Exception as e:
        if args.output_format == 'json':
            print(json.dumps({"success": False, "error": str(e)}))
        else:
            print(f"[ERROR] Błąd: {e}")
        return 1

if __name__ == "__main__":
    sys.exit(main())