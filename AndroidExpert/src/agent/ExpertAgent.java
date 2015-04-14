package agent;

import features.Log;
import gui.OrdeListActivity;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.util.leap.Iterator;
import jade.util.leap.Set;
import jade.util.leap.SortedSetImpl;

import java.io.IOException;
import java.util.ArrayList;

import structures.ExpertData;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import communication.ExpertMessage;
import communication.Message;
import communication.MessageType;
import communication.UserListMessage;

/**
 * Agent dla wersji Android
 * 
 * @author Mateusz Kaflowski
 * 
 */
public class ExpertAgent extends Agent {
	private static final long serialVersionUID = 1594371294421614291L;

	/** Zbiór uczestnikow chatu */
	private Set participants = new SortedSetImpl();
	/** Context */
	private Context context;
	/** Aktywnoœæ chatu */
	OrdeListActivity activity;

	private ExpertData expert;

	/**
	 * Inicjalizacja agenta
	 */
	protected void setup() {

		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			if (args[0] instanceof Context) {
				context = (Context) args[0];
			}
		}

		AgentManager.setAgent(this);

		Intent broadcast = new Intent();
		broadcast.setAction("jade.demo.chat.SHOW_CHAT");
		context.sendBroadcast(broadcast);

		// odbieranie wiadomoœci:
		addBehaviour(new CyclicBehaviour() {

			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {

					try {
						Log.logp(myAgent, "message received");
						Message msgMessage = (Message) msg.getContentObject();

						if (msgMessage.getType() == MessageType.ORDER) {
							Log.logp(myAgent, "type = ORDER");
							activity.addOrder(msgMessage);
						}
						
					} catch (UnreadableException e) {
						Toast.makeText(
								activity,
								"Oops! Soemthing goes wrong during receiving message.",
								Toast.LENGTH_LONG).show();
						e.printStackTrace();
					}

				} else
					block();
			}
		});

	}

	@Override
	protected void takeDown() {
		sendDeregistrationMessage();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		AgentManager.agent = null;
		if (activity != null)
			activity.finish();
		super.takeDown();
	}


	/**
	 * Wpisanie siê do czatu.
	 */
	public void sendRegistrationMessage() {
		Log.logp(this, "1registration message sent");
		addBehaviour(new OneShotBehaviour(this) {

			@Override
			public void action() {
				ExpertMessage expertMessage = new ExpertMessage(
						myAgent.getAID(), expert);
				expertMessage.setAid(myAgent.getAID());
				expertMessage.setType(MessageType.REGISTRATION);

				// wyszukanie managera:
				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Manager");

				dfd.addServices(sd);

				try {
					DFAgentDescription results[] = DFService.search(myAgent,
							dfd);

					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					if (results.length != 0) {
						Log.logp(myAgent, Integer.toString(results.length));
						msg.addReceiver(results[0].getName());
						msg.setContentObject(expertMessage);
						send(msg);
						Log.logp(myAgent, "registration message sent");
					} else {
						Log.logp(myAgent, "there is no chat manager");
						myAgent.doDelete();
						return;
					}

				} catch (FIPAException e) {
					Log.logp(myAgent, "FIPAException - due to searching");
					return;
				} catch (IOException e) {
					Log.logp(myAgent, "adding content object error");
					return;
				}

			}
		});
	}

	/**
	 * Wypisanie siê z czatu.
	 */
	public void sendDeregistrationMessage() {

		// ustawienie nadawcy:
		ExpertMessage message = new ExpertMessage(this.getAID(), expert);
		message.setAid(this.getAID());
		message.setType(MessageType.DEREGISTRATION);

		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Manager");

		dfd.addServices(sd);

		try {
			DFAgentDescription results[] = DFService.search(this, dfd);

			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			if (results.length != 0) {
				msg.addReceiver(results[0].getName());
				msg.setContentObject(message);
				send(msg);
			} else {
				Log.logp(this, "there is no chat manager");
				return;
			}

		} catch (FIPAException e) {
			Log.logp(this, "FIPAException - due to searching");
			return;
		} catch (IOException e) {
			Log.logp(this, "sending serialized data message");
			return;
		}

		Log.logp(this, "deregistration message sent");

	}

	/**
	 * Wys³anie wiadomoœci
	 * 
	 * @param message
	 *            wiadomoœc
	 */
	public void sendMessage(final Message message) {
		addBehaviour(new OneShotBehaviour(this) {

			@Override
			public void action() {
				// ustawienie nadawcy:
				message.setAid(myAgent.getAID());

				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Manager");

				dfd.addServices(sd);

				try {
					DFAgentDescription results[] = DFService.search(myAgent,
							dfd);

					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					if (results.length != 0) {
						msg.addReceiver(results[0].getName());
						msg.setContentObject(message);
						send(msg);
					} else {
						Log.logp(myAgent, "there is no chat manager");
						return;
					}

				} catch (FIPAException e) {
					Log.logp(myAgent, "FIPAException - due to searching");
					return;
				} catch (IOException e) {
					Log.logp(myAgent, "sending serialized data message");
					return;
				}

				Log.logp(myAgent, "message sent");

			}
		});
	}

	/**
	 * 
	 * @param activity
	 *            aktywnoœæ chatu
	 */
	public void setActivity(OrdeListActivity activity) {
		this.activity = activity;
	}

	public void setExpert(ExpertData expert) {
		this.expert = expert;
	}

	public ExpertData getExpertData() {
		return expert;
	}

}
