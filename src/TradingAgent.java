import jade.core.Agent;

public class TradingAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " is starting...");
    }

    @Override
    protected void takeDown() {
        System.out.println(getLocalName() + " is shutting down...");
    }
}
