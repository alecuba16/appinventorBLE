// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.runtime.util.BluetoothReflection;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.content.pm.PackageManager;
import android.util.Log;


/**
 * An abstract base class for the Bluetooth BLE.
 *
 * @author alecuba16@gmail.com (Alejandro Blanco Martinez)
 */
@SimpleObject
@UsesPermissions(permissionNames =
"android.permission.BLUETOOTH, " +
"android.permission.BLUETOOTH_ADMIN")
public abstract class BluetoothBleBase  extends AndroidNonvisibleComponent
implements Component, OnDestroyListener, Deleteable {
  
  protected final String logTag;
  private final List<BluetoothBleConnectionListener> bluetoothConnectionListeners =
      new ArrayList<BluetoothBleConnectionListener>();  
  
  


  protected ComponentContainer container;
  private final List<Component> attachedComponents = new ArrayList<Component>();
  private Set<Integer> acceptableDeviceClasses;
  
  /**
   * Creates a new BluetoothBle.
   */
  protected BluetoothBleBase(ComponentContainer container, String logTag) {
    this(container.$form(), logTag);
    this.container=container;
    form.registerForOnDestroy(this);
  }
  
  private BluetoothBleBase(Form form, String logTag) {
    super(form);
    this.logTag = logTag;
  }
  
  
  /**
   * Adds a {@link BluetoothBleConnectionListener} to the listener list.
   *
   * @param listener  the {@code BluetoothBleConnectionListener} to be added
   */
  void addBluetoothConnectionListener(BluetoothBleConnectionListener listener) {
    bluetoothConnectionListeners.add(listener);
  }

  /**
   * Removes a {@link BluetoothBleConnectionListener} from the listener list.
   *
   * @param listener  the {@code BluetoothBleConnectionListener} to be removed
   */
  void removeBluetoothConnectionListener(BluetoothConnectionListener listener) {
    bluetoothConnectionListeners.remove(listener);
  }

  private void fireAfterConnectEvent() {
    for (BluetoothBleConnectionListener listener : bluetoothConnectionListeners) {
      listener.afterConnect(this);
    }
  }

  private void fireBeforeDisconnectEvent() {
    for (BluetoothBleConnectionListener listener : bluetoothConnectionListeners) {
      listener.beforeDisconnect(this);
    }
  }
  
  public void fireAfterBleScanResult(BluetoothDevice device) {
    for (BluetoothBleConnectionListener listener : bluetoothConnectionListeners) {
      listener.afterBleScanResult(this,device);
    }
  }
  
//OnDestroyListener implementation

 @Override
 public void onDestroy() {
   //TODO Disconnect here
 }

 // Deleteable implementation

 @Override
 public void onDelete() {
   //TODO Disconnect here
 }
  
  boolean attachComponent(Component component, Set<Integer> acceptableDeviceClasses) {
    if (attachedComponents.isEmpty()) {
      // If this is the first/only attached component, we keep the acceptableDeviceClasses.
      this.acceptableDeviceClasses = (acceptableDeviceClasses == null) ? null
          : new HashSet<Integer>(acceptableDeviceClasses);

    } else {
      // If there is already one or more attached components, the acceptableDeviceClasses must be
      // the same as what we already have.
      if (this.acceptableDeviceClasses == null) {
        if (acceptableDeviceClasses != null) {
          return false;
        }
      } else {
        if (acceptableDeviceClasses == null) {
          return false;
        }
        if (!this.acceptableDeviceClasses.containsAll(acceptableDeviceClasses)) {
          return false;
        }
        if (!acceptableDeviceClasses.containsAll(this.acceptableDeviceClasses)) {
          return false;
        }
      }
    }

    attachedComponents.add(component);
    return true;
  }

  void detachComponent(Component component) {
    attachedComponents.remove(component);
    if (attachedComponents.isEmpty()) {
      acceptableDeviceClasses = null;
    }
  }
  
  /**
   * Checks if the Bluetooth device is scanning.
   *
   * @return true if the device is scanning, false otherwise
   */
  @SimpleFunction(description = "Checks if the Bluetooth device is scanning")
  public boolean isDiscovering() {
    boolean scanning=false;
    BluetoothAdapter bluetoothAdapter =(BluetoothAdapter) BluetoothReflection.getBluetoothAdapter();
    if (bluetoothAdapter == null) return false;
    return   bluetoothAdapter.isDiscovering();
  }
    
  BluetoothBleScanCallBack scanCallBack = new BluetoothBleScanCallBack();
  
  public final class BluetoothBleScanCallBack implements LeScanCallback {
    
      @Override
      public void onLeScan(final BluetoothDevice device,final int rssi,
      final byte[] scanRecord) {
      Log.d("BluetoothBleScanCallBack", "onLeScan() - encontrado=" + device + ", rssi=" + rssi + " ,nombre:"+device.getName());
      fireAfterBleScanResult(device);
     }
  }
   
  BluetoothGattCallback gattCallBack = new GattCallBack();
  
  public class GattCallBack extends BluetoothGattCallback {
    private transient final String TAG = GattCallBack.class.getSimpleName();
 
    public GattCallBack(){
      super();
    }
    
   @Override
   public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
     Log.d(TAG, "onConnectionStateChange ("
         + gatt.getDevice().getAddress() + ") newstate=" + newState);
     switch(newState){
       case BluetoothProfile.STATE_CONNECTING://Conectando
         Log.d(TAG,"onConnectionStateChange ->conectando");
         break;
       case BluetoothProfile.STATE_CONNECTED://Conectado
         fireAfterConnectEvent();
         gatt.discoverServices();
         break;
       case BluetoothProfile.STATE_DISCONNECTING://Desconectando
         break;
       case BluetoothProfile.STATE_DISCONNECTED://Desconectado
         fireBeforeDisconnectEvent();
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
         //if (serviciotemp.getUuid().equals(Constantes.UUID_SERVICIO)) {
         //  serviciostxt.append("\nHe encontrado el com.leitat.servicio");
          // servicio.habilitarNotificaciones(gatt.getDevice());
        // }
         serviciostxt.append(serviciotemp.getUuid()+",");
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
   
 
   
   @Override
   public void onCharacteristicChanged(final BluetoothGatt gatt,
       final BluetoothGattCharacteristic characteristic) {
     Log.d(TAG,
         "onCharacteristicChanged - ha cambiado una caracteristica");
     final StringBuilder mensaje_debug = new StringBuilder();
     mensaje_debug.append("caracteristicaActualizada -");
     
     final UUID charUuid = characteristic.getUuid();

     //if (!charUuid.equals(Constantes.UUID_CONFIG_DESC)) {
     //    final GestorDatos GestorDatos=new GestorDatos(servicio);
     //    final Bundle mBundle=GestorDatos.tramaAGui(characteristic.getValue());
     //    servicio.mensajeAprincipal(Constantes.ID_VALOR, mBundle);
    // }

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


}

 

 
  private boolean isDeviceClassAcceptable(Object bluetoothDevice) {
    if (acceptableDeviceClasses == null) {
      // Add devices are acceptable.
      return true;
    }

    Object bluetoothClass = BluetoothReflection.getBluetoothClass(bluetoothDevice);
    if (bluetoothClass == null) {
      // This device has no class.
      return false;
    }

    int deviceClass = BluetoothReflection.getDeviceClass(bluetoothClass);
    return acceptableDeviceClasses.contains(deviceClass);
  }

  @SimpleFunction(description = "Checks if the current device supports bluetooth 4.0 LE")
  public boolean IsBluetoothBLE() {
    boolean supports = false;
    Object bluetoothAdapter = BluetoothReflection.getBluetoothAdapter();
    if (bluetoothAdapter != null) {
      if (container.$context()!=null){
        if(container.$context().getPackageManager()!=null){
          if(container.$context().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
              supports = true;
              }
        }else{Log.d(logTag,"container.$context().getPackageManager() null");}
        }else{Log.d(logTag,"container.$context(). null");}
      }else{Log.d(logTag,"container.$context() null");}
    return supports;
  } 
  
  public void discoverServices(BluetoothGatt bluetoothGatt){
    bluetoothGatt.discoverServices();
  }
}
