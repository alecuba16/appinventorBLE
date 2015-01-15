package com.leitat.gui;

import java.util.List;
import java.util.Map;
import com.leitat.R;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class AdaptadorDeviceAdapter extends BaseAdapter {
	private final transient List<BluetoothDevice> devices;
	private final transient LayoutInflater inflater;
	private final transient Map<String, Integer> rssiDispositivos;

	public AdaptadorDeviceAdapter(final Context context,Map<String, Integer> rssiDispositivos,final List<BluetoothDevice> devices) {
		super();
		inflater = LayoutInflater.from(context);
		this.devices = devices;
		this.rssiDispositivos=rssiDispositivos;
	}

	@Override
	public int getCount() {
		return devices.size();
	}

	@Override
	public Object getItem(int position) {
		return devices.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewGroup vg;

		if (convertView != null) {
			vg = (ViewGroup) convertView;
		} else {
			vg = (ViewGroup) inflater
					.inflate(R.layout.un_dispositivo, null);
		}

		BluetoothDevice device = devices.get(position);
		final TextView tvadd = ((TextView) vg.findViewById(R.id.direccion));
		final TextView tvname = ((TextView) vg.findViewById(R.id.nombre));
		final TextView tvpaired = (TextView) vg.findViewById(R.id.apareado);
		final TextView tvrssi = (TextView) vg.findViewById(R.id.rssi);

		tvrssi.setVisibility(View.VISIBLE);
		byte rssival = (byte) rssiDispositivos.get(
				device.getAddress()).intValue();
		if (rssival != 0) {
			tvrssi.setText("Rssi = " + String.valueOf(rssival));
		}

		tvname.setText(device.getName());
		tvadd.setText(device.getAddress());
		if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
			tvname.setTextColor(Color.GRAY);
			tvadd.setTextColor(Color.GRAY);
			tvpaired.setTextColor(Color.GRAY);
			tvpaired.setVisibility(View.VISIBLE);
			tvpaired.setText(R.string.apareado);
			tvrssi.setVisibility(View.GONE);
		} else {
			tvname.setTextColor(Color.WHITE);
			tvadd.setTextColor(Color.WHITE);
			tvpaired.setVisibility(View.GONE);
			tvrssi.setVisibility(View.VISIBLE);
			tvrssi.setTextColor(Color.WHITE);
		}
		return vg;
	}
}
