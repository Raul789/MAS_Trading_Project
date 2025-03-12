import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import org.json.JSONObject;

public class TradingAgent extends Agent {
    private double sma;
    private double rsi;
    private double lastPrice;

    protected void setup() {
        System.out.println(getLocalName() + " is ready to trade...");

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    try {
                        String content = msg.getContent().trim();
                        // Expecting message in the format: price,sma,rsi
                        String[] data = content.split(",");
                        lastPrice = Double.parseDouble(data[0]);
                        sma = Double.parseDouble(data[1]);
                        rsi = Double.parseDouble(data[2]);

                        // Take action based on SMA and RSI values
                        decideAction();
                    } catch (Exception e) {
                        System.out.println("Error processing data: " + e.getMessage());
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void decideAction() {
        if (rsi > 70) {
            // Overbought, SELL
            System.out.println("RSI is overbought. Signal: SELL");
            sendTradeSignal("SELL");
        } else if (rsi < 30) {
            // Oversold, BUY
            System.out.println("RSI is oversold. Signal: BUY");
            sendTradeSignal("BUY");
        } else if (lastPrice > sma) {
            // Price above SMA, potential BUY
            System.out.println("Price is above SMA. Signal: BUY");
            sendTradeSignal("BUY");
        } else if (lastPrice < sma) {
            // Price below SMA, potential SELL
            System.out.println("Price is below SMA. Signal: SELL");
            sendTradeSignal("SELL");
        } else {
            // No action, hold
            System.out.println("No clear signal. Hold.");
        }
    }

    private void sendTradeSignal(String signal) {
        JSONObject tradeSignal = new JSONObject();
        tradeSignal.put("action", signal);
        tradeSignal.put("price", lastPrice);
        tradeSignal.put("sma", sma);
        tradeSignal.put("rsi", rsi);

        sendMessage("TradingExecutor", tradeSignal.toString());
    }

    private void sendMessage(String receiver, String content) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
        msg.setContent(content);
        send(msg);
    }
}
