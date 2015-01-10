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
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;


import android.view.View;

@DesignerComponent(version = YaVersion.BLUETOOTHBLESCANCALLBACK_COMPONENT_VERSION,
    description = "A new component ",
    category = ComponentCategory.CONNECTIVITY,
    nonVisible = true,
    iconName = "images/bluetoothBleScanCallBack.png")
@SimpleObject
public final class BluetoothBleScanCallBack extends AndroidNonvisibleComponent {

  /**
   * Creates a new component.
   *
   * @param container  container, component will be placed in
   */
  public BluetoothBleScanCallBack(ComponentContainer container) {
    super(container.$form());
  }

}
