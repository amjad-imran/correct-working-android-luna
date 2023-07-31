package com.noisefit_ryeex_sdk.di;

import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions;
import com.noisefit_ryeex_sdk.handler.RyeexQueryDeviceUnitsHandler;
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
public final class WatchModule_ProvideRyeexQueryDeviceDataActionsFactory implements Factory<QueryDeviceDataActions> {
  private final Provider<RyeexQueryDeviceUnitsHandler> ryeexQueryDeviceUnitsHandlerProvider;

  public WatchModule_ProvideRyeexQueryDeviceDataActionsFactory(
      Provider<RyeexQueryDeviceUnitsHandler> ryeexQueryDeviceUnitsHandlerProvider) {
    this.ryeexQueryDeviceUnitsHandlerProvider = ryeexQueryDeviceUnitsHandlerProvider;
  }

  @Override
  public QueryDeviceDataActions get() {
    return provideRyeexQueryDeviceDataActions(ryeexQueryDeviceUnitsHandlerProvider.get());
  }

  public static WatchModule_ProvideRyeexQueryDeviceDataActionsFactory create(
      Provider<RyeexQueryDeviceUnitsHandler> ryeexQueryDeviceUnitsHandlerProvider) {
    return new WatchModule_ProvideRyeexQueryDeviceDataActionsFactory(ryeexQueryDeviceUnitsHandlerProvider);
  }

  public static QueryDeviceDataActions provideRyeexQueryDeviceDataActions(
      RyeexQueryDeviceUnitsHandler ryeexQueryDeviceUnitsHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideRyeexQueryDeviceDataActions(ryeexQueryDeviceUnitsHandler));
  }
}
