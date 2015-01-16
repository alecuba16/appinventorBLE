package com.google.appinventor.components.runtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.BluetoothReflection;
import com.google.appinventor.components.runtime.util.ErrorMessages;

@DesignerComponent(version = YaVersion.BLUETOOTHBLECLIENT_COMPONENT_VERSION,
    description = "A new component ",
    category = ComponentCategory.CONNECTIVITY,
    nonVisible = true,
    iconName = "images/bluetoothBleClient.png")
@SimpleObject
@UsesPermissions(permissionNames =
                 "android.permission.BLUETOOTH, " +
                 "android.permission.BLUETOOTH_ADMIN")
public final class BluetoothBleClient extends BluetoothBleBase implements BluetoothBleConnectionListener, Component, Deleteable { 
  private final List<Component> attachedComponents = new ArrayList<Component>();
  private Set<Integer> acceptableDeviceClasses;

 
  //private static UUID UUID_SERVICIO = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
  //private static UUID UUID_CARACT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
  //private static UUID UUID_CONFIG_DESC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
  
  /**
   * Creates a new BluetoothClient.
   */
  public BluetoothBleClient(ComponentContainer containerup) {
    super(containerup, "BluetoothClient");
  }

  
  boolean attachComponent(Component component, Set<Integer> acceptableDeviceClasses) {
    if (attachedComponents.isEmpty()) {
      // If this is the first/only attached component, we keep the acceptableDeviceClasses.
      this.acceptableDeviceClasses = (acceptableDeviceClasses == null)
          ? null
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
  @SimpleFunction(description = "Checks whether the Bluetooth device with the specified address " +
  "is paired.")
  public boolean IsDevicePaired(String address) {
    String functionName = "IsDevicePaired";
    Object bluetoothAdapter = BluetoothReflection.getBluetoothAdapter();
    if (bluetoothAdapter == null) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_BLUETOOTH_NOT_AVAILABLE);
      return false;
    }

    if (!BluetoothReflection.isBluetoothEnabled(bluetoothAdapter)) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_BLUETOOTH_NOT_ENABLED);
      return false;
    }

    // Truncate the address at the first space.
    // This allows the address to be an element from the AddressesAndNames property.
    int firstSpace = address.indexOf(" ");
    if (firstSpace != -1) {
      address = address.substring(0, firstSpace);
    }

    if (!BluetoothReflection.checkBluetoothAddress(bluetoothAdapter, address)) {
      form.dispatchErrorOccurredEvent(this, functionName,
          ErrorMessages.ERROR_BLUETOOTH_INVALID_ADDRESS);
      return false;
    }

    Object bluetoothDevice = BluetoothReflection.getRemoteDevice(bluetoothAdapter, address);
    return BluetoothReflection.isBonded(bluetoothDevice);
  }
 
  /**
   * scanDevices.
   * @param scanCallBack scanCallBack component
   * @return true if scan is started,false otherwise.
   */
  @SimpleFunction(description = "scanDevices")
  public boolean scanDevices(){
    addBluetoothConnectionListener(this);
    if(!isDiscovering()){
      BluetoothAdapter bluetoothAdapter =(BluetoothAdapter) BluetoothReflection.getBluetoothAdapter();
      if (bluetoothAdapter == null) return false;
      return bluetoothAdapter.startLeScan(scanCallBack);
    }else  return false; 
  }
       
  @Override
  public void afterConnect(BluetoothBleBase bluetoothConnection) {
    Log.d(logTag,"afterConnect->He conectado!");
    discoverServices(bluetoothGatt);    
  }


  @Override
  public void beforeDisconnect(BluetoothBleBase bluetoothConnection) {
    Log.d(logTag,"afterConnect->He desconectado!");    
  }
  
  @Override
  public void afterBleScanResult(BluetoothBleBase bluetoothConnection) {
      Log.d(logTag,"afterBleScanResult->invocado en BleClient");
  }
  
  /**
   * Connects to a Bluetooth device with the given address. 
   *
   * @param deviceAddress deviceAddress
   * @return true if connection petition is processed correctly, false otherwise.
   */
  @SimpleFunction(description = "Connects to a Bluetooth device with the given address")
  public boolean connectWithAddress(String deviceAddress) {
    BluetoothAdapter.getDefaultAdapter().stopLeScan(scanCallBack);
    //BluetoothDevice device=scanCallBack.getDeviceInList(deviceAddress,false);
    //BluetoothDevice device=((BluetoothAdapter) BluetoothReflection.getBluetoothAdapter()).getRemoteDevice(deviceAddress);
    if(device2!=null){
      Log.d(logTag,container.$context().getTitle().toString());
      bluetoothGatt = device2.connectGatt(container.$context().getApplicationContext(), true,gattCallBack);
      if(bluetoothGatt==null){
        Log.d(logTag,"->Dispositivo esta en la lista, pero connectGatt es null");
        return false;
      }else{
        Log.d(logTag,"connectWithName->conectado a:"+bluetoothGatt.getDevice().getName()+" al gat!");
        return true;
        }
    }else{Log.d(logTag,"connectWithAddress->Dispositivo "+deviceAddress+" no esta en la lista"); return false;}
  }
  
  /**
   * Connects to a Bluetooth device with the given deviceName. 
   *
   * @param deviceName deviceName
   * @return true if connection petition is processed correctly, false otherwise.
   */
  @SimpleFunction(description = "Connects to a Bluetooth device with the given Name")
  public boolean connectWithName(String deviceName) {
    BluetoothAdapter.getDefaultAdapter().stopLeScan(scanCallBack);
    //TODO BluetoothDevice device=scanCallBack.getDeviceInList(deviceName,false);
    //BluetoothDevice device=((BluetoothAdapter) BluetoothReflection.getBluetoothAdapter()).getRemoteDevice(scanCallBack.getDevicerAddressPerName(deviceName));
    if(device2!=null){
      Log.d(logTag,"->1"+container.$context().getTitle().toString());
      bluetoothGatt = device2.connectGatt(container.$context(), true,gattCallBack);
      if(bluetoothGatt==null){
        Log.d(logTag,"connectWithName->Dispositivo esta en la lista, pero connectGatt es null");
        return false;
      }else{
        Log.d(logTag,"connectWithName->conectado a:"+bluetoothGatt.getDevice().getName()+" al gat!");
        return true;        
        }
    }else{Log.d(logTag,"connectWithName->Dispositivo "+deviceName+" no esta en la lista"); return false;} 
  }
  
  /**
   * get DeviceScanned.
   * 
   * something.
   * 
   * @return true if connection petition is processed correctly, false otherwise.
   */ 
  @SimpleFunction(description = "Get number found devices")
  public int getNumberFoundDevices(){
    return scanCallBack.getNumberFoundDevices();
  }
   
  /**
   * get getFoundDevicesPerName.
   * @return a List with the name of the found devices.
   */
  @SimpleFunction(description = "getFoundDevicesPerName")
  public List<String> getFoundDevicesPerName(){
   return  scanCallBack.getDeviceNames();
  }
  
  /**
   * get getFoundDevicesPerAddress.
   * @return a List with the addrress of the found devices.
   */
  @SimpleFunction(description = "getFoundDevicesPerAddress")
  public List<String> getFoundDevicesPerAddress(){
    return  scanCallBack.getDeviceAddress();
  }
 
  /** alecuba16
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
  /*
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

   //TODO Disconnect here

   //connect(bluetoothDevice);
   return true;
 }*/
}
