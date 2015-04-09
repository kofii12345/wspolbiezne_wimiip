package communication;

import jade.core.AID;
import jade.util.leap.Serializable;

import java.util.ArrayList;

import structures.ExpertData;

public class ExpertMessage extends Message implements Serializable {

	protected ExpertData expert;

	public ExpertMessage(AID aid, ExpertData expert) {
		super(aid);
		this.expert = expert;
		type = MessageType.EXPERT_MESSAGE;
	}

	public void setExpert(ExpertData expert) {
		this.expert = expert;
	}

	public ExpertData getExpert() {
		return expert;
	}

}
