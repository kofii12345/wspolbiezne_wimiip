package agent.behaviours;

import communication.ExpertMessage;

import agent.ExpertAgent;
import android.content.Context;
import android.widget.Toast;
import structures.ExpertStatus;
import jade.core.behaviours.OneShotBehaviour;

public class ChangeStatusBehaviour extends OneShotBehaviour {
	
	ExpertStatus status;
	
	public ChangeStatusBehaviour(ExpertStatus status) {
		this.status = status;
	}

	@Override
	public void action() {
		ExpertAgent agent = (ExpertAgent) getAgent();
		agent.getExpertData().setStatus(status);
		agent.sendMessage(new ExpertMessage(agent.getAID(), agent.getExpertData()));
	}

}
