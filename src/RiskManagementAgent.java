import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import org.json.JSONObject;

public class RiskManagementAgent extends Agent {
    private double riskPercentage = 2.0; // 2% of balance per trade
    private double stopLossMultiplier = 1.5; // Stop-loss is 1.5x ATR
    private double takeProfitMultiplier = 2.0; // Take-profit is 2x ATR

    protected void setup() {
        System.out.println(getLocalName() + " is managing risk...");

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    try {
                        String content = msg.getContent().trim();
                        JSONObject data = new JSONObject(content);
                        double balance = data.getDouble("balance");
                        double atr = data.getDouble("atr");

                        // Compute Stop-Loss and Take-Profit
                        double riskAmount = (riskPercentage / 100) * balance;
                        double stopLoss = stopLossMultiplier * atr;
                        double takeProfit = takeProfitMultiplier * atr;

                        JSONObject response = new JSONObject();
                        response.put("stopLoss", stopLoss);
                        response.put("takeProfit", takeProfit);

                        sendMessage("TradingAgent", response.toString());
                    } catch (Exception e) {
                        System.out.println("Error processing risk data: " + e.getMessage());
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void sendMessage(String receiver, String content) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
        msg.setContent(content);
        send(msg);
    }
}
