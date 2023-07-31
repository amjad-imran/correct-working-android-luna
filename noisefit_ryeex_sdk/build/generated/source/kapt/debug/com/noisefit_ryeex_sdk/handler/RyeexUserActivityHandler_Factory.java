package com.noisefit_ryeex_sdk.handler;

import com.noisefit_ryeex_sdk.base.RyeexApplicationHandler;
import com.noisefit_ryeex_sdk.dataConversion.DataConverter;
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
public final class RyeexUserActivityHandler_Factory implements Factory<RyeexUserActivityHandler> {
  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider;

  public RyeexUserActivityHandler_Factory(Provider<DataConverter> dataConverterProvider,
      Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider) {
    this.dataConverterProvider = dataConverterProvider;
    this.ryeexApplicationHandlerProvider = ryeexApplicationHandlerProvider;
  }

  @Override
  public RyeexUserActivityHandler get() {
    return newInstance(dataConverterProvider.get(), ryeexApplicationHandlerProvider.get());
  }

  public static RyeexUserActivityHandler_Factory create(
      Provider<DataConverter> dataConverterProvider,
      Provider<RyeexApplicationHandler> ryeexApplicationHandlerProvider) {
    return new RyeexUserActivityHandler_Factory(dataConverterProvider, ryeexApplicationHandlerProvider);
  }

  public static RyeexUserActivityHandler newInstance(DataConverter dataConverter,
      RyeexApplicationHandler ryeexApplicationHandler) {
    return new RyeexUserActivityHandler(dataConverter, ryeexApplicationHandler);
  }
}
