package features;

import jade.core.Agent;

import java.util.ArrayList;


/**
 * Klasa rejestruj¹ca logi.
 * 
 * @author Mateusz Kaflowski <mkaflowski@gmail.com>
 */
public class Log {

	/**Kolekcja wszystkich zebranych logów.*/
	public static ArrayList<String> logs = new ArrayList<String>();

	/**
	 * Dodaje loga do kolekcji i wypisuje go w konsoli
	 * @param agent agent wysy³aj¹cy loga
	 * @param string treœæ loga
	 */
	public static void logp(Agent agent, String string) {
		android.util.Log.i("expert", "@" + agent.getAID().getLocalName() + ": " + string);
		logs.add("@" + agent.getAID().getLocalName() + ": " + string);
	}

	/**
	 * Dodaje loga do kolekcji i wypisuje go w konsoli
	 * @param string treœæ loga
	 */
	public static void logp(String string) {
		android.util.Log.i("expert", string);
		logs.add(string);
	}

}
