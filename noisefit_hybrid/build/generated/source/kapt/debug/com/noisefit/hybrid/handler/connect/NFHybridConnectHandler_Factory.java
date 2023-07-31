package com.noisefit.hybrid.handler.connect;

import com.noisefit.hybrid.base.NFHybridApplicationHandler;
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
public final class NFHybridConnectHandler_Factory implements Factory<NFHybridConnectHandler> {
  private final Provider<NFHybridApplicationHandler> applicationHandlerProvider;

  public NFHybridConnectHandler_Factory(
      Provider<NFHybridApplicationHandler> applicationHandlerProvider) {
    this.applicationHandlerProvider = applicationHandlerProvider;
  }

  @Override
  public NFHybridConnectHandler get() {
    return newInstance(applicationHandlerProvider.get());
  }

  public static NFHybridConnectHandler_Factory create(
      Provider<NFHybridApplicationHandler> applicationHandlerProvider) {
    return new NFHybridConnectHandler_Factory(applicationHandlerProvider);
  }

  public static NFHybridConnectHandler newInstance(NFHybridApplicationHandler applicationHandler) {
    return new NFHybridConnectHandler(applicationHandler);
  }
}
