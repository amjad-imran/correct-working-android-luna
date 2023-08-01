package com.noisefit_evolve2.di;

import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions;
import com.noisefit_evolve2.handler.Evolve2QueryDeviceUnitsHandler;
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
public final class WatchModule_ProvideEvolveQueryDeviceDataActionsFactory implements Factory<QueryDeviceDataActions> {
  private final Provider<Evolve2QueryDeviceUnitsHandler> evolve2QueryDeviceUnitsHandlerProvider;

  public WatchModule_ProvideEvolveQueryDeviceDataActionsFactory(
      Provider<Evolve2QueryDeviceUnitsHandler> evolve2QueryDeviceUnitsHandlerProvider) {
    this.evolve2QueryDeviceUnitsHandlerProvider = evolve2QueryDeviceUnitsHandlerProvider;
  }

  @Override
  public QueryDeviceDataActions get() {
    return provideEvolveQueryDeviceDataActions(evolve2QueryDeviceUnitsHandlerProvider.get());
  }

  public static WatchModule_ProvideEvolveQueryDeviceDataActionsFactory create(
      Provider<Evolve2QueryDeviceUnitsHandler> evolve2QueryDeviceUnitsHandlerProvider) {
    return new WatchModule_ProvideEvolveQueryDeviceDataActionsFactory(evolve2QueryDeviceUnitsHandlerProvider);
  }

  public static QueryDeviceDataActions provideEvolveQueryDeviceDataActions(
      Evolve2QueryDeviceUnitsHandler evolve2QueryDeviceUnitsHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideEvolveQueryDeviceDataActions(evolve2QueryDeviceUnitsHandler));
  }
}
