import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class Main {
    public static void main(String[] args) {
        // Get JADE runtime instance
        Runtime rt = Runtime.instance();

        // Create a container for agents
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true"); // Enable GUI

        ContainerController mainContainer = rt.createMainContainer(profile);

        try {
            // Start TradingAgent
            AgentController tradingAgent = mainContainer.createNewAgent("Trader1", TradingAgent.class.getName(), null);
            tradingAgent.start();

            // Start MarketDataAgent
            AgentController marketDataAgent = mainContainer.createNewAgent("MarketDataAgent", MarketDataAgent.class.getName(), null);
            marketDataAgent.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
