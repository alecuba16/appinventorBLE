package com.leitat.servicio;

import com.leitat.gui.ActivityListaDispositivos;
import com.leitat.sonido.EfectosSonidoSingleton;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class Servicio extends Service {

	private transient final String TAG = Servicio.class
			.getSimpleName();
	
	/** Adaptadores */
	private transient BluetoothGatt bluetoothGatt = null;
	private transient Handler handActPrincipal = null;
	private transient Handler handListaDisp = null;
	private transient BluetoothManager bluetoothManager;
	private transient BluetoothAdapter adaptBluetooth;


	/**
	 * Profile service connection listener
	 */
	public class LocalBinder extends Binder {
		public Servicio getServicio() {
			return Servicio.this;
		}
	}

	@Override
	public IBinder onBind(final Intent arg0) {
		return binder;
	}

	private transient final IBinder binder = new LocalBinder();

	public void setHandlerActivityPrincipal(final Handler mHandler) {
		Log.d(TAG, "setHandlerActivityPrincipal establecido");
		handActPrincipal = mHandler;
	}

	public void setHandlerActivityListaDispositivos(final Handler mHandler) {
		if (mHandler == null) {
			Log.d(TAG, "setHandlerListaDispositivos quitado.");
		} else {
			Log.d(TAG, "setHandlerListaDispositivos establecido.");
		}
		handListaDisp = mHandler;
	}

	private boolean cambiaEstadoNotificaciones(final boolean enable,
			final BluetoothGattCharacteristic characteristic) {
		boolean exito;
		final StringBuilder mensaje_debug = new StringBuilder();
		mensaje_debug.append("cambiaEstadoNotificaciones -");
		if (bluetoothGatt == null || !bluetoothGatt.setCharacteristicNotification(characteristic, enable) || characteristic.getDescriptor(Constantes.UUID_CONFIG_DESC) == null ) {
			exito=false;
		} else {
			final BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(Constantes.UUID_CONFIG_DESC);
			if (enable) {
						mensaje_debug.append(" activar");
						clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
						exito = bluetoothGatt.writeDescriptor(clientConfig);
		    	} else {
		    			mensaje_debug.append(" desactivar");
		    			clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
		    			handActPrincipal.sendMessage(handActPrincipal.obtainMessage(BluetoothProfile.STATE_DISCONNECTING));
		    			exito = bluetoothGatt.writeDescriptor(clientConfig);
		    			}
		}
		Log.d(TAG, mensaje_debug.toString());
		return exito;
	}

	public void habilitarNotificaciones(final BluetoothDevice device) {
		final StringBuilder mensaje_debug = new StringBuilder();
		mensaje_debug.append("habilitarNotificaciones -");
		boolean isError;

		if (adaptBluetooth == null || bluetoothGatt == null) {
			Log.e(TAG,
					"habilitarNotificaciones->El adatador de com.leitat.servicio no esta inicializado.");
			return;
		}

		final BluetoothGattService servicio = bluetoothGatt
				.getService(Constantes.UUID_SERVICIO);
		if (servicio == null) {
			mensaje_debug
					.append(" Servicio servicio_generic_uuid no encontrado!");
			isError = true;
		} else {
			if (servicio.getCharacteristics().size() < 1) {
				mensaje_debug
						.append(" No hay caracteristicas disponibles para servicio_generic!");
				isError = true;
			} else {
				final BluetoothGattCharacteristic caracteristica = servicio
						.getCharacteristic(Constantes.UUID_CARACT);
				if (caracteristica == null) {
					mensaje_debug
							.append(" caracteristica \"UUID_CARACT\" no encontrado!");
					isError = true;
				} else {
					bluetoothGatt.setCharacteristicNotification(caracteristica,
							true);
					if (Constantes.UUID_CARACT.equals(caracteristica.getUuid())) {
						final BluetoothGattDescriptor descriptor = caracteristica
								.getDescriptor(Constantes.UUID_CONFIG_DESC);
						descriptor
								.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
						bluetoothGatt.writeDescriptor(descriptor);
					}
					isError=false;
				}
			}
		}

		if (isError) {
			Log.e(TAG, mensaje_debug.toString());
            mensajeAprincipal(Constantes.ID_ERROR);
		} else {
			mensaje_debug.append(" Ejecutado correctamente");
			Log.d(TAG, mensaje_debug.toString());
		}

	}

	public void mensajeAprincipal(final int idMensaje){
		if(handActPrincipal!=null){
			
			Message.obtain(handActPrincipal,idMensaje).sendToTarget();
		}
	}
	
	public void mensajeAprincipal(final int idMensaje,final Bundle mBundle){
		if(handActPrincipal!=null){
			    final Message msg = Message.obtain(handActPrincipal, idMensaje);
				msg.setData(mBundle);
				msg.sendToTarget();
		}
	}
	
	public void mensajeAListaDispositivos(final int idMensaje){
		if(handListaDisp!=null){
			Message.obtain(handListaDisp,idMensaje).sendToTarget();
		}
	}
	
	public void mensajeAListaDispositivos(final int idMensaje,final Bundle mBundle){
		if(handListaDisp!=null){
			    final Message msg = Message.obtain(handListaDisp, idMensaje);
				msg.setData(mBundle);
				msg.sendToTarget();
		}
	}
	
	private final Runnable timeoutbuscar = new Runnable() {//NOPMD

		@Override
		public void run() {
			if (Servicio.this.adaptBluetooth.getState()==Constantes.ID_DESCONECTADO) {
				Log.d(TAG, "conectarAutomaticoCallback - dispositivoBLE null");
				if (handListaDisp == null) {//No se ha llamado
					final Intent intent = new Intent(Servicio.this,
							ActivityListaDispositivos.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					Servicio.this.startActivity(intent);
					startActivity(intent);
					Log.d(TAG,
							"conectarAutomaticoCallback  - lanzando activity");
				} else {
					Log.d(TAG,
							"conectarAutomatico -  handListaDisp no es null");
				}
			} else {
				Log.d(TAG, "conectarAutomatico -  dispositivoBLE no null");
			}
			timeoutBuscarSet = false;
			timeoutHandler.removeCallbacks(timeoutbuscar);
		}
	};

	private transient boolean timeoutBuscarSet = false;
	private transient final Handler timeoutHandler = new Handler();

	public void conectarAutomatico() {
		if (timeoutBuscarSet) {
			Log.d(TAG, "conectarautomatico-Timeout ya programado");
		} else {
			final int timeout = (3 * 1000);
			Log.d(TAG, "conectarAutomatico - programado timeout dentro de"
					+ timeout);
			timeoutHandler.postDelayed(timeoutbuscar, timeout);
			timeoutBuscarSet = true;
			buscar(true);
		}
	}
	
	private transient final MLeScanCallBack scanCallBack = new MLeScanCallBack(this);

	public void buscar(final boolean start) {
		Log.d(TAG, "Buscar->" + start);
		//Precondiciones Bluetooth este on y habilitado
		if (start) {
				if (!(adaptBluetooth.isDiscovering()) && !(adaptBluetooth.startLeScan(scanCallBack)) ){
				    	 Log.d(TAG, "Buscar-> adaptador no quiere buscar estado: "+ adaptBluetooth.getState()
									+ " isDiscovering:"+adaptBluetooth.isDiscovering()
									+ " isEnabled():"+adaptBluetooth.isEnabled()
									+ " adaptBluetooth.getProfileConnectionState(BluetoothProfile.GATT):"+adaptBluetooth.getProfileConnectionState(BluetoothProfile.GATT));
				    	 if (!(adaptBluetooth.getState()==Constantes.ID_CONECTADO)) {
								mensajeAprincipal(Constantes.ID_DESCONECTADO);
							}
							timeoutBuscarSet = false;
							timeoutHandler.removeCallbacks(timeoutbuscar);
							adaptBluetooth.stopLeScan(scanCallBack);
							adaptBluetooth.cancelDiscovery();
				}
			} else if (adaptBluetooth.isDiscovering()) {
				adaptBluetooth.stopLeScan(scanCallBack);
				// Mandamos a cerrar la lista de dispositivos porque el scan ha
				// encontrado el acushoes
				Log.d(TAG, "Buscar->Mandamos a cerrar la lista de dispositivos porque el scan ha encontrado el acushoes");
			    mensajeAListaDispositivos(100);
			    
				// Si se para el escaneo pero no se ha establecido dispositivo,
				// pasamos el com.leitat.gui a desconectado
			    if (!(adaptBluetooth.getState()==Constantes.ID_CONECTADO)) {
					mensajeAprincipal(Constantes.ID_DESCONECTADO);
				}
				// Si el hardler del timeout buscar esta establecido, lo
				// cancelamos
				if (timeoutBuscarSet) {
					timeoutHandler.removeCallbacks(timeoutbuscar);
					timeoutBuscarSet = false;
				}
			}
	}

	public void conectar(final BluetoothDevice dispositivoBLE) {
		Log.d(TAG,"conectar() - intentado conectar a " + dispositivoBLE.getName());
		/*
		if (adaptBluetooth == null) {
			Log.e(TAG,
					"conectar->El adaptador de com.leitat.servicio no esta inicializado");
			return false;
		}
		if (dispositivoBLE == null) {
			Log.e(TAG, "conectar->No se ha establecido el dispositivo btledo");
			return false;
		}
		*/
		//Apagar la busqueda
		buscar(false);
		//Crear conexion
		bluetoothGatt = dispositivoBLE.connectGatt(this, false,
				gattCallBack);
		//Establecer timeout.
		timeoutConexion(true);
		
		if (bluetoothGatt == null) {
			Log.e(TAG, "conectar - bluetoothGatt es null!");
			mensajeAprincipal(Constantes.ID_NOBT);
		}
		Log.d(TAG, "conectar->conectado a com.leitat.servicio Gatt");
		// setEstadoConexion(true);
	}

	public void desconectar() {
		if (bluetoothGatt != null && adaptBluetooth != null) {
			final BluetoothGattService servicio = bluetoothGatt.getService(Constantes.UUID_SERVICIO);
			adaptBluetooth.stopLeScan(scanCallBack); //TODO arreglar esto
			if (servicio == null) {
				Log.e(TAG,
						"desactivarNotificaciones - Servicio UUID_SERVICIO no encontrado!");
			} else {

				final BluetoothGattCharacteristic caracteristica = servicio
						.getCharacteristic(Constantes.UUID_CARACT);
				if (caracteristica == null) {
					Log.e(TAG,
							"desactivarNotificacionesHRP - caracteristica piezo_uuid no encontrada!");
				} else {
					cambiaEstadoNotificaciones(false, caracteristica);
				}
			}
	       mensajeAprincipal(BluetoothAdapter.STATE_DISCONNECTED);
			bluetoothGatt.disconnect();
		}
	}

	// -------- Ciclo de vida android para com.leitat.servicio -------
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d(TAG, "onCreate() llamado");
		
		Thread.setDefaultUncaughtExceptionHandler(new AtrapaExcepciones());
		bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
			if (bluetoothManager == null) {
				Log.e(TAG, "No se ha podido inicializar el com.leitat.servicio manager.");
			}
		// Checks if Bluetooth is supported on the device.
		adaptBluetooth=BluetoothAdapter.getDefaultAdapter();
		if (adaptBluetooth == null) {
			Toast.makeText(this, "Bluetooth no soportado", Toast.LENGTH_SHORT)
					.show();
			return;
		} else if (!adaptBluetooth.isEnabled()) {
			Log.i(TAG, "onCreate - BTLE no está activado todavía");
			final Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			enableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(enableIntent);
		}

		// Recibir eventos Bluetooth
		final IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		this.registerReceiver(listEstadoConex, filter);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy() called");
		unregisterReceiver(listEstadoConex);
		EfectosSonidoSingleton.getInstance(this).reset();
		super.onDestroy();
	}

	private final transient Handler mHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(final Message msg) {
			switch (msg.what) {
			case Constantes.ID_CONECTAR:
				Log.d(TAG, "handleMessage->ID_CONECTAR");
				conectarAutomatico();
				break;
			case Constantes.ID_DESCONECTAR:
				Log.d(TAG, "handleMessage->ID_DESCONECTAR");
				desconectar();
				break;
			default:
				Log.d(TAG, "handleMessage->default");
				break;
			}
			return true;
		}
	});

	public Handler getHandlerServicio() {
		return mHandler;
	}

	private final transient BroadcastReceiver listEstadoConex = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context,final  Intent intent) {
			final String action = intent.getAction();
			if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
				final BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				final int estadoDispositivo = intent.getIntExtra(
						BluetoothDevice.EXTRA_BOND_STATE, -1);
				Log.d(TAG,
						"listenerEstadoConexion onReceive - BluetoothDevice.ACTION_BOND_STATE_CHANGED");
				// cambiarEstadoGui();
				if (device.getName().equals(Constantes.NOMBRE_PLACA)
						&& estadoDispositivo == BluetoothDevice.BOND_NONE) {
					desconectar();
				} else if (device.getName().equals(Constantes.NOMBRE_PLACA)
						&& estadoDispositivo == BluetoothDevice.BOND_BONDED) {
					Log.d(TAG, "listenerEstadoConexion->Apareado");
				}
			}

			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				final int state = intent.getIntExtra(
						BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
				Log.d(TAG,
						"listenerEstadoConexion onReceive - BluetoothAdapter.ACTION_STATE_CHANGED"
								+ "state is" + state);
				desconectar();
			}
		}
	};

	private Runnable timeoutConexionRun;//NOPMD
	
	
	public void timeoutConexion(final boolean set){
		if(set){
			timeoutConexionRun = new Runnable() {//NOPMD
				@Override
				public void run() {
                        mensajeAprincipal(BluetoothProfile.STATE_DISCONNECTED);
						timeoutHandler.removeCallbacks(timeoutConexionRun);
				}
			};
			timeoutHandler.postDelayed(timeoutConexionRun, Constantes.TIMEOUT_SEG*1000);
		}else{
			timeoutHandler.removeCallbacks(timeoutConexionRun);
		}
	}
	
	private transient final GattCallBack gattCallBack = new GattCallBack(this);

	public void agregarDispositivoAListaDispositivos(final BluetoothDevice device,
			final int rssi) {
		Log.d(TAG, "agregarDispositivoAListaDispositivos-> Dispositivo:"
				+ device.getName());

		if (this.handListaDisp != null) {

			final Bundle mBundle = new Bundle();
			final Message msg = Message.obtain(handListaDisp, Constantes.ID_ENCONTRADO);
			mBundle.putParcelable(BluetoothDevice.EXTRA_DEVICE, device);
			mBundle.putInt(Constantes.EXTRA_RSSI, rssi);
			// mBundle.putInt(EXTRA_SOURCE, DEVICE_SOURCE_SCAN);
			msg.setData(mBundle);
			msg.sendToTarget();
		}
	}
	
	/*
	private boolean invariante() {
		boolean cumple = true;
		// Dispone de adaptador de com.leitat.servicio
		if (adaptBluetooth == null) {
			Log.e(TAG,
					"invariante->No se ha podido conseguir el adaptador com.leitat.servicio.");
			cumple = false;
		}
		if (adaptBluetooth.getState() == BluetoothAdapter.STATE_ON) {// El
																			// adaptador
																			// esta
																			// encendido
			/*
			if (adaptBluetooth.getProfileConnectionState(BluetoothProfile.GATT) == BluetoothAdapter.STATE_CONNECTED) {
				cumple = true;
			} else {
				Log.d(TAG,
						"invariante->No esta conectado al adaptador GATT del telefono el estado es:"
								+ adaptBluetooth
										.getProfileConnectionState(BluetoothProfile.GATT));
				// cumple = false;
				cumple = false;
			}
		} else {
			Log.e(TAG, "Buscar-> el adaptador Bluetooth no esta encendido.");
			cumple = false;
		}
		return cumple;
	}
*/
}