import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class MarketDataAgent extends Agent {
    private static final String API_KEY = "CLTKPOL1FGLF39UH";
    private static final String FROM_CURRENCY = "EUR";  // Base currency
    private static final String TO_CURRENCY = "USD";    // Quote currency
    private static final int SMA_PERIOD = 14;
    private static final int RSI_PERIOD = 14;

    private double lastClosePrice = 0;

    protected void setup() {
        System.out.println(getLocalName() + " is fetching market data...");

        addBehaviour(new TickerBehaviour(this, 5000) {  // Fetch data every 5 sec
            protected void onTick() {
                fetchMarketData();
            }
        });
    }

//    private void fetchMarketData() {
//        try {
//            // ✅ NEW URL for real-time Forex exchange rate
//            String url = "https://www.alphavantage.co/query?function=CURRENCY_EXCHANGE_RATE&from_currency=" + FROM_CURRENCY +
//                    "&to_currency=" + TO_CURRENCY + "&apikey=" + API_KEY;
//            JSONObject response = getJsonFromUrl(url);
//
//            // ✅ Check if "Realtime Currency Exchange Rate" exists
//            if (!response.has("Realtime Currency Exchange Rate")) {
//                System.out.println("Error: 'Realtime Currency Exchange Rate' key not found in API response.");
//                System.out.println("DEBUG: Full API Response -> " + response.toString());
//                return; // Exit early if data is missing
//            }
//
//            JSONObject exchangeRateData = response.getJSONObject("Realtime Currency Exchange Rate");
//            lastClosePrice = exchangeRateData.getDouble("5. Exchange Rate");
//
//            double sma = fetchIndicator("SMA", SMA_PERIOD);
//            double rsi = fetchIndicator("RSI", RSI_PERIOD);
//
//            System.out.println(getLocalName() + " - Price: " + lastClosePrice + ", SMA: " + sma + ", RSI: " + rsi);
//
//            // ✅ Send data to TradingAgent
//            sendMessage("TradingAgent", lastClosePrice, sma, rsi);
//
//        } catch (Exception e) {
//            System.out.println("Error fetching market data: " + e.getMessage());
//        }
//    }

    private void fetchMarketData() {
        try {
            // URL for real-time Forex exchange rate
            String url = "https://www.alphavantage.co/query?function=CURRENCY_EXCHANGE_RATE&from_currency=" + FROM_CURRENCY +
                    "&to_currency=" + TO_CURRENCY + "&apikey=" + API_KEY;
            JSONObject response = getJsonFromUrl(url);

            // Print full response for debugging
            System.out.println("DEBUG: Full API Response -> " + response.toString());

            // Check if "Realtime Currency Exchange Rate" exists
            if (!response.has("Realtime Currency Exchange Rate")) {
                System.out.println("Error: 'Realtime Currency Exchange Rate' key not found in API response.");
                return; // Exit early if data is missing
            }

            JSONObject exchangeRateData = response.getJSONObject("Realtime Currency Exchange Rate");
            lastClosePrice = exchangeRateData.getDouble("5. Exchange Rate");

            double sma = fetchIndicator("SMA", SMA_PERIOD);
            double rsi = fetchIndicator("RSI", RSI_PERIOD);

            System.out.println(getLocalName() + " - Price: " + lastClosePrice + ", SMA: " + sma + ", RSI: " + rsi);

            // Send data to TradingAgent
            sendMessage("TradingAgent", lastClosePrice, sma, rsi);

        } catch (Exception e) {
            System.out.println("Error fetching market data: " + e.getMessage());
        }
    }


    private double fetchIndicator(String function, int period) throws Exception {
        // Corrected URL for SMA and RSI requests
        String url = "https://www.alphavantage.co/query?function=" + function + "&symbol=" + FROM_CURRENCY + TO_CURRENCY +
                "&interval=5min&time_period=" + period + "&series_type=close&apikey=" + API_KEY;
        JSONObject response = getJsonFromUrl(url);

        // Debugging full response to ensure it's correct
        System.out.println("DEBUG: " + function + " API Response -> " + response.toString());

        // Check if the response contains the Technical Analysis data
        if (!response.has("Technical Analysis: " + function)) {
            System.out.println("Error: 'Technical Analysis: " + function + "' key not found. Full response: " + response.toString());
            return -1;  // Return an invalid value in case of an error
        }

        // Extract the last timestamp and return the SMA or RSI value
        String lastTimestamp = response.getJSONObject("Technical Analysis: " + function).keys().next();
        return response.getJSONObject("Technical Analysis: " + function).getJSONObject(lastTimestamp).getDouble(function);
    }


    private JSONObject getJsonFromUrl(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        conn.disconnect();

        return new JSONObject(content.toString());
    }

    private void sendMessage(String receiver, double price, double sma, double rsi) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
        msg.setContent(String.format("%f,%f,%f", price, sma, rsi));  // Send as CSV format
        send(msg);
    }
}
