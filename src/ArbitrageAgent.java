import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ArbitrageAgent extends Agent {
    private Map<String, Double> marketPrices = new HashMap<>(); // Stores prices from different sources
    private double threshold = 0.001; // Arbitrage threshold (adjust as needed)

    protected void setup() {
        System.out.println(getLocalName() + " is monitoring arbitrage opportunities...");

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    try {
                        String content = msg.getContent().trim();
                        JSONObject data = new JSONObject(content);
                        String market = data.getString("market");
                        double price = data.getDouble("price");

                        marketPrices.put(market, price);

                        if (marketPrices.size() > 1) {
                            checkArbitrage();
                        }
                    } catch (Exception e) {
                        System.out.println("Error processing arbitrage data: " + e.getMessage());
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void checkArbitrage() {
        String[] markets = marketPrices.keySet().toArray(new String[0]);
        for (int i = 0; i < markets.length; i++) {
            for (int j = i + 1; j < markets.length; j++) {
                double priceA = marketPrices.get(markets[i]);
                double priceB = marketPrices.get(markets[j]);

                if (Math.abs(priceA - priceB) > threshold) {
                    System.out.println("⚡ Arbitrage Opportunity Detected! Buy from " + markets[i] +
                            " at " + priceA + " and sell at " + markets[j] + " at " + priceB);

                    JSONObject tradeSuggestion = new JSONObject();
                    tradeSuggestion.put("action", "arbitrage");
                    tradeSuggestion.put("buyMarket", markets[i]);
                    tradeSuggestion.put("buyPrice", priceA);
                    tradeSuggestion.put("sellMarket", markets[j]);
                    tradeSuggestion.put("sellPrice", priceB);

                    sendMessage("TradingAgent", tradeSuggestion.toString());
                }
            }
        }
    }

    private void sendMessage(String receiver, String content) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
        msg.setContent(content);
        send(msg);
    }
}
