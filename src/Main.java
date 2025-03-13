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
        profile.setParameter(Profile.GUI, "true"); // Enable GUI (optional)

        ContainerController mainContainer = rt.createMainContainer(profile);

        try {
            // Start TradingAgent
            AgentController tradingAgent = mainContainer.createNewAgent("Trader1", TradingAgent.class.getName(), null);
            tradingAgent.start();
            System.out.println("Trader1 agent started.");

            // Start MarketDataAgent
            AgentController marketDataAgent = mainContainer.createNewAgent("MarketDataAgent", MarketDataAgent.class.getName(), null);
            marketDataAgent.start();
            System.out.println("MarketDataAgent started.");

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Optional: Shutdown container after a certain condition or timeout
        // mainContainer.shutdown();
    }
}
