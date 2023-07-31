package com.noisefit.colorfit_pro.di;

import android.content.Context;
import com.noisefit.colorfit_pro.base.ProApplicationHandler;
import com.noisefit.colorfit_pro.dataConversion.DataConverter;
import com.noisefit.colorfit_pro.handler.ProUpdateDeviceUnitsHandler;
import com.noisefit.colorfit_pro.handler.connect.ProConnectHandler;
import com.noisefit_commans.data.local.abstraction.WatchDataStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class WatchModule_ProvideProUpdateDeviceUnitsHandlerFactory implements Factory<ProUpdateDeviceUnitsHandler> {
  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<Context> appContextProvider;

  private final Provider<ProConnectHandler> proConnectHandlerProvider;

  private final Provider<ProApplicationHandler> proApplicationHandlerProvider;

  private final Provider<WatchDataStore> watchDataStoreProvider;

  public WatchModule_ProvideProUpdateDeviceUnitsHandlerFactory(
      Provider<DataConverter> dataConverterProvider, Provider<Context> appContextProvider,
      Provider<ProConnectHandler> proConnectHandlerProvider,
      Provider<ProApplicationHandler> proApplicationHandlerProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    this.dataConverterProvider = dataConverterProvider;
    this.appContextProvider = appContextProvider;
    this.proConnectHandlerProvider = proConnectHandlerProvider;
    this.proApplicationHandlerProvider = proApplicationHandlerProvider;
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public ProUpdateDeviceUnitsHandler get() {
    return provideProUpdateDeviceUnitsHandler(dataConverterProvider.get(), appContextProvider.get(), proConnectHandlerProvider.get(), proApplicationHandlerProvider.get(), watchDataStoreProvider.get());
  }

  public static WatchModule_ProvideProUpdateDeviceUnitsHandlerFactory create(
      Provider<DataConverter> dataConverterProvider, Provider<Context> appContextProvider,
      Provider<ProConnectHandler> proConnectHandlerProvider,
      Provider<ProApplicationHandler> proApplicationHandlerProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    return new WatchModule_ProvideProUpdateDeviceUnitsHandlerFactory(dataConverterProvider, appContextProvider, proConnectHandlerProvider, proApplicationHandlerProvider, watchDataStoreProvider);
  }

  public static ProUpdateDeviceUnitsHandler provideProUpdateDeviceUnitsHandler(
      DataConverter dataConverter, Context appContext, ProConnectHandler proConnectHandler,
      ProApplicationHandler proApplicationHandler, WatchDataStore watchDataStore) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideProUpdateDeviceUnitsHandler(dataConverter, appContext, proConnectHandler, proApplicationHandler, watchDataStore));
  }
}
