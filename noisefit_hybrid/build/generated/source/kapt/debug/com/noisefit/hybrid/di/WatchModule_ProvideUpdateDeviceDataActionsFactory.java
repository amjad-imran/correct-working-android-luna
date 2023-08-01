package com.noisefit.hybrid.di;

import com.noisefit.hybrid.handler.NFHUpdateDeviceUnitsHandler;
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
  private final Provider<NFHUpdateDeviceUnitsHandler> nfhDeviceUnitsHandlerProvider;

  public WatchModule_ProvideUpdateDeviceDataActionsFactory(
      Provider<NFHUpdateDeviceUnitsHandler> nfhDeviceUnitsHandlerProvider) {
    this.nfhDeviceUnitsHandlerProvider = nfhDeviceUnitsHandlerProvider;
  }

  @Override
  public UpdateDeviceDataActions get() {
    return provideUpdateDeviceDataActions(nfhDeviceUnitsHandlerProvider.get());
  }

  public static WatchModule_ProvideUpdateDeviceDataActionsFactory create(
      Provider<NFHUpdateDeviceUnitsHandler> nfhDeviceUnitsHandlerProvider) {
    return new WatchModule_ProvideUpdateDeviceDataActionsFactory(nfhDeviceUnitsHandlerProvider);
  }

  public static UpdateDeviceDataActions provideUpdateDeviceDataActions(
      NFHUpdateDeviceUnitsHandler nfhDeviceUnitsHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideUpdateDeviceDataActions(nfhDeviceUnitsHandler));
  }
}
