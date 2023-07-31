package com.noisefit_cf2.handler;

import com.noisefit_cf2.base.ColorFit2ApplicationHandler;
import com.noisefit_cf2.dataconversions.Colorfit2DataConverter;
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
public final class CF2UserActivityDataHandler_Factory implements Factory<CF2UserActivityDataHandler> {
  private final Provider<Colorfit2DataConverter> dataConverterProvider;

  private final Provider<ColorFit2ApplicationHandler> colorFit2ApplicationHandlerProvider;

  public CF2UserActivityDataHandler_Factory(Provider<Colorfit2DataConverter> dataConverterProvider,
      Provider<ColorFit2ApplicationHandler> colorFit2ApplicationHandlerProvider) {
    this.dataConverterProvider = dataConverterProvider;
    this.colorFit2ApplicationHandlerProvider = colorFit2ApplicationHandlerProvider;
  }

  @Override
  public CF2UserActivityDataHandler get() {
    return newInstance(dataConverterProvider.get(), colorFit2ApplicationHandlerProvider.get());
  }

  public static CF2UserActivityDataHandler_Factory create(
      Provider<Colorfit2DataConverter> dataConverterProvider,
      Provider<ColorFit2ApplicationHandler> colorFit2ApplicationHandlerProvider) {
    return new CF2UserActivityDataHandler_Factory(dataConverterProvider, colorFit2ApplicationHandlerProvider);
  }

  public static CF2UserActivityDataHandler newInstance(Colorfit2DataConverter dataConverter,
      ColorFit2ApplicationHandler colorFit2ApplicationHandler) {
    return new CF2UserActivityDataHandler(dataConverter, colorFit2ApplicationHandler);
  }
}
