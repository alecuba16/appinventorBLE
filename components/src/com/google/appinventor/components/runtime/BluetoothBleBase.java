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
    
  
}
