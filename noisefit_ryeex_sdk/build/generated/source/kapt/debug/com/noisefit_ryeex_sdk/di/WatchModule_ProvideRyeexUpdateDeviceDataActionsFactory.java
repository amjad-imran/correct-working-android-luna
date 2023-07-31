package com.noisefit_ryeex_sdk.di;

import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions;
import com.noisefit_ryeex_sdk.handler.RyeexUpdateDeviceUnitsHandler;
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
public final class WatchModule_ProvideRyeexUpdateDeviceDataActionsFactory implements Factory<UpdateDeviceDataActions> {
  private final Provider<RyeexUpdateDeviceUnitsHandler> ryeexUpdateDeviceUnitsHandlerProvider;

  public WatchModule_ProvideRyeexUpdateDeviceDataActionsFactory(
      Provider<RyeexUpdateDeviceUnitsHandler> ryeexUpdateDeviceUnitsHandlerProvider) {
    this.ryeexUpdateDeviceUnitsHandlerProvider = ryeexUpdateDeviceUnitsHandlerProvider;
  }

  @Override
  public UpdateDeviceDataActions get() {
    return provideRyeexUpdateDeviceDataActions(ryeexUpdateDeviceUnitsHandlerProvider.get());
  }

  public static WatchModule_ProvideRyeexUpdateDeviceDataActionsFactory create(
      Provider<RyeexUpdateDeviceUnitsHandler> ryeexUpdateDeviceUnitsHandlerProvider) {
    return new WatchModule_ProvideRyeexUpdateDeviceDataActionsFactory(ryeexUpdateDeviceUnitsHandlerProvider);
  }

  public static UpdateDeviceDataActions provideRyeexUpdateDeviceDataActions(
      RyeexUpdateDeviceUnitsHandler ryeexUpdateDeviceUnitsHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideRyeexUpdateDeviceDataActions(ryeexUpdateDeviceUnitsHandler));
  }
}
