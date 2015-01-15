package com.leitat.servicio;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

public class MLeScanCallBack implements BluetoothAdapter.LeScanCallback {
	 // Device scan callback.
	private transient final String TAG = MLeScanCallBack.class
	.getSimpleName();
	
	private transient final Servicio servicio;
	
	public MLeScanCallBack(final Servicio servicio){
		this.servicio=servicio;
	}
	
	@Override
	public void onLeScan(final BluetoothDevice device,final int rssi,
	final byte[] scanRecord) {
	Log.d(TAG, "onLeScan() - dispositivo=" + device + ", rssi=" + rssi + " ,nombre:"+device.getName());
	if (device.getName().equals(Constantes.NOMBRE_PLACA)) {
		Log.d(TAG, "onLeScan() - encontrado=" + device + ", rssi=" + rssi + " ,nombre:"+device.getName());
		
		//Hemos encontrado el dispositivo
		servicio.conectar(device);
	} else { //Dispositivo no encontrado
		Log.d(TAG, "onLeScan() - no encontrado("
				+Constantes.NOMBRE_PLACA
				+ ") pero detectado("+device.getName()+"), se añade a la lista del selector");
		servicio.agregarDispositivoAListaDispositivos(
				device, rssi);
	}
}
}
