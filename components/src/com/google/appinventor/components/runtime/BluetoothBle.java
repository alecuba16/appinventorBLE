// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.BluetoothReflection;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.TextViewUtil;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


@DesignerComponent(version = YaVersion.BLUETOOTHBLE_COMPONENT_VERSION,
    description = "A new component ",
    category = ComponentCategory.CONNECTIVITY,
    nonVisible = true,
    iconName = "images/bluetoothBle.png")
@SimpleObject
public final class BluetoothBle  extends BluetoothConnectionBase implements BluetoothConnectionListener {
  private static UUID UUID_SERVICIO = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
  private static UUID UUID_CARACT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
  private static UUID UUID_CONFIG_DESC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
  private transient BluetoothGatt bluetoothGatt = null;
  private ComponentContainer container;
  private final List<Component> attachedComponents = new ArrayList<Component>();
  private Set<Integer> acceptableDeviceClasses;

  /**
   * Creates a new BluetoothBle.
   */
  public BluetoothBle(ComponentContainer container) {
    super(container, "BluetoothBle");
    this.container = container;
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
   * Checks whether the Bluetooth device with the given address is paired.
   *
   * @param address the MAC address of the Bluetooth device
   * @return true if the device is paired, false otherwise
   */
  @SimpleFunction(description = "Checks whether the Bluetooth device with the specified address "
      + "is paired.")
  public boolean IsDevicePaired(String address) {
    String functionName = "IsDevicePaired";
    Object bluetoothAdapter = BluetoothReflection.getBluetoothAdapter();
    if (bluetoothAdapter == null) {
      form.dispatchErrorOccurredEvent(this,
          functionName,
          ErrorMessages.ERROR_BLUETOOTH_NOT_AVAILABLE);
      return false;
    }

    if (!BluetoothReflection.isBluetoothEnabled(bluetoothAdapter)) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_BLUETOOTH_NOT_ENABLED);
      return false;
    }

    // Truncate the address at the first space.
    // This allows the address to be an element from the AddressesAndNames property.
    int firstSpace = address.indexOf(" ");
    if (firstSpace != -1) {
      address = address.substring(0, firstSpace);
    }

    if (!BluetoothReflection.checkBluetoothAddress(bluetoothAdapter, address)) {
      form.dispatchErrorOccurredEvent(this,
          functionName,
          ErrorMessages.ERROR_BLUETOOTH_INVALID_ADDRESS);
      return false;
    }

    Object bluetoothDevice = BluetoothReflection.getRemoteDevice(bluetoothAdapter, address);
    return BluetoothReflection.isBonded(bluetoothDevice);
  }

  /**
   * Returns the list of paired Bluetooth devices. Each element of the returned list is a String
   * consisting of the device's address, a space, and the device's name.
   *
   * This method calls isDeviceClassAcceptable to determine whether to include a particular device
   * in the returned list.
   *
   * @return a List representing the addresses and names of paired Bluetooth devices
   */
  @SimpleProperty(description = "The addresses and names of paired Bluetooth devices",
                  category = PropertyCategory.BEHAVIOR)
  public List<String> AddressesAndNames() {
    List<String> addressesAndNames = new ArrayList<String>();

    Object bluetoothAdapter = BluetoothReflection.getBluetoothAdapter();
    if (bluetoothAdapter != null) {
      if (BluetoothReflection.isBluetoothEnabled(bluetoothAdapter)) {
        for (Object bluetoothDevice : BluetoothReflection.getBondedDevices(bluetoothAdapter)) {
          if (isDeviceClassAcceptable(bluetoothDevice)) {
            String name = BluetoothReflection.getBluetoothDeviceName(bluetoothDevice);
            String address = BluetoothReflection.getBluetoothDeviceAddress(bluetoothDevice);
            addressesAndNames.add(address + " " + name);
          }
        }
      }
    }

    return addressesAndNames;
  }
 
  /**
   * alecuba16
   * Connects to a Bluetooth device with the given address and UUID. 
   *
   * If the address contains a space, the space and any characters after it are ignored. This
   * facilitates passing an element of the list returned from the addressesAndNames method above.
   *
   * @param functionName the name of the SimpleFunction calling this method
   * @param mac the address of the device
   * @param serviceUUID the Service UUID
   * @param characteristicUUID the characteristicUUID
   * @param descriptorUUID the descriptorUUID
   */
  public boolean connect(
                         String functionName,
                         String mac,
                         String serviceUUID,
                         String characteristicUUID,
                         String descriptorUUID) {
   
    Object bluetoothAdapter = BluetoothReflection.getBluetoothAdapter();
    if (bluetoothAdapter == null) {
      form.dispatchErrorOccurredEvent(this,
          functionName,
          ErrorMessages.ERROR_BLUETOOTH_NOT_AVAILABLE);
      return false;
    }

    if (!BluetoothReflection.isBluetoothEnabled(bluetoothAdapter)) {
      form.dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_BLUETOOTH_NOT_ENABLED);
      return false;
    }

    // Truncate the address at the first space.
    // This allows the address to be an element from the AddressesAndNames property.
    int firstSpace = mac.indexOf(" ");
    if (firstSpace != -1) {
      mac = mac.substring(0, firstSpace);
    }

    if (!BluetoothReflection.checkBluetoothAddress(bluetoothAdapter, mac)) {
      form.dispatchErrorOccurredEvent(this,
          functionName,
          ErrorMessages.ERROR_BLUETOOTH_INVALID_ADDRESS);
      return false;
    }

    Object bluetoothDevice = BluetoothReflection.getRemoteDevice(bluetoothAdapter, mac);
    if (!BluetoothReflection.isBonded(bluetoothDevice)) {
      form.dispatchErrorOccurredEvent(this,
          functionName,
          ErrorMessages.ERROR_BLUETOOTH_NOT_PAIRED_DEVICE);
      return false;
    }

    if (!isDeviceClassAcceptable(bluetoothDevice)) {
      form.dispatchErrorOccurredEvent(this,
          functionName,
          ErrorMessages.ERROR_BLUETOOTH_NOT_REQUIRED_CLASS_OF_DEVICE);
      return false;
    }

    UUID serviceuuid;
    try {
      serviceuuid = UUID.fromString(serviceUUID);
    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this,
          functionName,
          ErrorMessages.ERROR_BLUETOOTH_INVALID_UUID,
          serviceUUID);
      return false;
    }

    UUID characteristicuuid;
    try {
      characteristicuuid = UUID.fromString(characteristicUUID);
    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this,
          functionName,
          ErrorMessages.ERROR_BLUETOOTH_INVALID_UUID,
          serviceUUID);
      return false;
    }

    UUID descriptoruuid;
    try {
      descriptoruuid = UUID.fromString(descriptorUUID);
    } catch (IllegalArgumentException e) {
      form.dispatchErrorOccurredEvent(this,
          functionName,
          ErrorMessages.ERROR_BLUETOOTH_INVALID_UUID,
          serviceUUID);
      return false;
    }

    UUID_SERVICIO = serviceuuid;
    UUID_CARACT = characteristicuuid;
    UUID_CONFIG_DESC = descriptoruuid;

    Disconnect();

    //connect(bluetoothDevice);
    return true;
  }


  /**
   * Returns true if the class of the given device is acceptable.
   *
   * @param bluetoothDevice the Bluetooth device
   */
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

  /**
   * Checks if the current device supports bluetooth 4.0 LE
   *
   * @return true if the device supports BLE 4.0, false otherwise
   */
  @SimpleFunction(description = "Checks if the current device supports bluetooth 4.0 LE")
  public boolean IsBluetoothBLE() {
    boolean supports = false;
    Object bluetoothAdapter = BluetoothReflection.getBluetoothAdapter();
    if (bluetoothAdapter != null) {
      if (container.$context()
          .getPackageManager()
          .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
        supports = true;
      }
    }
    return supports;
  }
  protected BluetoothClient bluetooth;
 
 
  
 
  // -------------------------------------------------

  private void habilitarNotificaciones(final BluetoothDevice device) {
    final StringBuilder mensaje_debug = new StringBuilder();
    mensaje_debug.append("habilitarNotificaciones -");
    boolean isError;
    Object bluetoothAdapter = BluetoothReflection.getBluetoothAdapter();
    if (bluetoothAdapter == null || bluetoothGatt == null) {
      Log.e(logTag,
          "habilitarNotificaciones->El adatador de com.leitat.servicio no esta inicializado.");
      return;
    }

    final BluetoothGattService servicio = bluetoothGatt.getService(UUID_SERVICIO);
    if (servicio == null) {
      mensaje_debug.append(" Servicio servicio_generic_uuid no encontrado!");
      isError = true;
    } else {
      if (servicio.getCharacteristics().size() < 1) {
        mensaje_debug.append(" No hay caracteristicas disponibles para servicio_generic!");
        isError = true;
      } else {
        final BluetoothGattCharacteristic caracteristica = servicio.getCharacteristic(UUID_CARACT);
        if (caracteristica == null) {
          mensaje_debug.append(" caracteristica \"UUID_CARACT\" no encontrado!");
          isError = true;
        } else {
          bluetoothGatt.setCharacteristicNotification(caracteristica, true);
          if (UUID_CARACT.equals(caracteristica.getUuid())) {
            final BluetoothGattDescriptor descriptor = caracteristica.getDescriptor(UUID_CONFIG_DESC);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
          }
          isError = false;
        }
      }
    }

    if (isError) {
      Log.e(logTag, mensaje_debug.toString());
      // mensajeAprincipal(Constantes.ID_ERROR);
    } else {
      mensaje_debug.append(" Ejecutado correctamente");
      Log.d(logTag, mensaje_debug.toString());
    }

  }

  @Override
  public void afterConnect(BluetoothConnectionBase bluetoothConnection) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void beforeDisconnect(BluetoothConnectionBase bluetoothConnection) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void afterBleScanResult(BluetoothConnectionBase bluetoothConnection) {
    // TODO Auto-generated method stub
    
  }
  

}
