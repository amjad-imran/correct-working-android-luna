package com.noisefit_evolve2.di;

import com.noisefit_evolve2.base.Evolve2ApplicationHandler;
import com.noisefit_evolve2.dataConversion.DataConverter;
import com.noisefit_evolve2.handler.Evolve2UserActivityHandler;
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
public final class WatchModule_ProvideEvolve2UserActivityHandlerFactory implements Factory<Evolve2UserActivityHandler> {
  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<Evolve2ApplicationHandler> evolveApplicationHandlerProvider;

  public WatchModule_ProvideEvolve2UserActivityHandlerFactory(
      Provider<DataConverter> dataConverterProvider,
      Provider<Evolve2ApplicationHandler> evolveApplicationHandlerProvider) {
    this.dataConverterProvider = dataConverterProvider;
    this.evolveApplicationHandlerProvider = evolveApplicationHandlerProvider;
  }

  @Override
  public Evolve2UserActivityHandler get() {
    return provideEvolve2UserActivityHandler(dataConverterProvider.get(), evolveApplicationHandlerProvider.get());
  }

  public static WatchModule_ProvideEvolve2UserActivityHandlerFactory create(
      Provider<DataConverter> dataConverterProvider,
      Provider<Evolve2ApplicationHandler> evolveApplicationHandlerProvider) {
    return new WatchModule_ProvideEvolve2UserActivityHandlerFactory(dataConverterProvider, evolveApplicationHandlerProvider);
  }

  public static Evolve2UserActivityHandler provideEvolve2UserActivityHandler(
      DataConverter dataConverter, Evolve2ApplicationHandler evolveApplicationHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideEvolve2UserActivityHandler(dataConverter, evolveApplicationHandler));
  }
}
