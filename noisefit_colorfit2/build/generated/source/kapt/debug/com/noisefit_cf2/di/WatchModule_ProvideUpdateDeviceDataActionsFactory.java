package com.noisefit_cf2.di;

import com.noisefit_cf2.handler.CF2UpdateDeviceUnitsHandler;
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("javax.inject.Named")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class WatchModule_ProvideUpdateDeviceDataActionsFactory implements Factory<UpdateDeviceDataActions> {
  private final Provider<CF2UpdateDeviceUnitsHandler> cf2DeviceUnitsHandlerProvider;

  public WatchModule_ProvideUpdateDeviceDataActionsFactory(
      Provider<CF2UpdateDeviceUnitsHandler> cf2DeviceUnitsHandlerProvider) {
    this.cf2DeviceUnitsHandlerProvider = cf2DeviceUnitsHandlerProvider;
  }

  @Override
  public UpdateDeviceDataActions get() {
    return provideUpdateDeviceDataActions(cf2DeviceUnitsHandlerProvider.get());
  }

  public static WatchModule_ProvideUpdateDeviceDataActionsFactory create(
      Provider<CF2UpdateDeviceUnitsHandler> cf2DeviceUnitsHandlerProvider) {
    return new WatchModule_ProvideUpdateDeviceDataActionsFactory(cf2DeviceUnitsHandlerProvider);
  }

  public static UpdateDeviceDataActions provideUpdateDeviceDataActions(
      CF2UpdateDeviceUnitsHandler cf2DeviceUnitsHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideUpdateDeviceDataActions(cf2DeviceUnitsHandler));
  }
}
