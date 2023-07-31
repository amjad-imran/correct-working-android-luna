package com.noisefit.colorfit_pro.handler;

import com.noisefit.colorfit_pro.base.ProApplicationHandler;
import com.noisefit.colorfit_pro.dataConversion.DataConverter;
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
public final class ProUserActivityHandler_Factory implements Factory<ProUserActivityHandler> {
  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<ProApplicationHandler> navPlusApplicationHandlerProvider;

  public ProUserActivityHandler_Factory(Provider<DataConverter> dataConverterProvider,
      Provider<ProApplicationHandler> navPlusApplicationHandlerProvider) {
    this.dataConverterProvider = dataConverterProvider;
    this.navPlusApplicationHandlerProvider = navPlusApplicationHandlerProvider;
  }

  @Override
  public ProUserActivityHandler get() {
    return newInstance(dataConverterProvider.get(), navPlusApplicationHandlerProvider.get());
  }

  public static ProUserActivityHandler_Factory create(Provider<DataConverter> dataConverterProvider,
      Provider<ProApplicationHandler> navPlusApplicationHandlerProvider) {
    return new ProUserActivityHandler_Factory(dataConverterProvider, navPlusApplicationHandlerProvider);
  }

  public static ProUserActivityHandler newInstance(DataConverter dataConverter,
      ProApplicationHandler navPlusApplicationHandler) {
    return new ProUserActivityHandler(dataConverter, navPlusApplicationHandler);
  }
}
