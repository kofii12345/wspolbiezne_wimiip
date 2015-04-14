package manager;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FilenameUtils;

import structures.ExpertData;
import structures.ExpertStatus;
import structures.Location;

import communication.ExpertMessage;
import communication.Message;
import communication.MessageType;
import communication.Order;
face
public class OrderManagerAgent extends Agent {

	private static final long serialVersionUID = 1L;
	private static final long REFRESH_TIME = 1 * 5 * 1000;
	ArrayList<Message> unhandledOrders;
	ArrayList<ExpertData> experts;
	final int SYNCH_MESSAGE_NUM = 20;

	// osobna mapa bo zawartosc eksperta moze sie zmienic (np. kategoria)
	HashMap<AID, Location> expertsLocations;

	@Override
	protected void setup() {
		super.setup();

		System.out.println("Setting up OrderManagerAgent");

		unhandledOrders = new ArrayList<Message>();
		experts = new ArrayList<>();
		expertsLocations = new HashMap<>();

		unhandledOrders = new ArrayList<>();
		
		registerInYP();

		// odbieranie wiadomoœci:
		addBehaviour(new CyclicBehaviour() {

			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					try {
						Message msgMessage = (Message) msg.getContentObject();
						System.out.println("Message received!");

						// wiadomoœc ze zg³oszeniem:
						if (msgMessage.getType() == MessageType.ORDER) {
							System.out.println("ORDER MESSAGE");
							String category = findCategory(msgMessage);
							ExpertData expert = findExpert(category, msgMessage);
							if (expert != null)
								sendMessage(msgMessage, expert.getAid());
							else
								unhandledOrders.add(msgMessage);
							return;
						}

						// wiadomoœc z lokacj¹ serwisanata:
						if (msgMessage.getType() == MessageType.LOCATION) {
							System.out.println("LOCATION MESSAGE");
							updateExpertPosition(msgMessage);
							return;
						}

						// wiadomoœc rejestracyjna eksperta:
						if (msgMessage.getType() == MessageType.REGISTRATION) {
							ExpertMessage em = (ExpertMessage) msgMessage;
							System.out.println("REGISTRATION MESSAGE");
							
							updateExpertPosition(msgMessage);
							addExpert(em.getExpert());
							
							// TODO do usuniêcia
							// wys³anie wiadomoœci testowej - DO USUNIÊCIA!
							addBehaviour(new OneShotBehaviour() {

								@Override
								public void action() {
									
									Order m = new Order(getAID(), "Problem 2",
											"I have a problem with integral and area");
									m.setLocation(new Location(50.0784863, 19.8940399));
									sendMessage(m, getAID());
								}
							});
							//--
							
							return;
						}
						// wiadomoœc derejestracyjna:
						if (msgMessage.getType() == MessageType.DEREGISTRATION) {
							System.out.println("DEREGISTRATION MESSAGE");
							
							ExpertMessage em = (ExpertMessage) msgMessage;
							removeExpert(em.getExpert());
							return;
						}

						//
						// // normalna wiadomoœæ:
						// System.out.println("NORMAL MESSAGE");
						// messages.add(msgMessage);
						// broadcastMessage(msgMessage);

					} catch (UnreadableException e) {
						e.printStackTrace();
					} catch (FileNotFoundException e) {
						System.out.println("Reading file error.");
						e.printStackTrace();
					}

				} else
					block();
			}

		});

		// obs³uga nieobs³u¿onych zleceñ - ponowne szukanie specjalistow:
		addBehaviour(new TickerBehaviour(this, REFRESH_TIME) {

			@Override
			protected void onTick() {
				System.out.println("----------------");
				System.out.println("LISTA EKSPERTÓW:");
				for (ExpertData expert : experts) {
					System.out.println(expert.getName() + " - "
							+ expert.getCategory());
				}
				System.out.println("----------------");

				if (unhandledOrders.isEmpty()) {
					System.out.println("BRAK NIEOBSLUZONYCH ZLECEN");
					return;
				}

				System.out
						.println("PONOWNE SZUKANIE DLA NIEOBSLUZONYCH ZLECEN");

				ArrayList<Message> toDelete = new ArrayList<>();
				for (Message order : unhandledOrders) {

					String category = null;
					try {
						category = findCategory(order);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ExpertData expert = findExpert(category, order);
					if (expert != null) {
						sendMessage(order, expert.getAid());
						toDelete.add(order);
					}
				}

				for (Message message : toDelete) {
					unhandledOrders.remove(message);
				}
			}
		});
	}

	protected void removeExpert(ExpertData expert) {
		for (ExpertData ex : experts) {
			if (ex.getName().equals(expert.getName())){
				experts.remove(ex);
				return;
			}
		}
	}

	protected void addExpert(ExpertData expert) {
		for (ExpertData ex : experts) {
			if (ex.getName().equals(expert.getName()))
				return;
		}
		experts.add(expert);
	}

	protected ExpertData findExpert(String category, Message msgMessage) {
		// szuka dostêpnych
		ArrayList<ExpertData> expertsFound = new ArrayList<>();

		for (ExpertData expert : experts) {
			if (expert.getStatus() == ExpertStatus.AVAILABLE
					&& expert.getCategory().equals(category))
				expertsFound.add(expert);
		}

		// jezeli nie ma dostêpnych to szuka na przerwie
		if (experts.isEmpty())
			for (ExpertData expert : experts) {
				if (expert.getStatus() == ExpertStatus.BREAK
						&& expert.getCategory().equals(category))
					expertsFound.add(expert);
			}

		// jezeli nie ma dostêpnych to szuka zajêtych
		if (experts.isEmpty())
			for (ExpertData expert : experts) {
				if (expert.getStatus() == ExpertStatus.IN_PROGRESS
						&& expert.getCategory().equals(category))
					expertsFound.add(expert);
			}

		// jezeli nie ma nikogo zwraca null i dodaje wiadomoœc do kolejki do
		// pozniejszego sprawdzenia:
		if (experts.isEmpty()) {
			System.out.println("Brak dostêpnych ekspertow na liœcie.");
			return null;
		}

		// szuka najblizszego - liczy odleg³oœci euklidesowe
		double winningDistance = Double.MAX_VALUE;
		ExpertData winningExpert = null;

		for (ExpertData expert : expertsFound) {
			double distance = MathOperations.calcDistance(msgMessage
					.getLocation().getLatitude(), msgMessage.getLocation()
					.getLongitude(), expert.getLocation().getLatitude(), expert
					.getLocation().getLongitude());
			if (distance < winningDistance) {
				winningDistance = distance;
				winningExpert = expert;
			}
		}

		System.out.println("Wybrany ekspert - " + winningExpert.getName());
		return winningExpert;

	}

	protected void updateExpertPosition(Message msgMessage) {
		expertsLocations.put(msgMessage.getAid(), msgMessage.getLocation());
	}

	protected String findCategory(Message msgMessage)
			throws FileNotFoundException {

		Order o = (Order) msgMessage;

		File folder = new File("categories");
		File[] listOfFiles = folder.listFiles();

		String actualCategory, winningCategory = null;
		int actualPoints = 0, winningPoints = 0;

		String text = o.getDescription();
		if (text == null)
			return "none";
		String[] words = text.split("\\s+");
		List<String> wordsList = Arrays.asList(words);

		for (File file : listOfFiles) {

			Scanner scanner = new Scanner(file);

			actualCategory = FilenameUtils.removeExtension(file.getName());
			actualPoints = 0;

			while (scanner.hasNext()) {
				int wage = scanner.nextInt();
				String word = scanner.next();

				for (String string : wordsList) {
					if (string.equals(word)) {

						switch (wage) {
						case 1:
							actualPoints += 1;
							break;
						case 2:
							actualPoints += 2;
							break;
						case 3:
							return actualCategory;
						}

						continue;

					}
				}
			}

			if (actualPoints > winningPoints) {
				winningCategory = actualCategory;
				winningPoints = actualPoints;
			}

			scanner.close();
		}

		System.out.println("Category - " + winningCategory);

		return winningCategory;

	}

	private void sendMessage(Message message, AID user) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

		System.out.println(user.getLocalName());

		msg.addReceiver(user);
		try {
			msg.setContentObject(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
		send(msg);
	}

	// private void registerExpert(Message msgMessage) {
	// experts.add(msgMessage.getAid());
	// }
	//
	// private void deRegisterExpert(Message msgMessage) {
	// experts.remove(msgMessage.getAid());
	// }

	private void registerInYP() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName("Manager");
		sd.setType("Manager");

		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
