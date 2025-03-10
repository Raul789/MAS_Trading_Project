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
            // Start TradingAgent with the name "Trader1"
            AgentController agent = mainContainer.createNewAgent("Trader1", TradingAgent.class.getName(), null);
            agent.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
