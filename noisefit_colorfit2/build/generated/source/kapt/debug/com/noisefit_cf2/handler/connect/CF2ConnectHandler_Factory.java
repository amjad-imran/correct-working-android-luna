package com.noisefit_cf2.handler.connect;

import com.noisefit_cf2.base.ColorFit2ApplicationHandler;
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
public final class CF2ConnectHandler_Factory implements Factory<CF2ConnectHandler> {
  private final Provider<ColorFit2ApplicationHandler> applicationHandlerProvider;

  public CF2ConnectHandler_Factory(
      Provider<ColorFit2ApplicationHandler> applicationHandlerProvider) {
    this.applicationHandlerProvider = applicationHandlerProvider;
  }

  @Override
  public CF2ConnectHandler get() {
    return newInstance(applicationHandlerProvider.get());
  }

  public static CF2ConnectHandler_Factory create(
      Provider<ColorFit2ApplicationHandler> applicationHandlerProvider) {
    return new CF2ConnectHandler_Factory(applicationHandlerProvider);
  }

  public static CF2ConnectHandler newInstance(ColorFit2ApplicationHandler applicationHandler) {
    return new CF2ConnectHandler(applicationHandler);
  }
}
