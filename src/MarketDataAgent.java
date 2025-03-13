import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MarketDataAgent extends Agent {
    private static final String SYMBOL = "EUR/USD"; // Alpha Vantage format for forex pairs
    private static final int FETCH_INTERVAL = 12000; // Fetch every 12 sec to stay under 5 requests/min
    private static final int RSI_PERIOD = 14; // RSI period

    private static final String API_KEY = "CLTKPOL1FGLF39UH";  // Replace with your Alpha Vantage API key
    private boolean fetchSMA = true; // Toggle between SMA and RSI to stay within limits

    protected void setup() {
        System.out.println(getLocalName() + " is fetching market data...");

        // Add a TickerBehaviour to periodically fetch data
        addBehaviour(new TickerBehaviour(this, FETCH_INTERVAL) {
            protected void onTick() {
                fetchMarketData();
            }
        });
    }

    private void fetchMarketData() {
        try {
            // Fetch live EUR/USD exchange rate from AlphaVantage
            String priceUrl = String.format(
                    "https://www.alphavantage.co/query?function=CURRENCY_EXCHANGE_RATE&from_currency=EUR&to_currency=USD&apikey=%s",
                    API_KEY
            );
            JSONObject priceResponse = getJsonFromUrl(priceUrl);
            double price = extractPrice(priceResponse);

            if (price == -1) {
                System.out.println("Failed to fetch price, skipping...");
                return;  // Skip processing if price is invalid
            }

            JSONObject marketData = new JSONObject();
            marketData.put("price", price);

            // Alternate between fetching SMA and RSI to stay within API limits
            if (fetchSMA) {
                String smaUrl = String.format(
                        "https://www.alphavantage.co/query?function=SMA&symbol=EURUSD&interval=5min&time_period=14&series_type=close&apikey=%s",
                        API_KEY
                );
                JSONObject smaResponse = getJsonFromUrl(smaUrl);
                double sma = extractSMA(smaResponse);
                marketData.put("sma", sma);
            } else {
                String rsiUrl = String.format(
                        "https://www.alphavantage.co/query?function=RSI&symbol=EURUSD&interval=5min&time_period=14&series_type=close&apikey=%s",
                        API_KEY
                );
                JSONObject rsiResponse = getJsonFromUrl(rsiUrl);
                double rsi = extractRSI(rsiResponse);
                marketData.put("rsi", rsi);
            }

            fetchSMA = !fetchSMA; // Toggle between SMA and RSI each request

            // Send data to TradingAgent for analysis
            sendMessage("TradingAgent", marketData);

        } catch (Exception e) {
            System.out.println("Error fetching market data: " + e.getMessage());
        }
    }

    // Updated function that should work for the API request
//    private void fetchMarketData() {
//        try {
//            String url = String.format("https://www.alphavantage.co/query?function=FX_INTRADAY&from_symbol=EUR&to_symbol=USD&interval=1min&apikey=%s", API_KEY);
//            String response = sendHttpRequest(url);
//            JSONObject json = new JSONObject(response);
//
//            // Check if 'Time Series FX (1min)' exists in the response
//            if (json.has("Time Series FX (1min)")) {
//                JSONObject timeSeries = json.getJSONObject("Time Series FX (1min)");
//                String latestTimestamp = timeSeries.keys().next(); // Get the most recent key
//                double price = timeSeries.getJSONObject(latestTimestamp).getDouble("1. open");
//                System.out.println("Latest EUR/USD price: " + price);
//
//                // Continue with fetching SMA and RSI...
//            } else if (json.has("Realtime Currency Exchange Rate")) {
//                JSONObject exchangeRateData = json.getJSONObject("Realtime Currency Exchange Rate");
//                double exchangeRate = exchangeRateData.getDouble("5. Exchange Rate");
//                System.out.println("Real-time EUR/USD exchange rate: " + exchangeRate);
//            } else {
//                System.out.println("Error: 'Time Series FX (1min)' not found in response.");
//            }
//        } catch (Exception e) {
//            System.out.println("Error fetching market data: " + e.getMessage());
//        }
//    }




    private JSONObject getJsonFromUrl(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        conn.disconnect();

        System.out.println("API Response: " + content.toString());  // Debugging

        return new JSONObject(content.toString());
    }

    private double extractPrice(JSONObject response) {
        try {
            JSONObject exchangeRateData = response.getJSONObject("Realtime Currency Exchange Rate");
            return exchangeRateData.getDouble("5. Exchange Rate");  // Extract the exchange rate
        } catch (Exception e) {
            System.out.println("Error parsing exchange rate: " + e.getMessage());
            return -1;  // Return -1 in case of error
        }
    }


    private double extractSMA(JSONObject response) {
        JSONObject technicalAnalysis = response.getJSONObject("Technical Analysis: SMA");

        // Get the latest available timestamp key
        String latestTimestamp = technicalAnalysis.keys().next();

        // Access the corresponding SMA value
        return technicalAnalysis.getJSONObject(latestTimestamp).getDouble("SMA");
    }

    private double extractRSI(JSONObject response) {
        JSONObject technicalAnalysis = response.getJSONObject("Technical Analysis: RSI");

        // Get the latest available timestamp key
        String latestTimestamp = technicalAnalysis.keys().next();

        // Access the corresponding RSI value
        return technicalAnalysis.getJSONObject(latestTimestamp).getDouble("RSI");
    }

    private void sendMessage(String receiver, JSONObject marketData) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
        msg.setContent(marketData.toString());
        send(msg);
    }
}
