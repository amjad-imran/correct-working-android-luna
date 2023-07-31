package com.noisefit_cf2.di;

import com.noisefit_cf2.base.ColorFit2ApplicationHandler;
import com.noisefit_cf2.dataconversions.Colorfit2DataConverter;
import com.noisefit_cf2.handler.CF2UserActivityDataHandler;
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
public final class WatchModule_ProvideCF2UserActivityDataHandlerFactory implements Factory<CF2UserActivityDataHandler> {
  private final Provider<Colorfit2DataConverter> dataConverterProvider;

  private final Provider<ColorFit2ApplicationHandler> colorFit2ApplicationHandlerProvider;

  public WatchModule_ProvideCF2UserActivityDataHandlerFactory(
      Provider<Colorfit2DataConverter> dataConverterProvider,
      Provider<ColorFit2ApplicationHandler> colorFit2ApplicationHandlerProvider) {
    this.dataConverterProvider = dataConverterProvider;
    this.colorFit2ApplicationHandlerProvider = colorFit2ApplicationHandlerProvider;
  }

  @Override
  public CF2UserActivityDataHandler get() {
    return provideCF2UserActivityDataHandler(dataConverterProvider.get(), colorFit2ApplicationHandlerProvider.get());
  }

  public static WatchModule_ProvideCF2UserActivityDataHandlerFactory create(
      Provider<Colorfit2DataConverter> dataConverterProvider,
      Provider<ColorFit2ApplicationHandler> colorFit2ApplicationHandlerProvider) {
    return new WatchModule_ProvideCF2UserActivityDataHandlerFactory(dataConverterProvider, colorFit2ApplicationHandlerProvider);
  }

  public static CF2UserActivityDataHandler provideCF2UserActivityDataHandler(
      Colorfit2DataConverter dataConverter,
      ColorFit2ApplicationHandler colorFit2ApplicationHandler) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideCF2UserActivityDataHandler(dataConverter, colorFit2ApplicationHandler));
  }
}
