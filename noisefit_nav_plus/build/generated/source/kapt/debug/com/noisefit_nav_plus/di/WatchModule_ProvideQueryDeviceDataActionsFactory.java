package com.noisefit_nav_plus.di;

import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions;
import com.noisefit_nav_plus.handler.NavPlusQueryDeviceUnitsHandler;
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
  private final Provider<NavPlusQueryDeviceUnitsHandler> navPlusQueryDeviceUnitsHandlerProvider;

  public WatchModule_ProvideQueryDeviceDataActionsFactory(
      Provider<NavPlusQueryDeviceUnitsHandler> navPlusQueryDeviceUnitsHandlerProvider) {
    this.navPlusQueryDeviceUnitsHandlerProvider = navPlusQueryDeviceUnitsHandlerProvider;
  }

  @Override
  public QueryDeviceDataActions get() {
    return provideQueryDeviceDataActions(navPlusQueryDeviceUnitsHandlerProvider.get());
  }

  public static WatchModule_ProvideQueryDeviceDataActionsFactory create(
      Provider<NavPlusQueryDeviceUnitsHandler> navPlusQueryDeviceUnitsHandlerProvider) {
    return new WatchModule_ProvideQueryDeviceDataActionsFactory(navPlusQueryDeviceUnitsHandlerProvider);
  }

  public static QueryDeviceDataActions provideQueryDeviceDataActions(
      NavPlusQueryDeviceUnitsHandler navPlusQueryDeviceUnitsHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideQueryDeviceDataActions(navPlusQueryDeviceUnitsHandler));
  }
}
