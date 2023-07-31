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
public final class NavPlusUpdateDeviceUnitsHandler_Factory implements Factory<NavPlusUpdateDeviceUnitsHandler> {
  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<Context> contextProvider;

  private final Provider<Gson> gsonProvider;

  private final Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider;

  private final Provider<WatchDataStore> watchDataStoreProvider;

  public NavPlusUpdateDeviceUnitsHandler_Factory(Provider<DataConverter> dataConverterProvider,
      Provider<Context> contextProvider, Provider<Gson> gsonProvider,
      Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    this.dataConverterProvider = dataConverterProvider;
    this.contextProvider = contextProvider;
    this.gsonProvider = gsonProvider;
    this.navPlusApplicationHandlerProvider = navPlusApplicationHandlerProvider;
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public NavPlusUpdateDeviceUnitsHandler get() {
    return newInstance(dataConverterProvider.get(), contextProvider.get(), gsonProvider.get(), navPlusApplicationHandlerProvider.get(), watchDataStoreProvider.get());
  }

  public static NavPlusUpdateDeviceUnitsHandler_Factory create(
      Provider<DataConverter> dataConverterProvider, Provider<Context> contextProvider,
      Provider<Gson> gsonProvider,
      Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    return new NavPlusUpdateDeviceUnitsHandler_Factory(dataConverterProvider, contextProvider, gsonProvider, navPlusApplicationHandlerProvider, watchDataStoreProvider);
  }

  public static NavPlusUpdateDeviceUnitsHandler newInstance(DataConverter dataConverter,
      Context context, Gson gson, NavPlusApplicationHandler navPlusApplicationHandler,
      WatchDataStore watchDataStore) {
    return new NavPlusUpdateDeviceUnitsHandler(dataConverter, context, gson, navPlusApplicationHandler, watchDataStore);
  }
}
