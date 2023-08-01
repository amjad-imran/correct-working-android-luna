package com.noisefit.hybrid.utils;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class BluetoothSDK_Exp_Factory implements Factory<BluetoothSDK_Exp> {
  @Override
  public BluetoothSDK_Exp get() {
    return newInstance();
  }

  public static BluetoothSDK_Exp_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static BluetoothSDK_Exp newInstance() {
    return new BluetoothSDK_Exp();
  }

  private static final class InstanceHolder {
    private static final BluetoothSDK_Exp_Factory INSTANCE = new BluetoothSDK_Exp_Factory();
  }
}
