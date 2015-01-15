package com.leitat.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.leitat.R;
import com.leitat.servicio.Constantes;
import com.leitat.servicio.Servicio;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ActivityListaDispositivos extends Activity {
	//private BluetoothAdapter adaptadorBTLE;
	private transient TextView listaVacia;
	public static final String TAG = "ActivityListaDispositivos";
	private transient Servicio servicio = null;
	private transient List<BluetoothDevice> listaDispositivos;
	private transient AdaptadorDeviceAdapter adaptadorMovil;
	private final transient ServiceConnection conexionServicio = new ServiceConnection() {
		@Override
		public void onServiceConnected(final ComponentName className,
				final IBinder rawBinder) {
			Log.d(TAG, "onServiceConnected->servicioBTLE");
			servicio = ((Servicio.LocalBinder) rawBinder)
					.getServicio();
			if (servicio != null) {
				servicio.setHandlerActivityListaDispositivos(mHandler);
				cancelButton.setActivated(true);
				rellenarListaDispositivos();
			}			
		}

		@Override
		public void onServiceDisconnected(final ComponentName classname) {
			Log.d(TAG, "onServiceDisconnected->servicioBTLE");
			//com.leitat.servicio = null;
		}
	};

	
	private transient Map<String, Integer> rssiDispositivos;
	
	@SuppressLint("HandlerLeak")
	private transient final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
			case Constantes.ID_CONECTADO:
				Log.d(TAG,"handleMessage case STATE_CONNECTED");
				servicio.setHandlerActivityListaDispositivos(null);
				ActivityListaDispositivos.this.finish();
				break;
			case Constantes.ID_CONECTANDO:
				Log.d(TAG,"handleMessage case STATE_CONNECTING");
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
						WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
				break;
			case Constantes.ID_DESCONECTANDO:
				Log.d(TAG,"handleMessage case STATE_DISCONNECTING");
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
				break;
			case Constantes.ID_DESCONECTADO:
				Log.d(TAG,"handleMessage case STATE_DISCONNECTED");
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
				break;
			case Constantes.ID_ENCONTRADO:
				Log.d(TAG,"handleMessage case ID_ENCONTRADO");
				final Bundle data = msg.getData();
				final BluetoothDevice device = data
						.getParcelable(BluetoothDevice.EXTRA_DEVICE);
				final int rssi = data.getInt(Constantes.EXTRA_RSSI);
				//agregarDispositivoAlista(device, rssi);
				
				runOnUiThread(new Runnable() {//NOPMD
					
					@Override
					public void run() {
						agregarDispositivoAlista(device, rssi);
					}
				});
				break;	
			case 100:
				Log.d(TAG,"handleMessage case 100,cerrar");
				
				servicio.setHandlerActivityListaDispositivos(null);
				ActivityListaDispositivos.this.finish();
				//if(receptorIntents!=null) unregisterReceiver(receptorIntents)				
				break;
			default:
				break;
			}
		}
	};
	
	private transient Button cancelButton;
	
	@Override
	protected void onCreate(final Bundle savedInstanState) {
		super.onCreate(savedInstanState);
		
		Log.d(TAG, "onCreate");
		//adaptadorBTLE = BluetoothAdapter.getDefaultAdapter();
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.barra_encabezado);
		setContentView(R.layout.lista_dispositivos);
		 cancelButton = (Button) findViewById(R.id.btn_cancelar);
		//adaptadorBTLE = BluetoothAdapter.getDefaultAdapter();
		listaVacia = (TextView) findViewById(R.id.escaneando);
		
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View view) {
				Log.d(TAG, "onClickCancel");
				if(servicio!=null){
					servicio.buscar(false);
				finish();
				}
			}
		});
		cancelButton.setActivated(false);
	}

	private void rellenarListaDispositivos() {
		/* Initialize device list container */
		Log.d(TAG, "rellenarListaDispositivos");
		listaDispositivos = new ArrayList<BluetoothDevice>();
		adaptadorMovil = new AdaptadorDeviceAdapter(this,rssiDispositivos,listaDispositivos);
		rssiDispositivos = new HashMap<String, Integer>();

		final ListView nuevosDispView = (ListView) findViewById(R.id.nuevos_dispositivos);
		nuevosDispView.setAdapter(adaptadorMovil);
		nuevosDispView.setOnItemClickListener(devListListener);

		final Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
		for (BluetoothDevice pairedDevice : pairedDevices) {
			if(pairedDevice.getType()==BluetoothDevice.DEVICE_TYPE_LE){
				Log.d(TAG, "dispositivoApareado:" + pairedDevice.getName()+ " , es BTLE.");
			agregarDispositivoAlista(pairedDevice, 0);
			}else{
				Log.d(TAG, "dispositivoApareado:" + pairedDevice.getName()+ " , no es BTLE.");
			}
		}
		//Constantes.buscar(true);
	}

	private void agregarDispositivoAlista(final BluetoothDevice device, final int rssi) {
		boolean deviceFound;
        if(listaDispositivos==null){
        	Log.e(TAG,"agregarDispositivoAlista->listaDispositivos es null");
        }
        else if(device==null){
        	Log.e(TAG,"agregarDispositivoAlista->device es null");
        } else {
        	    deviceFound=false;
        		for (BluetoothDevice listDev : listaDispositivos) {
        		if (listDev.getAddress().equals(device.getAddress())) {
        			deviceFound = true;
        			break;
        			}
        		}
        		rssiDispositivos.put(device.getAddress(), rssi);
        		if (!deviceFound) {
        			listaVacia.setVisibility(View.GONE);
        			listaDispositivos.add(device);
        			adaptadorMovil.notifyDataSetChanged();
        		}
        }		
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart - com.leitat.servicio= "+ this.getTaskId());

		//IntentFilter filter = new IntentFilter(
		//		BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		//filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		//this.registerReceiver(receptorIntents, filter);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume - mi id " + this.getTaskId());
		if(servicio==null){
			// start service, if not already running (but it is)
			startService(new Intent(this, Constantes.class));
			final Intent bindIntent = new Intent(this, Constantes.class);
			bindService(bindIntent, conexionServicio, Context.BIND_AUTO_CREATE);
		}
	}
	
	

	@Override
	public void onStop() {
		super.onStop();
		Log.d(TAG,"OnStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG,"OnDestroy");
		if(servicio!=null && conexionServicio != null) {
			servicio.setHandlerActivityListaDispositivos(null);
			servicio.buscar(false);
			unbindService(conexionServicio);			
		}	
	}

	private transient final OnItemClickListener devListListener = new OnItemClickListener() {
		@Override
		public void onItemClick(final AdapterView<?> parent, final View view, final int position,
				final long idbotton) {
			Log.d(TAG, "devListListener");
			Log.d(TAG, "onItemClick - no Conectado al dispositivo");
			servicio.conectar(BluetoothAdapter
					.getDefaultAdapter().getRemoteDevice(
							listaDispositivos.get(position).getAddress()));
			finish();
		}
	};

	/*
	/**
	 * The BroadcastReceiver that listens for discovered devices and changes the
	 * title when discovery is finished.
	 
	private final BroadcastReceiver receptorIntents = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				setProgressBarIndeterminateVisibility(false);
				setTitle(R.string.seleccionar_dispositivo);
				if (listaDispositivos.size() == 0) {
					listaVacia.setText(R.string.no_ble_devices);
				}
			}
			if (BluetoothAdapter.ACTION_STATE_CHANGED
					.equals(intent.getAction())) {
				if (!BluetoothAdapter.getDefaultAdapter().isEnabled())
					finish();
			}
		}
	};
	*/
}
