package org.example.T3;

import javax.json.*;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;

public class JsonIo {
    public static JsonValue read(String uri) throws IOException {
        if (uri.toLowerCase().startsWith("http://")) {
            return readHttp(new URL(uri));
        } else if (uri.toLowerCase().startsWith("https://")) {
            return readHttps(new URL(uri));
        } else {
            return readFile(uri);
        }
    }

    public static void writeFile(JsonValue value, File file) throws FileNotFoundException {
        try (PrintWriter pw = new PrintWriter(file); JsonWriter jw = Json.createWriter(pw)) {
            if (value.getValueType() == JsonValue.ValueType.OBJECT) {
                jw.writeObject((JsonObject)value);
            } else if (value.getValueType() == JsonValue.ValueType.ARRAY) {
                jw.writeArray((JsonArray)value);
            } else {
                throw new IllegalArgumentException("Unsupported subtype of JsonValue");
            }
        }
    }

    public static JsonValue readHttp(URL url) throws IOException {
        try (InputStream is = url.openStream(); JsonReader reader = Json.createReader(is)) {
            return reader.read();
        }
    }

    public static JsonValue readHttps(URL url) throws IOException {
        HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
        try {
            try (JsonReader reader = Json.createReader(conn.getInputStream())) {
                return reader.read();
            }
        } finally {
            // Se cierra tanto la conexión como el socket. Llamando al InputStream#close
            // no se llega a cerrar el socket, aunque se siga llamando a disconnect
            // después.
            conn.disconnect();
        }
    }

    public static JsonValue readFile(String path) throws IOException {
        try (FileReader fReader = new FileReader(path)) {
            try (JsonReader jsonReader = Json.createReader(fReader)) {
                return jsonReader.read();
            }
        }
    }
}
