package com.google.appinventor.components.runtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
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

  private transient BluetoothGatt bluetoothGatt = null;
  private static UUID UUID_SERVICIO = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
  private static UUID UUID_CARACT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
  private static UUID UUID_CONFIG_DESC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
  
  /**
   * Creates a new BluetoothClient.
   */
  public BluetoothBleClient(ComponentContainer container) {
    super(container, "BluetoothClient");
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
   * Returns the list of paired Bluetooth devices. Each element of the returned
   * list is a String consisting of the device's address, a space, and the
   * device's name.
   *
   * This method calls isDeviceClassAcceptable to determine whether to include
   * a particular device in the returned list.
   *
   * @return a List representing the addresses and names of paired
   *         Bluetooth devices
   */
  @SimpleProperty(description = "The addresses and names of paired Bluetooth devices",
      category = PropertyCategory.BEHAVIOR)
  public List<String> AddressesAndNames() {
    List<String> addressesAndNames = new ArrayList<String>();

    Object bluetoothAdapter = BluetoothReflection.getBluetoothAdapter();
    if (bluetoothAdapter != null) {
      if (BluetoothReflection.isBluetoothEnabled(bluetoothAdapter)) {
        for (Object bluetoothDevice : BluetoothReflection.getBondedDevices(bluetoothAdapter)) {
         // if (isDeviceClassAcceptable(bluetoothDevice)) {
            String name = BluetoothReflection.getBluetoothDeviceName(bluetoothDevice);
            String address = BluetoothReflection.getBluetoothDeviceAddress(bluetoothDevice);
            addressesAndNames.add(address + " " + name);
         // }
        }
      }
    }

    return addressesAndNames;
  }

 
  
/**
 * scanDevices.
 * @param scanCallBack scanCallBack component
 */
@SimpleFunction(description = "scanDevices")
public boolean scanDevices(){
  addBluetoothConnectionListener(this);
  if(!isDiscovering()){
    BluetoothAdapter bluetoothAdapter =(BluetoothAdapter) BluetoothReflection.getBluetoothAdapter();
    if (bluetoothAdapter == null) return false;
    return false;
    //return bluetoothAdapter.startLeScan(scanCallBack);
  }else  return false; 
}
      
 
  @Override
  public void afterConnect(BluetoothBleBase bluetoothConnection) {
    Log.d(logTag,"afterConnect->He conectado!");
    //discoverServices(bluetoothGatt);
    
  }


  @Override
  public void beforeDisconnect(BluetoothBleBase bluetoothConnection) {
    // TODO Auto-generated method stub
    
  }
  
  @Override
  public void afterBleScanResult(BluetoothBleBase bluetoothConnection,BluetoothDevice device) {
    if(!checkIfExistsInList(device.getAddress())){
      bleScanResult.add(device);
      Log.d(logTag,"afterBleScanResult->anadidolista");
    }else{  Log.d(logTag,"afterBleScanResult->existe");}    
  }
  
  //private YailList bleScanResult = new YailList(); ;
  private List<BluetoothDevice> bleScanResult = new ArrayList<BluetoothDevice>();
  
  private boolean checkIfExistsInList(String deviceAddress){
    int i=0;
    boolean encontrado=false;
    while(!encontrado && i<bleScanResult.size())
    {
      if(bleScanResult.get(i).getAddress()==deviceAddress) encontrado=true;
    }
    return encontrado;
  }
}
