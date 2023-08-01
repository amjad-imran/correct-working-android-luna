package com.noisefit_nav_plus.di;

import android.content.Context;
import com.google.gson.Gson;
import com.noisefit_commans.data.local.abstraction.WatchDataStore;
import com.noisefit_nav_plus.base.NavPlusApplicationHandler;
import com.noisefit_nav_plus.handler.NavPlusQueryDeviceUnitsHandler;
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
public final class WatchModule_ProvideNavPlusQueryDeviceUnitsHandlerFactory implements Factory<NavPlusQueryDeviceUnitsHandler> {
  private final Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider;

  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<Context> contextProvider;

  private final Provider<Gson> gsonProvider;

  private final Provider<WatchDataStore> watchDataStoreProvider;

  public WatchModule_ProvideNavPlusQueryDeviceUnitsHandlerFactory(
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
    return provideNavPlusQueryDeviceUnitsHandler(navPlusApplicationHandlerProvider.get(), dataConverterProvider.get(), contextProvider.get(), gsonProvider.get(), watchDataStoreProvider.get());
  }

  public static WatchModule_ProvideNavPlusQueryDeviceUnitsHandlerFactory create(
      Provider<NavPlusApplicationHandler> navPlusApplicationHandlerProvider,
      Provider<DataConverter> dataConverterProvider, Provider<Context> contextProvider,
      Provider<Gson> gsonProvider, Provider<WatchDataStore> watchDataStoreProvider) {
    return new WatchModule_ProvideNavPlusQueryDeviceUnitsHandlerFactory(navPlusApplicationHandlerProvider, dataConverterProvider, contextProvider, gsonProvider, watchDataStoreProvider);
  }

  public static NavPlusQueryDeviceUnitsHandler provideNavPlusQueryDeviceUnitsHandler(
      NavPlusApplicationHandler navPlusApplicationHandler, DataConverter dataConverter,
      Context context, Gson gson, WatchDataStore watchDataStore) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideNavPlusQueryDeviceUnitsHandler(navPlusApplicationHandler, dataConverter, context, gson, watchDataStore));
  }
}
