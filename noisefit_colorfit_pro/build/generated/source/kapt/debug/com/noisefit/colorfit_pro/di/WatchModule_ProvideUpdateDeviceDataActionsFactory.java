package com.noisefit.colorfit_pro.di;

import com.noisefit.colorfit_pro.handler.ProUpdateDeviceUnitsHandler;
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
  private final Provider<ProUpdateDeviceUnitsHandler> proUpdateDeviceUnitsHandlerProvider;

  public WatchModule_ProvideUpdateDeviceDataActionsFactory(
      Provider<ProUpdateDeviceUnitsHandler> proUpdateDeviceUnitsHandlerProvider) {
    this.proUpdateDeviceUnitsHandlerProvider = proUpdateDeviceUnitsHandlerProvider;
  }

  @Override
  public UpdateDeviceDataActions get() {
    return provideUpdateDeviceDataActions(proUpdateDeviceUnitsHandlerProvider.get());
  }

  public static WatchModule_ProvideUpdateDeviceDataActionsFactory create(
      Provider<ProUpdateDeviceUnitsHandler> proUpdateDeviceUnitsHandlerProvider) {
    return new WatchModule_ProvideUpdateDeviceDataActionsFactory(proUpdateDeviceUnitsHandlerProvider);
  }

  public static UpdateDeviceDataActions provideUpdateDeviceDataActions(
      ProUpdateDeviceUnitsHandler proUpdateDeviceUnitsHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideUpdateDeviceDataActions(proUpdateDeviceUnitsHandler));
  }
}
