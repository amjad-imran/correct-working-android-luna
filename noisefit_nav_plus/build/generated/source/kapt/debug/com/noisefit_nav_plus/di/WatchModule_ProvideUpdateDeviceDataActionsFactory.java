package com.noisefit_nav_plus.di;

import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions;
import com.noisefit_nav_plus.handler.NavPlusUpdateDeviceUnitsHandler;
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
  private final Provider<NavPlusUpdateDeviceUnitsHandler> navPlusUpdateDeviceUnitsHandlerProvider;

  public WatchModule_ProvideUpdateDeviceDataActionsFactory(
      Provider<NavPlusUpdateDeviceUnitsHandler> navPlusUpdateDeviceUnitsHandlerProvider) {
    this.navPlusUpdateDeviceUnitsHandlerProvider = navPlusUpdateDeviceUnitsHandlerProvider;
  }

  @Override
  public UpdateDeviceDataActions get() {
    return provideUpdateDeviceDataActions(navPlusUpdateDeviceUnitsHandlerProvider.get());
  }

  public static WatchModule_ProvideUpdateDeviceDataActionsFactory create(
      Provider<NavPlusUpdateDeviceUnitsHandler> navPlusUpdateDeviceUnitsHandlerProvider) {
    return new WatchModule_ProvideUpdateDeviceDataActionsFactory(navPlusUpdateDeviceUnitsHandlerProvider);
  }

  public static UpdateDeviceDataActions provideUpdateDeviceDataActions(
      NavPlusUpdateDeviceUnitsHandler navPlusUpdateDeviceUnitsHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideUpdateDeviceDataActions(navPlusUpdateDeviceUnitsHandler));
  }
}
