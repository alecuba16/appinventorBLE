// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;




import android.view.View;

@DesignerComponent(version = YaVersion.BLUETOOTHBLECLIENT_COMPONENT_VERSION,
    description = "A new component ",
    category = ComponentCategory.CONNECTIVITY,
    nonVisible = true,
    iconName = "images/bluetoothBleClient.png")
@SimpleObject
@UsesPermissions(permissionNames =
"android.permission.BLUETOOTH, " +
"android.permission.BLUETOOTH_ADMIN")
public final class BluetoothBleClient extends BluetoothBle {

  /**
   * Creates a new component.
   *
   * @param container  container, component will be placed in
   */
  public BluetoothBleClient(ComponentContainer container) {
    super(container,"BluetoothBleScanCallBack");
  }
  
  /**
   * scanDevices.
   * @param scanCallBack scanCallBack component
   */
  @SimpleFunction(description = "startLeScan")
  public void scanDevices(BluetoothBleScanCallBack scanCallBack){
    if(!isDiscovering()){
      startLeScan(scanCallBack);
    } 
  }

}
