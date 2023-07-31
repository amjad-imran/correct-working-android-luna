package com.noisefit_cf2.di;

import com.noisefit_cf2.handler.CF2QueryDeviceUnitsHandler;
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
public final class WatchModule_ProvideQueryDeviceDataActionsFactory implements Factory<QueryDeviceDataActions> {
  private final Provider<CF2QueryDeviceUnitsHandler> cf2QueryDeviceUnitsHandlerProvider;

  public WatchModule_ProvideQueryDeviceDataActionsFactory(
      Provider<CF2QueryDeviceUnitsHandler> cf2QueryDeviceUnitsHandlerProvider) {
    this.cf2QueryDeviceUnitsHandlerProvider = cf2QueryDeviceUnitsHandlerProvider;
  }

  @Override
  public QueryDeviceDataActions get() {
    return provideQueryDeviceDataActions(cf2QueryDeviceUnitsHandlerProvider.get());
  }

  public static WatchModule_ProvideQueryDeviceDataActionsFactory create(
      Provider<CF2QueryDeviceUnitsHandler> cf2QueryDeviceUnitsHandlerProvider) {
    return new WatchModule_ProvideQueryDeviceDataActionsFactory(cf2QueryDeviceUnitsHandlerProvider);
  }

  public static QueryDeviceDataActions provideQueryDeviceDataActions(
      CF2QueryDeviceUnitsHandler cf2QueryDeviceUnitsHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideQueryDeviceDataActions(cf2QueryDeviceUnitsHandler));
  }
}
