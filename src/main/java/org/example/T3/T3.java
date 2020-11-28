package org.example.T3;

import javax.json.*;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class T3 {
    private static final String OWM_APPID = "a975f935caf274ab016f4308ffa23453";
    private static final String EVENFUL_APP_KEY = "c2tPtVFTrSk8xnQS";

    public static void main(String[] args) throws IOException {
        // Ejercicio 1
        JsonObject ex1Res = weatherFromPlaceName("vigo");
        System.out.println(ex1Res);

        // Ejercicio 2
        JsonObject ex2Res = weatherFromLatLon("42.232819", "-8.72264");
        System.out.println(ex2Res);

        // Ejercicio 3
        JsonObject ex3Res = weatherListFromLatLon("55.5", "37.5", 20);
        System.out.println(ex3Res);

        // Ejercicio 4
        // ... La API de Google es de pago, o sea que entiendo que los ejercicios
        // que la piden no entran, ¿no?

        // Ejercicio 5
        System.out.println(getCityId(ex1Res));

        // Ejercicio 6
        System.out.println(getCityName(ex1Res));

        // Ejercicio 7
        System.out.println(getCoords(ex1Res));

        // Ejercicio 8
        System.out.println(exercise8(ex1Res));

        // Ejercicio 9
        for (Ex8Info info: exercise9(ex3Res)) {
            System.out.println(info);
        }

        // Ejercicios 10 - 12: requieren la API de Google.

        // Ejercicio 13
        for (TriviaQuestion q: getTriviaQuestions(20)) {
            System.out.println();
            System.out.println(q.question);
            System.out.printf("* %s\n", q.correctAnswer);
            for (String incorrectAnswer: q.incorrectAnswers) {
                System.out.printf("  %s\n", incorrectAnswer);
            }
        }

        // Ejercicio 14
        JsonArray events = getEvents("vigo", 250, 10);

        // Ejercicios 15 y 17
        for (JsonObject event: events.getValuesAs(JsonObject.class)) {
            JsonObject venue = getEventVenue(event);
            System.out.println();

            // La parte del ejercicio 15...
            printEventInfo(event);
            printVenueInfo(venue);

            // ...y la del 17.
            JsonObject venueWeather = getVenueWeather(venue);
            JsonObject reportArray = (JsonObject)venueWeather.getJsonArray("weather").get(0);
            System.out.print("Tiempo actual en el lugar del evento: ");
            System.out.println(reportArray.getString("description"));
        }

        // Ejercicio 16: está en azul, por lo que asumo que requiere la API de Google.
    }

    private static JsonObject weatherFromPlaceName(String name) throws IOException {
        return (JsonObject)JsonIo.read(String.format(
            "https://api.openweathermap.org/data/2.5/weather?APPID=%s&lang=%s&q=%s&units=metric",
                OWM_APPID,
                "es",
                name));
    }

    private static JsonObject weatherFromLatLon(String lat, String lon) throws IOException {
        return (JsonObject)JsonIo.read(String.format(
            "https://api.openweathermap.org/data/2.5/weather?APPID=%s&lang=%s&lat=%s&lon=%s",
                OWM_APPID,
                "es",
                lat,
                lon));
    }

    private static JsonObject weatherListFromLatLon(String lat, String lon, int cnt)
        throws IOException
    {
        return (JsonObject)JsonIo.read(String.format(
            "https://api.openweathermap.org/data/2.5/find?APPID=%s&lang=%s&lat=%s&lon=%s&cnt=%d",
                OWM_APPID,
                "es",
                lat,
                lon,
                cnt));
    }

    private static int getCityId(JsonObject data) {
        return data.getInt("id");
    }

    private static String getCityName(JsonObject data) {
        return data.getString("name");
    }

    private static class Coords {
        public String lat;
        public String lon;

        public Coords(String lat, String lon) {
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        public String toString() {
            return String.format("(%s, %s)", lat, lon);
        }
    }

    private static Coords getCoords(JsonObject data) {
        return new Coords(
                data.getJsonObject("coord").getJsonNumber("lat").toString(),
                data.getJsonObject("coord").getJsonNumber("lon").toString()
        );
    }

    private static class Ex8Info {
        private static final DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        public long date;
        public double temp;
        public double humidity;
        public double cloudiness;
        public double windSpeed;
        public String weatherReport;

        public Ex8Info(long date, double temp, double humidity, double cloudiness,
               double windSpeed, String weatherReport)
        {
            this.date = date;
            this.temp = temp;
            this.humidity = humidity;
            this.cloudiness = cloudiness;
            this.windSpeed = windSpeed;
            this.weatherReport = weatherReport;
        }

        @Override
        public String toString() {
            ZonedDateTime zdt = Instant.ofEpochSecond(date).atZone(ZoneId.of("GMT+1"));
            return "<"
                    + "fecha: " + zdt.format(formatter) + ", "
                    + "temperatura: " + String.valueOf(temp) + ", "
                    + "humedad: " + String.valueOf(humidity) + ", "
                    + "probabilidad nubes: " + String.valueOf(cloudiness) + ", "
                    + "velocidad viento: " + String.valueOf(windSpeed) + ", "
                    + "pronóstico: " + weatherReport + ">";
        }
    }

    private static Ex8Info exercise8(JsonObject data) {
        return new Ex8Info(
            data.getJsonNumber("dt").longValue(),
            data.getJsonObject("main").getJsonNumber("temp").doubleValue(),
            data.getJsonObject("main").getJsonNumber("humidity").doubleValue(),
            data.getJsonObject("clouds").getJsonNumber("all").doubleValue(),
            data.getJsonObject("wind").getJsonNumber("speed").doubleValue(),
            ((JsonObject)data.getJsonArray("weather").get(0)).getString("description"));
    }

    private static Ex8Info[] exercise9(JsonObject data) {
        int recordCount = data.getJsonArray("list").size();
        Ex8Info[] res = new Ex8Info[recordCount];

        for (int i = 0; i < recordCount; i++) {
            res[i] = exercise8((JsonObject)data.getJsonArray("list").get(i));
        }

        return res;
    }

    private static class TriviaQuestion {
        public String question;
        public String correctAnswer;
        public String[] incorrectAnswers;

        public TriviaQuestion(String question, String correctAnswer, String[] incorrectAnswers) {
            this.question = question;
            this.correctAnswer = correctAnswer;
            this.incorrectAnswers = incorrectAnswers;
        }
    }

    private static TriviaQuestion[] getTriviaQuestions(int count) throws IOException {
        JsonObject resp = (JsonObject)JsonIo.read(String.format(
                "https://opentdb.com/api.php?amount=%d&category=18&difficulty=hard&type=multiple",
                count));
        TriviaQuestion[] res = new TriviaQuestion[count];

        for (int i = 0; i < count; i++) {
            JsonObject q = (JsonObject)resp.getJsonArray("results").get(i);

            JsonArray incorrectAnswerArray = q.getJsonArray("incorrect_answers");
            String incorrectAnswers[] = new String[incorrectAnswerArray.size()];
            for (int j = 0; j < incorrectAnswers.length; j++) {
                incorrectAnswers[j] = ((JsonString)incorrectAnswerArray.get(j)).toString();
            }

            res[i] = new TriviaQuestion(
                    q.getString("question"),
                    q.getString("correct_answer"),
                    incorrectAnswers);
        }

        return res;
    }

    private static JsonArray getEvents(String location, int radiusKm, int maxCount)
        throws IOException
    {
        JsonObject resp = (JsonObject)JsonIo.read(String.format(
            "https://api.eventful.com/json/events/search?"
            + "app_key=%s&l=%s&within=%d&page_size=%d&page_number=1",
                EVENFUL_APP_KEY,
                location,
                radiusKm,
                maxCount));

        return resp.getJsonObject("events").getJsonArray("event");
    }

    private static void printVenueInfo(JsonObject event) {
        String name = event.isNull("name") ? "?" : event.getString("name");
        String latitude = event.isNull("latitude") ? "?" : event.getString("latitude");
        String longitude = event.isNull("longitude") ? "?" : event.getString("longitude");
        String address = event.isNull("address") ? "?" : event.getString("address");
        String postalCode = event.isNull("postal_code") ? "?" : event.getString("postal_code");
        String region = event.isNull("region") ? "?" : event.getString("region");
        String country = event.isNull("country") ? "?" : event.getString("country");

        System.out.println(
                "Nombre del lugar del evento: " + name + "\n"
                + "País: " + country + "\n"
                + "Región: " + region + "\n"
                + "Dirección: " + address + "\n"
                + "Código postal: " + postalCode + "\n"
                + "Latitud: " + latitude + "\n"
                + "Longitud: " + longitude);
    }

    private static JsonObject getEventVenue(JsonObject event) throws IOException {
        return (JsonObject)JsonIo.read(String.format(
                "https://api.eventful.com/json/venues/get?app_key=%s&id=%s",
                EVENFUL_APP_KEY,
                event.getString("venue_id")));
    }

    private static void printEventInfo(JsonObject event) throws IOException {
        String title = event.isNull("title") ? "?" : event.getString("title");
        String description = event.isNull("description") ? "?" : event.getString("description");

        System.out.println(
                "Título del evento: " + title + "\n"
                + "Descripción: " + description);
    }

    private static JsonObject getVenueWeather(JsonObject venue) throws IOException {
        String lat = venue.getString("latitude");
        String lon = venue.getString("longitude");

        return (JsonObject)JsonIo.read(String.format(
                "https://api.openweathermap.org/data/2.5/weather?"
                + "APPID=%s&lang=es&lat=%s&lon=%s&units=metric",
                OWM_APPID,
                lat,
                lon));
    }
}
