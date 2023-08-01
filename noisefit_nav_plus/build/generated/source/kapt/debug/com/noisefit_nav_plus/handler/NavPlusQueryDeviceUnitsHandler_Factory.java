package com.noisefit_nav_plus.handler;

import android.content.Context;
import com.google.gson.Gson;
import com.noisefit_commans.data.local.abstraction.WatchDataStore;
import com.noisefit_nav_plus.base.NavPlusApplicationHandler;
import com.noisefit_nav_plus.handler.dataConversion.DataConverter;
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
public final class NavPlusQueryDeviceUnitsHandler_Factory implements Factory<NavPlusQueryDeviceUnitsHandler> {
  private final Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider;

  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<Context> contextProvider;

  private final Provider<Gson> gsonProvider;

  private final Provider<WatchDataStore> watchDataStoreProvider;

  public NavPlusQueryDeviceUnitsHandler_Factory(
      Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider,
      Provider<DataConverter> dataConverterProvider, Provider<Context> contextProvider,
      Provider<Gson> gsonProvider, Provider<WatchDataStore> watchDataStoreProvider) {
    this.navPlusApplicationHandlerProvider = navPlusApplicationHandlerProvider;
    this.dataConverterProvider = dataConverterProvider;
    this.contextProvider = contextProvider;
    this.gsonProvider = gsonProvider;
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public NavPlusQueryDeviceUnitsHandler get() {
    return newInstance(navPlusApplicationHandlerProvider.get(), dataConverterProvider.get(), contextProvider.get(), gsonProvider.get(), watchDataStoreProvider.get());
  }

  public static NavPlusQueryDeviceUnitsHandler_Factory create(
      Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider,
      Provider<DataConverter> dataConverterProvider, Provider<Context> contextProvider,
      Provider<Gson> gsonProvider, Provider<WatchDataStore> watchDataStoreProvider) {
    return new NavPlusQueryDeviceUnitsHandler_Factory(navPlusApplicationHandlerProvider, dataConverterProvider, contextProvider, gsonProvider, watchDataStoreProvider);
  }

  public static NavPlusQueryDeviceUnitsHandler newInstance(
      NavPlusApplicationHandler navPlusApplicationHandler, DataConverter dataConverter,
      Context context, Gson gson, WatchDataStore watchDataStore) {
    return new NavPlusQueryDeviceUnitsHandler(navPlusApplicationHandler, dataConverter, context, gson, watchDataStore);
  }
}
