package com.noisefit.colorfit_pro.handler;

import android.content.Context;
import com.noisefit.colorfit_pro.base.ProApplicationHandler;
import com.noisefit.colorfit_pro.dataConversion.DataConverter;
import com.noisefit_commans.data.local.abstraction.WatchDataStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class ProQueryDeviceUnitsHandler_Factory implements Factory<ProQueryDeviceUnitsHandler> {
  private final Provider<ProApplicationHandler> navPlusApplicationHandlerProvider;

  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<Context> contextProvider;

  private final Provider<WatchDataStore> watchDataStoreProvider;

  public ProQueryDeviceUnitsHandler_Factory(
      Provider<ProApplicationHandler> navPlusApplicationHandlerProvider,
      Provider<DataConverter> dataConverterProvider, Provider<Context> contextProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    this.navPlusApplicationHandlerProvider = navPlusApplicationHandlerProvider;
    this.dataConverterProvider = dataConverterProvider;
    this.contextProvider = contextProvider;
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public ProQueryDeviceUnitsHandler get() {
    return newInstance(navPlusApplicationHandlerProvider.get(), dataConverterProvider.get(), contextProvider.get(), watchDataStoreProvider.get());
  }

  public static ProQueryDeviceUnitsHandler_Factory create(
      Provider<ProApplicationHandler> navPlusApplicationHandlerProvider,
      Provider<DataConverter> dataConverterProvider, Provider<Context> contextProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    return new ProQueryDeviceUnitsHandler_Factory(navPlusApplicationHandlerProvider, dataConverterProvider, contextProvider, watchDataStoreProvider);
  }

  public static ProQueryDeviceUnitsHandler newInstance(
      ProApplicationHandler navPlusApplicationHandler, DataConverter dataConverter, Context context,
      WatchDataStore watchDataStore) {
    return new ProQueryDeviceUnitsHandler(navPlusApplicationHandler, dataConverter, context, watchDataStore);
  }
}
