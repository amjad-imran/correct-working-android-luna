package com.noisefit_evolve2.di;

import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions;
import com.noisefit_evolve2.handler.Evolve2UpdateDeviceUnitsHandler;
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
public final class WatchModule_ProvideEvolveUpdateDeviceDataActionsFactory implements Factory<UpdateDeviceDataActions> {
  private final Provider<Evolve2UpdateDeviceUnitsHandler> evolveUpdateDeviceUnitsHandlerProvider;

  public WatchModule_ProvideEvolveUpdateDeviceDataActionsFactory(
      Provider<Evolve2UpdateDeviceUnitsHandler> evolveUpdateDeviceUnitsHandlerProvider) {
    this.evolveUpdateDeviceUnitsHandlerProvider = evolveUpdateDeviceUnitsHandlerProvider;
  }

  @Override
  public UpdateDeviceDataActions get() {
    return provideEvolveUpdateDeviceDataActions(evolveUpdateDeviceUnitsHandlerProvider.get());
  }

  public static WatchModule_ProvideEvolveUpdateDeviceDataActionsFactory create(
      Provider<Evolve2UpdateDeviceUnitsHandler> evolveUpdateDeviceUnitsHandlerProvider) {
    return new WatchModule_ProvideEvolveUpdateDeviceDataActionsFactory(evolveUpdateDeviceUnitsHandlerProvider);
  }

  public static UpdateDeviceDataActions provideEvolveUpdateDeviceDataActions(
      Evolve2UpdateDeviceUnitsHandler evolveUpdateDeviceUnitsHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideEvolveUpdateDeviceDataActions(evolveUpdateDeviceUnitsHandler));
  }
}
