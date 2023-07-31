package com.noisefit_ryeex_sdk.di;

import com.noisefit_ryeex_sdk.base.RyeexApplicationHandler;
import com.noisefit_ryeex_sdk.dataConversion.DataConverter;
import com.noisefit_ryeex_sdk.handler.RyeexUserActivityHandler;
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
public final class WatchModule_ProvideRyeexUserActivityHandlerFactory implements Factory<RyeexUserActivityHandler> {
  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider;

  public WatchModule_ProvideRyeexUserActivityHandlerFactory(
      Provider<DataConverter> dataConverterProvider,
      Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider) {
    this.dataConverterProvider = dataConverterProvider;
    this.ryeexApplicationHandlerProvider = ryeexApplicationHandlerProvider;
  }

  @Override
  public RyeexUserActivityHandler get() {
    return provideRyeexUserActivityHandler(dataConverterProvider.get(), ryeexApplicationHandlerProvider.get());
  }

  public static WatchModule_ProvideRyeexUserActivityHandlerFactory create(
      Provider<DataConverter> dataConverterProvider,
      Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider) {
    return new WatchModule_ProvideRyeexUserActivityHandlerFactory(dataConverterProvider, ryeexApplicationHandlerProvider);
  }

  public static RyeexUserActivityHandler provideRyeexUserActivityHandler(
      DataConverter dataConverter, RyeexApplicationHandler ryeexApplicationHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideRyeexUserActivityHandler(dataConverter, ryeexApplicationHandler));
  }
}
