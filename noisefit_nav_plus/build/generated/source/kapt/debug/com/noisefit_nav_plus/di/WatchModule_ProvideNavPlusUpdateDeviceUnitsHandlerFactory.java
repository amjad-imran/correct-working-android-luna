package com.noisefit_nav_plus.di;

import android.content.Context;
import com.google.gson.Gson;
import com.noisefit_commans.data.local.abstraction.WatchDataStore;
import com.noisefit_nav_plus.base.NavPlusApplicationHandler;
import com.noisefit_nav_plus.handler.NavPlusUpdateDeviceUnitsHandler;
import com.noisefit_nav_plus.handler.dataConversion.DataConverter;
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
public final class WatchModule_ProvideNavPlusUpdateDeviceUnitsHandlerFactory implements Factory<NavPlusUpdateDeviceUnitsHandler> {
  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<Context> appContextProvider;

  private final Provider<Gson> gsonProvider;

  private final Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider;

  private final Provider<WatchDataStore> watchDataStoreProvider;

  public WatchModule_ProvideNavPlusUpdateDeviceUnitsHandlerFactory(
      Provider<DataConverter> dataConverterProvider, Provider<Context> appContextProvider,
      Provider<Gson> gsonProvider,
      Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    this.dataConverterProvider = dataConverterProvider;
    this.appContextProvider = appContextProvider;
    this.gsonProvider = gsonProvider;
    this.navPlusApplicationHandlerProvider = navPlusApplicationHandlerProvider;
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public NavPlusUpdateDeviceUnitsHandler get() {
    return provideNavPlusUpdateDeviceUnitsHandler(dataConverterProvider.get(), appContextProvider.get(), gsonProvider.get(), navPlusApplicationHandlerProvider.get(), watchDataStoreProvider.get());
  }

  public static WatchModule_ProvideNavPlusUpdateDeviceUnitsHandlerFactory create(
      Provider<DataConverter> dataConverterProvider, Provider<Context> appContextProvider,
      Provider<Gson> gsonProvider,
      Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    return new WatchModule_ProvideNavPlusUpdateDeviceUnitsHandlerFactory(dataConverterProvider, appContextProvider, gsonProvider, navPlusApplicationHandlerProvider, watchDataStoreProvider);
  }

  public static NavPlusUpdateDeviceUnitsHandler provideNavPlusUpdateDeviceUnitsHandler(
      DataConverter dataConverter, Context appContext, Gson gson,
      NavPlusApplicationHandler navPlusApplicationHandler, WatchDataStore watchDataStore) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideNavPlusUpdateDeviceUnitsHandler(dataConverter, appContext, gson, navPlusApplicationHandler, watchDataStore));
  }
}
