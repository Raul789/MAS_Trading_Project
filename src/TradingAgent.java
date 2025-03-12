import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class TradingAgent extends Agent {
    private double balance = 1000.0;
    private boolean holdingPosition = false;

    protected void setup() {
        System.out.println(getLocalName() + " is ready to trade.");

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String content = msg.getContent().trim();
                    System.out.println("DEBUG: Raw received message -> " + content); // ✅ Print raw message

                    try {
                        // Replace commas in decimal values with dots
                        content = content.replaceAll(",", "."); // Correct comma issue in decimal values

                        // Split by space or semicolon (adjust for proper separation)
                        String[] parts = content.split("[,;]"); // Now use both comma and semicolon as separators

                        // Ensure the message is correctly formatted with 3 parts (price, sma, rsi)
                        if (parts.length == 3) {
                            double price = Double.parseDouble(parts[0].trim());
                            double sma = Double.parseDouble(parts[1].trim());
                            double rsi = Double.parseDouble(parts[2].trim());

                            System.out.println("DEBUG: Extracted values -> Price: " + price + ", SMA: " + sma + ", RSI: " + rsi); // ✅ Debug extracted values

                            // Make trading decision
                            makeTradingDecision(price, sma, rsi);
                        } else {
                            System.out.println("Error: Received malformed message - " + content);
                        }
                    } catch (Exception e) {
                        System.out.println("Error processing the message content: " + e.getMessage());
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void makeTradingDecision(double price, double sma, double rsi) {
        System.out.println(getLocalName() + " - Price: " + price + ", SMA: " + sma + ", RSI: " + rsi);

        if (price < sma && rsi < 30 && !holdingPosition) {
            holdingPosition = true;
            balance -= price;
            System.out.println(getLocalName() + " - BUY at " + price);
        } else if (price > sma && rsi > 70 && holdingPosition) {
            holdingPosition = false;
            balance += price;
            System.out.println(getLocalName() + " - SELL at " + price);
        }

        System.out.println(getLocalName() + " - Balance: " + balance);
    }
}
