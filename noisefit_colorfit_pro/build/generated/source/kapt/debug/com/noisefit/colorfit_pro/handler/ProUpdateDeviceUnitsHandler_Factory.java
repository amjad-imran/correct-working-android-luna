package com.noisefit.colorfit_pro.handler;

import android.content.Context;
import com.noisefit.colorfit_pro.base.ProApplicationHandler;
import com.noisefit.colorfit_pro.dataConversion.DataConverter;
import com.noisefit.colorfit_pro.handler.connect.ProConnectHandler;
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
public final class ProUpdateDeviceUnitsHandler_Factory implements Factory<ProUpdateDeviceUnitsHandler> {
  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<Context> contextProvider;

  private final Provider<ProConnectHandler> proConnectHandlerProvider;

  private final Provider<ProApplicationHandler> proApplicationHandlerProvider;

  private final Provider<WatchDataStore> watchDataStoreProvider;

  public ProUpdateDeviceUnitsHandler_Factory(Provider<DataConverter> dataConverterProvider,
      Provider<Context> contextProvider, Provider<ProConnectHandler> proConnectHandlerProvider,
      Provider<ProApplicationHandler> proApplicationHandlerProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    this.dataConverterProvider = dataConverterProvider;
    this.contextProvider = contextProvider;
    this.proConnectHandlerProvider = proConnectHandlerProvider;
    this.proApplicationHandlerProvider = proApplicationHandlerProvider;
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public ProUpdateDeviceUnitsHandler get() {
    return newInstance(dataConverterProvider.get(), contextProvider.get(), proConnectHandlerProvider.get(), proApplicationHandlerProvider.get(), watchDataStoreProvider.get());
  }

  public static ProUpdateDeviceUnitsHandler_Factory create(
      Provider<DataConverter> dataConverterProvider, Provider<Context> contextProvider,
      Provider<ProConnectHandler> proConnectHandlerProvider,
      Provider<ProApplicationHandler> proApplicationHandlerProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    return new ProUpdateDeviceUnitsHandler_Factory(dataConverterProvider, contextProvider, proConnectHandlerProvider, proApplicationHandlerProvider, watchDataStoreProvider);
  }

  public static ProUpdateDeviceUnitsHandler newInstance(DataConverter dataConverter,
      Context context, ProConnectHandler proConnectHandler,
      ProApplicationHandler proApplicationHandler, WatchDataStore watchDataStore) {
    return new ProUpdateDeviceUnitsHandler(dataConverter, context, proConnectHandler, proApplicationHandler, watchDataStore);
  }
}
