import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TrendAnalysisAgent extends Agent {
    private static final String API_KEY = "CLTKPOL1FGLF39UH";
    private static final String SYMBOL = "EURUSD";
    private static final int SMA_PERIOD = 14;
    private static final int EMA_PERIOD = 14;

    protected void setup() {
        System.out.println(getLocalName() + " is analyzing market trends...");

        addBehaviour(new TickerBehaviour(this, 10000) { // Fetch every 10 seconds
            protected void onTick() {
                analyzeTrends();
            }
        });
    }

    private void analyzeTrends() {
        try {
            double sma = fetchIndicator("SMA", SMA_PERIOD);
            double ema = fetchIndicator("EMA", EMA_PERIOD);

            String trend = determineTrend(sma, ema);
            System.out.println(getLocalName() + " - SMA: " + sma + ", EMA: " + ema + ", Trend: " + trend);

            sendMessage("TradingAgent", sma, ema, trend);
        } catch (Exception e) {
            System.out.println("Error analyzing market trends: " + e.getMessage());
        }
    }

    private double fetchIndicator(String function, int period) throws Exception {
        String url = "https://www.alphavantage.co/query?function=" + function + "&symbol=" + SYMBOL +
                "&interval=1min&time_period=" + period + "&series_type=close&apikey=" + API_KEY;
        JSONObject response = getJsonFromUrl(url);
        String lastTimestamp = response.getJSONObject("Technical Analysis: " + function).keys().next();
        return response.getJSONObject("Technical Analysis: " + function).getJSONObject(lastTimestamp).getDouble(function);
    }

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
        return new JSONObject(content.toString());
    }

    private String determineTrend(double sma, double ema) {
        if (ema > sma) {
            return "Bullish";
        } else if (ema < sma) {
            return "Bearish";
        } else {
            return "Ranging";
        }
    }

    private void sendMessage(String receiver, double sma, double ema, String trend) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
        msg.setContent(String.format("SMA: %f, EMA: %f, Trend: %s", sma, ema, trend));
        send(msg);
    }
}
