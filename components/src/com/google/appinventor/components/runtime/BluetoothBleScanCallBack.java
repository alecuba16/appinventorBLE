// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import java.util.ArrayList;
import java.util.List;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.YailList;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;

@DesignerComponent(version = YaVersion.BLUETOOTHBLESCANCALLBACK_COMPONENT_VERSION,
    description = "A new component ",
    category = ComponentCategory.CONNECTIVITY,
    nonVisible = true,
    iconName = "images/bluetoothBleScanCallBack.png")
@SimpleObject
public final class BluetoothBleScanCallBack extends BluetoothConnectionBase implements LeScanCallback {
  
  
  /**
   * Creates a new component.
   *
   * @param container  container, component will be placed in
   */
  public BluetoothBleScanCallBack(ComponentContainer container) {
    super(container,"BluetoothBleScanCallBack");
  }
 
  
    @Override
    public void onLeScan(final BluetoothDevice device,final int rssi,
    final byte[] scanRecord) {
    //List<String> unresultado=new ArrayList<String>();
    //unresultado.add(device.getName());
    //unresultado.add(device.getAddress());
    //unresultado.add(Integer.toString(rssi));
    //BluetoothConnectionBase.this.bleScanResult.add(unresultado);
    bleScanResult.add(device);
    Log.d("BluetoothBleScanCallBack", "onLeScan() - encontrado=" + device + ", rssi=" + rssi + " ,nombre:"+device.getName());
    fireAfterBleScanResult();
   }
  
  //private YailList bleScanResult = new YailList(); ;
  private List<BluetoothDevice> bleScanResult = new ArrayList<BluetoothDevice>(); ;
  

  public List<BluetoothDevice> getBleScanResult() {
   return bleScanResult;
  }

}
