package gui;

import jade.util.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;

import structures.ExpertData;
import agent.AgentManager;
import agent.ExpertAgent;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mechanic.androidexpert.R;
import communication.Message;
import communication.Order;
import communication.OrderStatus;

import features.Log;

/**
 * AKtywnoœæ wyœwietlaj¹ca zlecenia
 * 
 * @author Mateusz Kaflowski
 */

public class OrdeListActivity extends Activity {
	private Logger logger = Logger.getJADELogger(this.getClass().getName());

	static final int PARTICIPANTS_REQUEST = 0;

	ArrayList<Order> orderList;
	public ExpertAgent expertAgent;
	public static ListView listView;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		// je¿eli agent siê nie utworzy³, aktywnoœc jest zamykana
		expertAgent = AgentManager.getAgent();
		if (expertAgent != null)
			expertAgent.setActivity(this);
		else {
			finish();
		}

		setContentView(R.layout.order_view);

		listView = (ListView) findViewById(R.id.order_view_order_list);
		orderList = new ArrayList<Order>();

		loadOrdersFromMemory();

		// rejestracja:
		try {
			expertAgent.setExpert(new ExpertData(expertAgent.getAID()
					.getLocalName(), "maths", expertAgent.getAID()));
			expertAgent.sendRegistrationMessage();
		} catch (Exception e) {
			Toast.makeText(this,
					"Registration in system problem! Contact admin.",
					Toast.LENGTH_LONG).show();
			finish();
		}

		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		if (AgentManager.agent == null)
			finish();

		listView.setAdapter(new OrderAdapter(this, orderList));
		listView.invalidate();
		// listView.setOnItemClickListener(this);
		super.onResume();
	}

	public void addOrder(final Message msgMessage) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Order order = (Order) msgMessage;
				orderList.add(order);
				Collections.sort(orderList);

				listView.setAdapter(new OrderAdapter(OrdeListActivity.this,
						orderList));
				listView.invalidate();
				Log.logp("order added to listview");
			}
		});

	}

	public void loadOrdersFromMemory() {
		ArrayList<Order> orders = null;
		try {
			FileInputStream fis = this.openFileInput("ORDERS");
			ObjectInputStream in = new ObjectInputStream(fis);

			orders = (ArrayList<Order>) in.readObject();
			in.close();
		} catch (IOException e) {
			return;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}

		if (orders == null)
			return;

		for (Order order : orders) {
			orderList.add(order);
			listView.invalidate();
		}
		
		Toast.makeText(this, "loading orders", Toast.LENGTH_SHORT).show();
	}

	public void saveOrdersOnMemory() {
		if (orderList == null)
			return;

		try {
			FileOutputStream fos = openFileOutput("ORDERS",
					Context.MODE_PRIVATE);

			ObjectOutputStream out = new ObjectOutputStream(fos);

			out.writeObject(orderList);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Toast.makeText(this, "saving orders", Toast.LENGTH_SHORT).show();
	}
	
	public void sortByStatus(){
		Collections.sort(orderList);

		listView.setAdapter(new OrderAdapter(OrdeListActivity.this,
				orderList));
		listView.invalidate();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.chat_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_clear) {
			orderList.clear();
			listView.setAdapter(new OrderAdapter(OrdeListActivity.this,
					orderList));
			listView.invalidate();
			saveOrdersOnMemory();
			
		}
		
		if(item.getItemId() == R.id.menu_sort_by_status){
			Collections.sort(orderList);

			listView.setAdapter(new OrderAdapter(OrdeListActivity.this,
					orderList));
			listView.invalidate();
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		saveOrdersOnMemory();
	}

}