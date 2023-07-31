package com.noisefit.colorfit_pro.di;

import android.content.Context;
import com.noisefit.colorfit_pro.base.ProApplicationHandler;
import com.noisefit.colorfit_pro.dataConversion.DataConverter;
import com.noisefit.colorfit_pro.handler.ProQueryDeviceUnitsHandler;
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
public final class WatchModule_ProvideProQueryDeviceUnitsHandlerFactory implements Factory<ProQueryDeviceUnitsHandler> {
  private final Provider<ProApplicationHandler> proApplicationHandlerProvider;

  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<Context> contextProvider;

  private final Provider<WatchDataStore> watchDataStoreProvider;

  public WatchModule_ProvideProQueryDeviceUnitsHandlerFactory(
      Provider<ProApplicationHandler> proApplicationHandlerProvider,
      Provider<DataConverter> dataConverterProvider, Provider<Context> contextProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    this.proApplicationHandlerProvider = proApplicationHandlerProvider;
    this.dataConverterProvider = dataConverterProvider;
    this.contextProvider = contextProvider;
    this.watchDataStoreProvider = watchDataStoreProvider;
  }

  @Override
  public ProQueryDeviceUnitsHandler get() {
    return provideProQueryDeviceUnitsHandler(proApplicationHandlerProvider.get(), dataConverterProvider.get(), contextProvider.get(), watchDataStoreProvider.get());
  }

  public static WatchModule_ProvideProQueryDeviceUnitsHandlerFactory create(
      Provider<ProApplicationHandler> proApplicationHandlerProvider,
      Provider<DataConverter> dataConverterProvider, Provider<Context> contextProvider,
      Provider<WatchDataStore> watchDataStoreProvider) {
    return new WatchModule_ProvideProQueryDeviceUnitsHandlerFactory(proApplicationHandlerProvider, dataConverterProvider, contextProvider, watchDataStoreProvider);
  }

  public static ProQueryDeviceUnitsHandler provideProQueryDeviceUnitsHandler(
      ProApplicationHandler proApplicationHandler, DataConverter dataConverter, Context context,
      WatchDataStore watchDataStore) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideProQueryDeviceUnitsHandler(proApplicationHandler, dataConverter, context, watchDataStore));
  }
}
