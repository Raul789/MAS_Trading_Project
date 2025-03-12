import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;

public class MomentumAgent extends Agent {
    private static final int RSI_PERIOD = 14;
    private static final int STOCHASTIC_PERIOD = 14;
    private static final int MOMENTUM_PERIOD = 10;

    private Queue<Double> priceHistory = new LinkedList<>();
    private double lastClosePrice = 0;

    protected void setup() {
        System.out.println(getLocalName() + " is analyzing momentum indicators...");

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    try {
                        String content = msg.getContent().trim();
                        JSONObject data = new JSONObject(content);
                        lastClosePrice = data.getDouble("price");

                        priceHistory.add(lastClosePrice);
                        if (priceHistory.size() > RSI_PERIOD) {
                            priceHistory.poll();
                        }

                        if (priceHistory.size() == RSI_PERIOD) {
                            double rsi = calculateRSI();
                            double stochastic = calculateStochastic();
                            double momentum = calculateMomentum();

                            JSONObject indicators = new JSONObject();
                            indicators.put("rsi", rsi);
                            indicators.put("stochastic", stochastic);
                            indicators.put("momentum", momentum);
                            indicators.put("signal", determineSignal(rsi, stochastic, momentum));

                            sendMessage("TradingAgent", indicators.toString());
                        }
                    } catch (Exception e) {
                        System.out.println("Error processing momentum data: " + e.getMessage());
                    }
                } else {
                    block();
                }
            }
        });
    }

    private double calculateRSI() {
        double gain = 0, loss = 0;
        Double[] prices = priceHistory.toArray(new Double[0]);

        for (int i = 1; i < prices.length; i++) {
            double change = prices[i] - prices[i - 1];
            if (change > 0) {
                gain += change;
            } else {
                loss -= change;
            }
        }

        double avgGain = gain / RSI_PERIOD;
        double avgLoss = loss / RSI_PERIOD;
        double rs = avgLoss == 0 ? 100 : avgGain / avgLoss;

        return 100 - (100 / (1 + rs));
    }

    private double calculateStochastic() {
        double highestHigh = Double.MIN_VALUE;
        double lowestLow = Double.MAX_VALUE;

        for (double price : priceHistory) {
            if (price > highestHigh) highestHigh = price;
            if (price < lowestLow) lowestLow = price;
        }

        return 100 * ((lastClosePrice - lowestLow) / (highestHigh - lowestLow));
    }

    private double calculateMomentum() {
        Double[] prices = priceHistory.toArray(new Double[0]);
        return lastClosePrice - prices[0]; // Momentum over the period
    }

    private String determineSignal(double rsi, double stochastic, double momentum) {
        if (rsi < 30 && stochastic < 20 && momentum > 0) {
            return "BUY"; // Oversold and momentum rising
        } else if (rsi > 70 && stochastic > 80 && momentum < 0) {
            return "SELL"; // Overbought and momentum dropping
        }
        return "HOLD"; // No strong signal
    }

    private void sendMessage(String receiver, String content) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
        msg.setContent(content);
        send(msg);
    }
}
