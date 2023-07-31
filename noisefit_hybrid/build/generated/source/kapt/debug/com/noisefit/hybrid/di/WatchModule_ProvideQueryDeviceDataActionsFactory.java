package com.noisefit.hybrid.di;

import com.noisefit.hybrid.handler.NFHQueryDeviceUnitsHandler;
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
  private final Provider<NFHQueryDeviceUnitsHandler> nfhQueryDeviceUnitsHandlerProvider;

  public WatchModule_ProvideQueryDeviceDataActionsFactory(
      Provider<NFHQueryDeviceUnitsHandler> nfhQueryDeviceUnitsHandlerProvider) {
    this.nfhQueryDeviceUnitsHandlerProvider = nfhQueryDeviceUnitsHandlerProvider;
  }

  @Override
  public QueryDeviceDataActions get() {
    return provideQueryDeviceDataActions(nfhQueryDeviceUnitsHandlerProvider.get());
  }

  public static WatchModule_ProvideQueryDeviceDataActionsFactory create(
      Provider<NFHQueryDeviceUnitsHandler> nfhQueryDeviceUnitsHandlerProvider) {
    return new WatchModule_ProvideQueryDeviceDataActionsFactory(nfhQueryDeviceUnitsHandlerProvider);
  }

  public static QueryDeviceDataActions provideQueryDeviceDataActions(
      NFHQueryDeviceUnitsHandler nfhQueryDeviceUnitsHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideQueryDeviceDataActions(nfhQueryDeviceUnitsHandler));
  }
}
