package com.google.appinventor.components.runtime;

/**
 * Callback for receiving Bluetooth Ble connection events
 *
 * @author alecuba16@gmail.com (Alejandro Blanco)
 */
interface BluetoothBleConnectionListener {
  /**
   *
   */
  void afterConnect(BluetoothBleBase bluetoothConnection);

  /**
   *
   */
  void beforeDisconnect(BluetoothBleBase bluetoothConnection);
  
  
  /**
   * 
   */
  void afterBleScanResult(BluetoothBleBase bluetoothConnection);
}
