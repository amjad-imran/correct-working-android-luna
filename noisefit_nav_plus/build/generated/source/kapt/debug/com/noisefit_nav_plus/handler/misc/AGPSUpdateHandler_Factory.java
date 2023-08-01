package com.noisefit_nav_plus.handler.misc;

import com.noisefit_commans.NoisefitApplication;
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
public final class AGPSUpdateHandler_Factory implements Factory<AGPSUpdateHandler> {
  private final Provider<NoisefitApplication> contextProvider;

  public AGPSUpdateHandler_Factory(Provider<NoisefitApplication> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AGPSUpdateHandler get() {
    return newInstance(contextProvider.get());
  }

  public static AGPSUpdateHandler_Factory create(Provider<NoisefitApplication> contextProvider) {
    return new AGPSUpdateHandler_Factory(contextProvider);
  }

  public static AGPSUpdateHandler newInstance(NoisefitApplication context) {
    return new AGPSUpdateHandler(context);
  }
}
