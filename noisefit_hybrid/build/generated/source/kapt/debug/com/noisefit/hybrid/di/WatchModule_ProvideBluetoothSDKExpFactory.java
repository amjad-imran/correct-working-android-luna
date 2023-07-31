package com.noisefit.hybrid.di;

import com.noisefit.hybrid.utils.BluetoothSDK_Exp;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class WatchModule_ProvideBluetoothSDKExpFactory implements Factory<BluetoothSDK_Exp> {
  @Override
  public BluetoothSDK_Exp get() {
    return provideBluetoothSDKExp();
  }

  public static WatchModule_ProvideBluetoothSDKExpFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static BluetoothSDK_Exp provideBluetoothSDKExp() {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideBluetoothSDKExp());
  }

  private static final class InstanceHolder {
    private static final WatchModule_ProvideBluetoothSDKExpFactory INSTANCE = new WatchModule_ProvideBluetoothSDKExpFactory();
  }
}
