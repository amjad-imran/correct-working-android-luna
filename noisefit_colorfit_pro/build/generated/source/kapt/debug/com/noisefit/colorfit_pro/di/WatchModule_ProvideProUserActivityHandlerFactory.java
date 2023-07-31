package com.noisefit.colorfit_pro.di;

import com.noisefit.colorfit_pro.base.ProApplicationHandler;
import com.noisefit.colorfit_pro.dataConversion.DataConverter;
import com.noisefit.colorfit_pro.handler.ProUserActivityHandler;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class WatchModule_ProvideProUserActivityHandlerFactory implements Factory<ProUserActivityHandler> {
  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<ProApplicationHandler> proApplicationHandlerProvider;

  public WatchModule_ProvideProUserActivityHandlerFactory(
      Provider<DataConverter> dataConverterProvider,
      Provider<ProApplicationHandler> proApplicationHandlerProvider) {
    this.dataConverterProvider = dataConverterProvider;
    this.proApplicationHandlerProvider = proApplicationHandlerProvider;
  }

  @Override
  public ProUserActivityHandler get() {
    return provideProUserActivityHandler(dataConverterProvider.get(), proApplicationHandlerProvider.get());
  }

  public static WatchModule_ProvideProUserActivityHandlerFactory create(
      Provider<DataConverter> dataConverterProvider,
      Provider<ProApplicationHandler> proApplicationHandlerProvider) {
    return new WatchModule_ProvideProUserActivityHandlerFactory(dataConverterProvider, proApplicationHandlerProvider);
  }

  public static ProUserActivityHandler provideProUserActivityHandler(DataConverter dataConverter,
      ProApplicationHandler proApplicationHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideProUserActivityHandler(dataConverter, proApplicationHandler));
  }
}
