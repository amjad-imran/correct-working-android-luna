package com.noisefit.colorfit_pro.handler.connect;

import com.noisefit.colorfit_pro.base.ProApplicationHandler;
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
public final class ProConnectHandler_Factory implements Factory<ProConnectHandler> {
  private final Provider<ProApplicationHandler> proApplicationHandlerProvider;

  public ProConnectHandler_Factory(Provider<ProApplicationHandler> proApplicationHandlerProvider) {
    this.proApplicationHandlerProvider = proApplicationHandlerProvider;
  }

  @Override
  public ProConnectHandler get() {
    return newInstance(proApplicationHandlerProvider.get());
  }

  public static ProConnectHandler_Factory create(
      Provider<ProApplicationHandler> proApplicationHandlerProvider) {
    return new ProConnectHandler_Factory(proApplicationHandlerProvider);
  }

  public static ProConnectHandler newInstance(ProApplicationHandler proApplicationHandler) {
    return new ProConnectHandler(proApplicationHandler);
  }
}
