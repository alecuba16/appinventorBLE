package com.leitat.servicio;

import java.util.Iterator;
import java.util.UUID;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.util.Log;

public class GattCallBack extends BluetoothGattCallback {
	   private transient final String TAG = GattCallBack.class.getSimpleName();
	   private transient final Servicio servicio;
	
	   public GattCallBack(final Servicio servicio){
		   super();
		   this.servicio=servicio;
	   }
	   
		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
			Log.d(TAG, "onConnectionStateChange ("
					+ gatt.getDevice().getAddress() + ") newstate=" + newState);
			switch(newState){
				case BluetoothProfile.STATE_CONNECTING://Conectando
					Log.d(TAG,"onConnectionStateChange ->conectando");
					servicio.mensajeAprincipal(Constantes.ID_CONECTANDO);
					servicio.mensajeAListaDispositivos(Constantes.ID_CONECTANDO);
					break;
				case BluetoothProfile.STATE_CONNECTED://Conectado
					servicio.timeoutConexion(false);
					gatt.discoverServices();
					servicio.mensajeAprincipal(Constantes.ID_CONECTADO);
					servicio.mensajeAListaDispositivos(Constantes.ID_CONECTADO);
					break;
				case BluetoothProfile.STATE_DISCONNECTING://Desconectando
					servicio.mensajeAprincipal(Constantes.ID_DESCONECTANDO);
					servicio.mensajeAListaDispositivos(Constantes.ID_DESCONECTANDO);
					break;
				case BluetoothProfile.STATE_DISCONNECTED://Desconectado
					servicio.timeoutConexion(false);
					servicio.desconectar();
					servicio.mensajeAprincipal(Constantes.ID_DESCONECTADO);
					servicio.mensajeAListaDispositivos(Constantes.ID_DESCONECTADO);
					break;
				default:
						break;
			}
		}

		@Override
		public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
			final StringBuilder serviciostxt = new StringBuilder();
			serviciostxt.append("onServicesDiscovered->Status: " + status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				serviciostxt.append("\nonServicesDiscovered->GATT success status: " + status);
				final Iterator<BluetoothGattService> todosLosServicios = gatt
						.getServices().iterator();
				BluetoothGattService serviciotemp;
				while (todosLosServicios.hasNext()) {
					serviciotemp = todosLosServicios.next();
					if (serviciotemp.getUuid().equals(Constantes.UUID_SERVICIO)) {
						serviciostxt.append("\nHe encontrado el com.leitat.servicio");
						servicio.habilitarNotificaciones(gatt.getDevice());
					}
					// serviciostxt.append(serviciotemp.getUuid()+",");
				}
			} else {
				serviciostxt.append(" error en el status no es success");
			}
			Log.d(TAG, serviciostxt.toString());
		}


		@Override
		public void onDescriptorWrite(final BluetoothGatt gatt,
				final BluetoothGattDescriptor descriptor, final int status) {
			Log.d(TAG, "onDescriptorWrite Servicio:"
					+ descriptor.getCharacteristic().getService().getUuid()
					+ " Caracteristica:"
					+ descriptor.getCharacteristic().getUuid() + " Descriptor:"
					+ descriptor.getUuid() + " status:" + status);
		}
		
		/*

		@Override
		public void onDescriptorRead(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			BluetoothGattCharacteristic caracteristica_del_descriptor = descriptor
					.getCharacteristic();
			Log.d(TAG,
					"onDescriptorRead Servicio:"
							+ descriptor.getCharacteristic().getService()
									.getUuid() + " Caracteristica:"
							+ descriptor.getCharacteristic().getUuid()
							+ " Descriptor:" + descriptor.getUuid()
							+ " permisosmask:" + descriptor.getPermissions());
			cambiaEstadoNotificaciones(true, caracteristica_del_descriptor);
		}*/
		
		@Override
		public void onCharacteristicChanged(final BluetoothGatt gatt,
				final BluetoothGattCharacteristic characteristic) {
			Log.d(TAG,
					"onCharacteristicChanged - ha cambiado una caracteristica");
			final StringBuilder mensaje_debug = new StringBuilder();
			mensaje_debug.append("caracteristicaActualizada -");
			
			final UUID charUuid = characteristic.getUuid();

			if (!charUuid.equals(Constantes.UUID_CONFIG_DESC)) {
			    final GestorDatos GestorDatos=new GestorDatos(servicio);
			    final Bundle mBundle=GestorDatos.tramaAGui(characteristic.getValue());
			    servicio.mensajeAprincipal(Constantes.ID_VALOR, mBundle);
			}

			mensaje_debug.append(" Campos ");
			for (int i = 1; i < 8; i++) {
				mensaje_debug.append(" campo(");
				mensaje_debug.append(i);
				mensaje_debug.append("):");
				mensaje_debug.append(characteristic.getIntValue(
						BluetoothGattCharacteristic.FORMAT_UINT8, i));
			}
			Log.d(TAG, mensaje_debug.toString());
		}

		/*
		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			Log.d(TAG, "onCharacteristicRead");
		}
*/
}
