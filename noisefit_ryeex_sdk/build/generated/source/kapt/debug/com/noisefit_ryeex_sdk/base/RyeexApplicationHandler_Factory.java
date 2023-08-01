package com.noisefit_ryeex_sdk.base;

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
public final class RyeexApplicationHandler_Factory implements Factory<RyeexApplicationHandler> {
  private final Provider<Context> contextProvider;

  public RyeexApplicationHandler_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public RyeexApplicationHandler get() {
    return newInstance(contextProvider.get());
  }

  public static RyeexApplicationHandler_Factory create(Provider<Context> contextProvider) {
    return new RyeexApplicationHandler_Factory(contextProvider);
  }

  public static RyeexApplicationHandler newInstance(Context context) {
    return new RyeexApplicationHandler(context);
  }
}
