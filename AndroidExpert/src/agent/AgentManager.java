package agent;

import features.Log;
import structures.ExpertData;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * Klasa zarz¹dzaj¹ca g³ównym agentem - producenckim.
 * @author Mateusz Kaflowski <mkaflowski@gmail.com>
 */
public class AgentManager {

	/** Agent producencki*/
	public static ExpertAgent agent;

	public static void setAgent(ExpertAgent agent) {
		AgentManager.agent = agent;
	}
	
	public static void setExpert(ExpertData expert){
		agent.setExpert(expert);
	}

	public static ExpertAgent getAgent() {
		return agent;
	}

	/**Metoda koñcz¹ca byt agent producenckiego.*/
	public static void deleteAgent() {
		Log.logp(agent, "deleting myself");
		agent.doDelete();
	}


}
