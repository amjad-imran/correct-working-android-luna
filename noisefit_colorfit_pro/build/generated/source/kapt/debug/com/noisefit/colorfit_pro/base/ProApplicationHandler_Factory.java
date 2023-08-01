package com.noisefit.colorfit_pro.base;

import android.content.Context;
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
public final class ProApplicationHandler_Factory implements Factory<ProApplicationHandler> {
  private final Provider<Context> appContextProvider;

  public ProApplicationHandler_Factory(Provider<Context> appContextProvider) {
    this.appContextProvider = appContextProvider;
  }

  @Override
  public ProApplicationHandler get() {
    return newInstance(appContextProvider.get());
  }

  public static ProApplicationHandler_Factory create(Provider<Context> appContextProvider) {
    return new ProApplicationHandler_Factory(appContextProvider);
  }

  public static ProApplicationHandler newInstance(Context appContext) {
    return new ProApplicationHandler(appContext);
  }
}
