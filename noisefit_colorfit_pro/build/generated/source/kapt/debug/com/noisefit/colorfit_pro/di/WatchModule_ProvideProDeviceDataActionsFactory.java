package com.noisefit.colorfit_pro.di;

import com.noisefit.colorfit_pro.handler.ProQueryDeviceUnitsHandler;
import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions;
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
public final class WatchModule_ProvideProDeviceDataActionsFactory implements Factory<QueryDeviceDataActions> {
  private final Provider<ProQueryDeviceUnitsHandler> proQueryDeviceUnitsHandlerProvider;

  public WatchModule_ProvideProDeviceDataActionsFactory(
      Provider<ProQueryDeviceUnitsHandler> proQueryDeviceUnitsHandlerProvider) {
    this.proQueryDeviceUnitsHandlerProvider = proQueryDeviceUnitsHandlerProvider;
  }

  @Override
  public QueryDeviceDataActions get() {
    return provideProDeviceDataActions(proQueryDeviceUnitsHandlerProvider.get());
  }

  public static WatchModule_ProvideProDeviceDataActionsFactory create(
      Provider<ProQueryDeviceUnitsHandler> proQueryDeviceUnitsHandlerProvider) {
    return new WatchModule_ProvideProDeviceDataActionsFactory(proQueryDeviceUnitsHandlerProvider);
  }

  public static QueryDeviceDataActions provideProDeviceDataActions(
      ProQueryDeviceUnitsHandler proQueryDeviceUnitsHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideProDeviceDataActions(proQueryDeviceUnitsHandler));
  }
}
