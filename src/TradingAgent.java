import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Collections;

public class TradingAgent extends Agent {
    private double sma;
    private double rsi;
    private double lastPrice;
    private static final String API_KEY = "CLTKPOL1FGLF39UH";  // Replace with your Alpha Vantage API key

    protected void setup() {
        System.out.println(getLocalName() + " is ready to trade...");
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                try {
                    // Fetch the latest data from Alpha Vantage API
                    fetchMarketData();
                    // Simulate some delay for the next cycle
                    Thread.sleep(5000);  // 5 seconds delay
                } catch (Exception e) {
                    System.out.println("Error fetching market data: " + e.getMessage());
                }
            }
        });
    }

    private void fetchMarketData() {
        try {
            // Fetch Latest EUR/USD Price
            String url = String.format("https://www.alphavantage.co/query?function=FX_INTRADAY&from_symbol=EUR&to_symbol=USD&interval=1min&apikey=%s", API_KEY);
            String response = sendHttpRequest(url);
            JSONObject json = new JSONObject(response);

            // Ensure the response contains data
            if (!json.has("Time Series FX (1min)")) {
                throw new Exception("Missing 'Time Series FX (1min)' in response.");
            }

            JSONObject timeSeries = json.getJSONObject("Time Series FX (1min)");

            // Get the latest timestamp dynamically
            String latestTimestamp = Collections.max(timeSeries.keySet());
            double price = timeSeries.getJSONObject(latestTimestamp).getDouble("1. open");

            // Fetch SMA for EUR/USD
            String smaUrl = String.format("https://www.alphavantage.co/query?function=SMA&symbol=EURUSD&interval=5min&time_period=14&series_type=close&apikey=%s", API_KEY);
            String smaResponse = sendHttpRequest(smaUrl);
            JSONObject smaJson = new JSONObject(smaResponse);

            // Ensure SMA response contains data
            if (!smaJson.has("Technical Analysis: SMA")) {
                throw new Exception("Missing 'Technical Analysis: SMA' in response.");
            }

            JSONObject smaData = smaJson.getJSONObject("Technical Analysis: SMA");

            // Get the latest SMA timestamp
            String latestSmaTimestamp = Collections.max(smaData.keySet());
            double sma = smaData.getJSONObject(latestSmaTimestamp).getDouble("SMA");

            // Fetch RSI for EUR/USD
            String rsiUrl = String.format("https://www.alphavantage.co/query?function=RSI&symbol=EURUSD&interval=5min&time_period=14&series_type=close&apikey=%s", API_KEY);
            String rsiResponse = sendHttpRequest(rsiUrl);
            JSONObject rsiJson = new JSONObject(rsiResponse);

            // Ensure RSI response contains data
            if (!rsiJson.has("Technical Analysis: RSI")) {
                throw new Exception("Missing 'Technical Analysis: RSI' in response.");
            }

            JSONObject rsiData = rsiJson.getJSONObject("Technical Analysis: RSI");

            // Get the latest RSI timestamp
            String latestRsiTimestamp = Collections.max(rsiData.keySet());
            double rsi = rsiData.getJSONObject(latestRsiTimestamp).getDouble("RSI");

            // Send data to Trading Agent
            JSONObject marketData = new JSONObject();
            marketData.put("price", price);
            marketData.put("sma", sma);
            marketData.put("rsi", rsi);
            sendTradeSignal(marketData);

        } catch (Exception e) {
            System.out.println("Error fetching market data: " + e.getMessage());
        }
    }


    private String sendHttpRequest(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    private void sendTradeSignal(JSONObject marketData) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("TradingExecutor", AID.ISLOCALNAME));
        msg.setContent(marketData.toString());
        send(msg);
    }
}
