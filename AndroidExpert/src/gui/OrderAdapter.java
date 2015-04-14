package gui;

import java.util.ArrayList;
import java.util.Locale;

import structures.ExpertStatus;

import agent.AgentManager;
import agent.behaviours.ChangeStatusBehaviour;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.mechanic.androidexpert.R;
import communication.Order;
import communication.OrderStatus;

public class OrderAdapter extends BaseAdapter {

	Context context;
	ArrayList<Order> orders;

	public OrderAdapter(Context context, ArrayList<Order> orders) {
		this.context = context;
		this.orders = orders;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final Order order = orders.get(position);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view;

		if (convertView == null) {
			view = inflater.inflate(R.layout.order_element, null);
		} else
			view = convertView;

		
		if(order.getStatus()==OrderStatus.IN_PROGRESS)
			view.setBackgroundDrawable(new ColorDrawable(0xffffd479));
		
		if(order.getStatus()==OrderStatus.NEW)
			view.setBackgroundDrawable(new ColorDrawable(0xff2ecc71));
		
		TextView tvTitle = (TextView) view
				.findViewById(R.id.order_element_title);
		tvTitle.setText(position+orders.get(position).getTitle());

		TextView tvPath = (TextView) view
				.findViewById(R.id.order_element_desc);
		tvPath.setText(orders.get(position).getDescription());

		// przycisk:
		final ImageView iv = (ImageView) view
				.findViewById(R.id.order_element_button);
		iv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				PopupMenu popup = new PopupMenu(context, iv);
				// Inflating the Popup using xml file
				popup.getMenuInflater().inflate(R.menu.order_popup_menu,
						popup.getMenu());

				// registering popup with OnMenuItemClickListener
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						switch (item.getItemId()) {

						case R.id.pop_up_order_map:
							Toast.makeText(context, "Opening order location.",
									Toast.LENGTH_SHORT).show();
							String uri = String.format(Locale.ENGLISH,
									"geo:%f,%f", order.getLocation()
											.getLatitude(), order.getLocation()
											.getLongitude());
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri
									.parse(uri));
							context.startActivity(intent);
							return true;

						case R.id.pop_up_order_navigate:
							Toast.makeText(context, "Opening navigation app.",
									Toast.LENGTH_SHORT).show();

							String uri2 = "http://maps.google.com/maps?"
									+ "daddr="
									+ order.getLocation().getLatitude() + ","
									+ order.getLocation().getLongitude();
							Intent intent2 = new Intent(
									android.content.Intent.ACTION_VIEW, Uri
											.parse(uri2));
							intent2.setClassName(
									"com.google.android.apps.maps",
									"com.google.android.maps.MapsActivity");
							context.startActivity(intent2);
							return true;

						case R.id.pop_up_order_take:
							Toast.makeText(context, "Order taken.",
									Toast.LENGTH_SHORT).show();
							
							order.setStatus(OrderStatus.IN_PROGRESS);
							AgentManager.agent.addBehaviour(new ChangeStatusBehaviour(ExpertStatus.IN_PROGRESS));
							notifyDataSetChanged();

							return true;
						default:
							return false;
						}
					}
				});

				popup.show();// showing popup menu

			}
		});

		return view;
	}

	@Override
	public int getCount() {
		if (orders == null)
			return 0;
		return orders.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

}
