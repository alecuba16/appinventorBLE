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

import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.runtime.BluetoothBleBase.Servicio;
import com.google.appinventor.components.runtime.util.BluetoothReflection;

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
  protected transient BluetoothGatt bluetoothGatt = null;
  
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
  
  public void fireAfterBleScanResult() {
    for (BluetoothBleConnectionListener listener : bluetoothConnectionListeners) {
      listener.afterBleScanResult(this);
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
   * Return if it is scanning BLE
   */
  @SimpleFunction(description =  "Return if it is scanning BLE")
  public boolean isScanning() {
    if(((BluetoothAdapter) BluetoothReflection.getBluetoothAdapter()).isDiscovering()){
      isDiscoveringServices=false;
      isNotificated=false;
      bluetoothGatt.disconnect();
    }
    return ((BluetoothAdapter) BluetoothReflection.getBluetoothAdapter()).isDiscovering();
  }
  
  /**
   * Return if it is isConnected
   */
  @SimpleFunction(description = "Return if it is Connected")
  public boolean isConnected() {
    if(((BluetoothAdapter) BluetoothReflection.getBluetoothAdapter()).getState()==BluetoothAdapter.STATE_CONNECTED) return true; else{
      isDiscoveringServices=false;
      isNotificated=false;
      return false;
    }
  }
  
  protected boolean isDiscoveringServices=false;
  protected boolean isNotificated=false;
  
  /**
   * Return if it isDiscoveringServices
   */
  @SimpleFunction(description = "Return if it isDiscoveringServices")
  public boolean isDiscoveringServices() {
    if(isConnected()&&!isScanning()) return isDiscoveringServices; else{
      isNotificated=false;
      return false;
    }
  }
  
  @SimpleFunction(description = "Return the bluetooth connection state:0=NotScanning,1=Scanning,0=STATE_DISCONNECTED,1=STATE_CONNECTING,2=STATE_CONNECTED,3=STATE_DISCONNECTING,4=DiscoveringServices,5=ServicesDiscovered,6=EnablingNotifications,7=NotificationsEnabled,8=NotificationsDisabled ")
  public boolean isEnabledNotifications() {
    if(isConnected()&&!isScanning()&&!isDiscoveringServices()) return isNotificated; else return false;
  }
  
  /**
   * Checks if the Bluetooth device is scanning.
   *
   * @return true if the device is scanning, false otherwise
   */
  @SimpleFunction(description = "Checks if the Bluetooth device is scanning")
  public boolean isDiscovering() {
    BluetoothAdapter bluetoothAdapter =(BluetoothAdapter) BluetoothReflection.getBluetoothAdapter();
    if (bluetoothAdapter == null) return false;
    return   bluetoothAdapter.isDiscovering();
  }
  
  protected BluetoothDevice device2 = null;
  protected BluetoothBleScanCallBack scanCallBack = new BluetoothBleScanCallBack();
  
  public final class BluetoothBleScanCallBack implements LeScanCallback {
      private final Set<BluetoothDevice> bleScanResult = new HashSet<BluetoothDevice>();
      @Override
      public void onLeScan(final BluetoothDevice device,final int rssi,
      final byte[] scanRecord) {
      Log.d(logTag, "onLeScan() - encontrado=" + device + ", rssi=" + rssi + " ,nombre:"+device.getName());
      //bluetoothGatt2 = device.connectGatt(container.$context(), true,gattCallBack);
      if(!checkIfExistsInList(device.getAddress())){
        bleScanResult.add(device);
        Log.d(logTag,"afterBleScanResult->anadidolista");
      }else{  Log.d(logTag,"afterBleScanResult->existe");}
      device2=device;
      fireAfterBleScanResult();
     }
      
    private boolean checkIfExistsInList(String deviceAddress){
      boolean encontrado=false;
      Iterator<BluetoothDevice> itr =bleScanResult.iterator();
      while(!encontrado &&itr.hasNext())
      {
        if(itr.next().getAddress()==deviceAddress) encontrado=true;
      }
      return encontrado;
    }
    
    /**
     * Returns the device if device is in the list, otherwise returns null
     * @param deviceAddressOrName address/Name to be searched
     * @param perName if you are looking by the name (true) or by address (false-default)
     * @return the device instance if exists on the list, null otherwise
     */
    public BluetoothDevice getDeviceInList(String deviceAddressOrName,boolean perName){
      BluetoothDevice device=null;
      BluetoothDevice deviceTemp=null;
      boolean encontrado=false;
      Iterator<BluetoothDevice> itr =bleScanResult.iterator();
      while(!encontrado&&itr.hasNext())
      {
        deviceTemp=itr.next();
        if(perName)
          if(deviceTemp.getName().equals(deviceAddressOrName.trim())) {encontrado=true;device=deviceTemp;}
        else
          if(deviceTemp.getAddress().equals(deviceAddressOrName.trim())) {encontrado=true;device=deviceTemp;}
      }
      return device;
    }
    
    /**
    * getDevicerAddressPerName Returns the device address of searched name
    * @param deviceName Name to be searched
    * @return the deviceAddress if exists on the list, empty otherwise
    */
   public String getDevicerAddressPerName(String deviceName){
     String deviceAddress="";
     boolean encontrado=false;     
     BluetoothDevice deviceTemp=null; 
     Iterator<BluetoothDevice> itr =bleScanResult.iterator();
     while(!encontrado &&itr.hasNext())
     {
       deviceTemp=itr.next();
       if(deviceTemp.getName().equals(deviceName.trim())) {encontrado=true;deviceAddress=deviceTemp.getAddress();}
     }
     return deviceAddress;
   }
    
    /**
     * Returns the number of found devices.
     * @return Returns the number of found devices
     */
    public int getNumberFoundDevices(){
      if(bleScanResult!=null&&bleScanResult.size()>0) return bleScanResult.size(); else return 0;
    }
    
    /**
     * Returns the device list names.
     * @return Returns the device list names.
     */
    public List<String> getDeviceNames(){
      List<String> deviceNameslist= new ArrayList<String>();
      Iterator<BluetoothDevice> itr =bleScanResult.iterator();
      while(itr.hasNext())
      {
        deviceNameslist.add(itr.next().getName());      
      }
      return deviceNameslist;
    }
    
    /**
     * Returns the device list address.
     * @return Returns the device list address.
     */
    public List<String> getDeviceAddress(){
      List<String> deviceAddresslist= new ArrayList<String>();
      Iterator<BluetoothDevice> itr =bleScanResult.iterator();
      while(itr.hasNext())
      {
        deviceAddresslist.add(itr.next().getAddress());      
      }
      return deviceAddresslist;
    }
  }
  
  /**
   * enableNotificationOfCharacteristic.
   * @param serviceuuid a string representing the uuid of the service
   * @param characteristic a string representing the uuid of the characteristic
   */
  @SimpleFunction(description = "enableNotificationOfCharacteristic.")
  public void enableNotificationOfCharacteristic(String serviceuuid,String characteristicuuid){
    Servicio servicio = getServiceFromUUID(serviceuuid);
    Caracteristica caracteristica = getCharacteristicFromUUID(servicio,characteristicuuid);
    bluetoothGatt.setCharacteristicNotification(caracteristica.characteristic,true);
  }
  
  /**
   * disableNotificationOfCharacteristic.
   * @param serviceuuid a string representing the uuid of the service
   * @param characteristic a string representing the uuid of the characteristic
   */
  @SimpleFunction(description = "disableNotificationOfCharacteristic.")
  public void disableNotificationOfCharacteristic(String serviceuuid,String characteristicuuid){
    Servicio servicio = getServiceFromUUID(serviceuuid);
    Caracteristica caracteristica = getCharacteristicFromUUID(servicio,characteristicuuid);
    bluetoothGatt.setCharacteristicNotification(caracteristica.characteristic,false);
  }
  
  
  /**
   * getServicesUUID.
   * @return returns a List of services uuid.
   */
  @SimpleFunction(description = "getServicesUUID returns a List of services uuid")
  public List<String> getServicesUUID(){
    List<String> listaServicios = new ArrayList<String>();
    Iterator<Servicio> itr =  serviciosCaracteristicas.iterator();
    while(itr.hasNext()){
      listaServicios.add(itr.next().service.getUuid().toString().replace("\"", ""));
    }
    return listaServicios;
  }
  
  /**
   * getCharacteristics of a given service uuid.
   * @param serviceuuid a string representing the uuid of the service
   * @return returns a List of characteristics inside this service uuid.
   */
  @SimpleFunction(description = "returns a List of characteristics inside this service uuid, empty if not found.")
  public List<String> getCharacteristicsOfServiceUUID(String serviceuuid){
    List<String> listaCaracteristicas = new ArrayList<String>();
    Servicio servicio = getServiceFromUUID(serviceuuid);
    if(servicio==null) {
      Log.d(logTag,"->Servicio es null");
      return listaCaracteristicas;
  } else if(servicio.caracteristicas==null) {
    Log.d(logTag,"->caracteristcias es null");
      return listaCaracteristicas;
  } else {
    Iterator<BluetoothGattCharacteristic> chr_itr=servicio.service.getCharacteristics().iterator();
    while(chr_itr.hasNext()){
      listaCaracteristicas.add(chr_itr.next().getUuid().toString().replace("\"", ""));
    }
    return listaCaracteristicas;
  }
  }
  
  
  
  
  /**
   * getDescriptorsOfCharacteristicUUID of a given service uuid and characteristicuuid.
   * @param serviceuuid a string representing the uuid of the service
   * @param characteristic a string representing the uuid of the characteristic
   * @return returns a List of descriptors inside this service uuid  and characteristic uuid .
   */
  @SimpleFunction(description = "returns a List of descriptors inside this service and characteristic uuid, empty if not found.")
  public List<String> getDescriptorsOfCharacteristicUUID(String serviceuuid,String characteristicuuid){
    Servicio servicio = getServiceFromUUID(serviceuuid);
    Caracteristica caracteristica = getCharacteristicFromUUID(servicio,characteristicuuid);
    List<String> listaDescriptores = new ArrayList<String>();
    for(int i=0;i<caracteristica.descriptores.size();i++){
      listaDescriptores.add(caracteristica.descriptores.get(i).uuidDescriptor.toString().replace("\"", ""));
    }
    return listaDescriptores;
  }
  
  private Servicio getServiceFromUUID(String uuidofService){
    boolean encontrado=false;
    Iterator<Servicio> itr = serviciosCaracteristicas.iterator();
    Servicio serviciotemp=null;
    while(!encontrado && itr.hasNext()){
      serviciotemp=itr.next();
      if(serviciotemp.uuidServicio.toString()==uuidofService){encontrado=true;}
    }
    if(!encontrado) serviciotemp=null;
    return serviciotemp;
  }
  
  private Caracteristica getCharacteristicFromUUID(Servicio servicio,String uuidofcharacteristic){
    boolean encontrado=false;
    Iterator<Caracteristica> itr = servicio.caracteristicas.iterator();
    Caracteristica caracteristicaTemp=null;
    while(!encontrado && itr.hasNext()){
      caracteristicaTemp=itr.next();
      if(caracteristicaTemp.uuidCaracteristica.toString()==uuidofcharacteristic){encontrado=true;}
    }
    if(!encontrado) caracteristicaTemp=null;
    return caracteristicaTemp;
  }
  
  private Descriptor getDescriptorFromUUID(Caracteristica caracteristica,String uuidofdescriptor){
    boolean encontrado=false;
    Iterator<Descriptor> itr = caracteristica.descriptores.iterator();
    Descriptor descriptorTemp=null;
    while(!encontrado && itr.hasNext()){
      descriptorTemp=itr.next();
      if(descriptorTemp.uuidDescriptor.toString()==uuidofdescriptor){encontrado=true;}
    }
    if(!encontrado) descriptorTemp=null;
    return descriptorTemp;
  }
  
  public final List<Servicio> serviciosCaracteristicas = new ArrayList<Servicio>();
  
  public class Servicio  {
    public UUID uuidServicio=null;
    public List<Caracteristica> caracteristicas=null;
    public BluetoothGattService service = null;
    public Servicio( final BluetoothGattService yo,final UUID uuidServicio,final List<Caracteristica> caracteristicas){
      this.service=yo;
      this.uuidServicio=uuidServicio;
      this.caracteristicas=caracteristicas;
    }
  
  }
  
  public class Caracteristica {
    public UUID uuidCaracteristica=null;
    public BluetoothGattCharacteristic characteristic = null;
    public List<Descriptor> descriptores=null;
    public Caracteristica(final BluetoothGattCharacteristic yo,final UUID uuidCaracteristica,final List<Descriptor> descriptores){
      this.characteristic=yo;
      this.uuidCaracteristica=uuidCaracteristica;
      this.descriptores=descriptores;
    }
  }
  
  public class Descriptor {
    public UUID uuidDescriptor=null;
    public BluetoothGattDescriptor descriptor = null;
    public Descriptor(final BluetoothGattDescriptor yo,final UUID uuidDescriptor){
      this.descriptor=yo;
      this.uuidDescriptor=uuidDescriptor;
    }
  }
  protected BluetoothGattCallback gattCallBack = new MiGattCallBack();
  
  public class MiGattCallBack extends BluetoothGattCallback {
 
    public MiGattCallBack(){
      super();
    }
    
   @Override
   public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
     Log.d(logTag, "onConnectionStateChange ->("
         + gatt.getDevice().getAddress() + ") newstate=" + newState);
     switch(newState){
       case BluetoothProfile.STATE_CONNECTING://Conectando
         isDiscoveringServices=false;
         isNotificated=false;
         Log.d(logTag,"onConnectionStateChange ->conectando");
         break;
       case BluetoothProfile.STATE_CONNECTED://Conectado
         fireAfterConnectEvent();
         gatt.discoverServices();
         break;
       case BluetoothProfile.STATE_DISCONNECTING://Desconectando
         isDiscoveringServices=false;
         isNotificated=false;
         break;
       case BluetoothProfile.STATE_DISCONNECTED://Desconectado
         isDiscoveringServices=false;
         isNotificated=false;
         fireBeforeDisconnectEvent();
         break;
       default:
           break;
     }
   }
  
   @Override
   public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
     isDiscoveringServices=true;
     final StringBuilder serviciostxt = new StringBuilder();
     serviciostxt.append("onServicesDiscovered->Status: " + status);
     if (status == BluetoothGatt.GATT_SUCCESS) {
       serviciostxt.append("\nonServicesDiscovered->GATT success status: " + status);
       final Iterator<BluetoothGattService> todosLosServicios = gatt
           .getServices().iterator();
       while (todosLosServicios.hasNext()) {
         BluetoothGattService serviciotemp = todosLosServicios.next();
         
         List<Caracteristica> listacaracteristicas = new ArrayList<Caracteristica>();
         Iterator<BluetoothGattCharacteristic> itr_Char = serviciotemp.getCharacteristics().iterator();
         while(itr_Char.hasNext()){
           BluetoothGattCharacteristic charTemp = itr_Char.next();
           Iterator<BluetoothGattDescriptor> itr_Descr = charTemp.getDescriptors().iterator();
           List<Descriptor> listadescriptores = new ArrayList<Descriptor>();
           while(itr_Descr.hasNext()){//Creo una lista de descriptores
             BluetoothGattDescriptor descTemp=itr_Descr.next();
             listadescriptores.add(new Descriptor(descTemp,descTemp.getUuid()));
             Log.d(logTag,"->Anadido descriptor");
           }
           listacaracteristicas.add(new Caracteristica(charTemp,charTemp.getUuid(),listadescriptores));
           Log.d(logTag,"->Anadido caracteristica");
         }
         serviciosCaracteristicas.add(new Servicio(serviciotemp,serviciotemp.getUuid(),listacaracteristicas));
         Log.d(logTag,"->Anadido servicio");
         serviciostxt.append(serviciotemp.getUuid()+",");
       }
     } else {
       serviciostxt.append(" error en el status no es success");
     }
     Log.d(logTag, serviciostxt.toString());
   }


   @Override
   public void onDescriptorWrite(final BluetoothGatt gatt,
       final BluetoothGattDescriptor descriptor, final int status) {
     Log.d(logTag, "->onDescriptorWrite Servicio:"
         + descriptor.getCharacteristic().getService().getUuid()
         + " Caracteristica:"
         + descriptor.getCharacteristic().getUuid() + " Descriptor:"
         + descriptor.getUuid() + " status:" + status);
   }
   
 
   
   @Override
   public void onCharacteristicChanged(final BluetoothGatt gatt,
       final BluetoothGattCharacteristic characteristic) {
     isNotificated=true;
     Log.d(logTag,
         "->onCharacteristicChanged - ha cambiado una caracteristica");
     final StringBuilder mensaje_debug = new StringBuilder();
     mensaje_debug.append("caracteristicaActualizada -");
     
     //final UUID charUuid = characteristic.getUuid();

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
     Log.d(logTag, mensaje_debug.toString());
   }


}

 
/*
 
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
  */

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
