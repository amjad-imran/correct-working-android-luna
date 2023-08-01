package com.noisefit_evolve2.handler;

import com.noisefit_evolve2.base.Evolve2ApplicationHandler;
import com.noisefit_evolve2.dataConversion.DataConverter;
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
public final class Evolve2UserActivityHandler_Factory implements Factory<Evolve2UserActivityHandler> {
  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<Evolve2ApplicationHandler> evolve2ApplicationHandlerProvider;

  public Evolve2UserActivityHandler_Factory(Provider<DataConverter> dataConverterProvider,
      Provider<Evolve2ApplicationHandler> evolve2ApplicationHandlerProvider) {
    this.dataConverterProvider = dataConverterProvider;
    this.evolve2ApplicationHandlerProvider = evolve2ApplicationHandlerProvider;
  }

  @Override
  public Evolve2UserActivityHandler get() {
    return newInstance(dataConverterProvider.get(), evolve2ApplicationHandlerProvider.get());
  }

  public static Evolve2UserActivityHandler_Factory create(
      Provider<DataConverter> dataConverterProvider,
      Provider<Evolve2ApplicationHandler> evolve2ApplicationHandlerProvider) {
    return new Evolve2UserActivityHandler_Factory(dataConverterProvider, evolve2ApplicationHandlerProvider);
  }

  public static Evolve2UserActivityHandler newInstance(DataConverter dataConverter,
      Evolve2ApplicationHandler evolve2ApplicationHandler) {
    return new Evolve2UserActivityHandler(dataConverter, evolve2ApplicationHandler);
  }
}
