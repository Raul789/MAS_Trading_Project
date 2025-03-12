import org.json.JSONObject;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AlphaVantageFetcher {
    public static void main(String[] args) {
        String apiKey = "YOUR_API_KEY";
        String symbol = "IBM";
        String interval = "1min";
        String urlString = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=" + symbol + "&interval=" + interval + "&apikey=" + apiKey;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            // Convert response to JSON
            JSONObject jsonResponse = new JSONObject(response.toString());

            // Check if "Time Series (1min)" exists
            if (jsonResponse.has("Time Series (1min)")) {
                JSONObject timeSeries = jsonResponse.getJSONObject("Time Series (1min)");

                // Get the latest timestamp
                String latestTimestamp = timeSeries.keys().next(); // Get the first key (most recent)
                JSONObject latestData = timeSeries.getJSONObject(latestTimestamp);

                // Extract relevant data
                double openPrice = latestData.getDouble("1. open");
                double closePrice = latestData.getDouble("4. close");

                System.out.println("Latest timestamp: " + latestTimestamp);
                System.out.println("Open Price: " + openPrice);
                System.out.println("Close Price: " + closePrice);
            } else {
                System.out.println("Error: 'Time Series (1min)' key not found in API response.");
                System.out.println("DEBUG: Full API Response -> " + jsonResponse.toString());
            }

        } catch (JSONException e) {
            System.out.println("JSON Parsing Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
