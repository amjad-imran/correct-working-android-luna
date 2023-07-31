package com.noisefit.colorfit_pro.di;

import android.content.Context;
import com.noisefit.colorfit_pro.base.ProApplicationHandler;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class WatchModule_ProvideProApplicationHandlerFactory implements Factory<ProApplicationHandler> {
  private final Provider<Context> appContextProvider;

  public WatchModule_ProvideProApplicationHandlerFactory(Provider<Context> appContextProvider) {
    this.appContextProvider = appContextProvider;
  }

  @Override
  public ProApplicationHandler get() {
    return provideProApplicationHandler(appContextProvider.get());
  }

  public static WatchModule_ProvideProApplicationHandlerFactory create(
      Provider<Context> appContextProvider) {
    return new WatchModule_ProvideProApplicationHandlerFactory(appContextProvider);
  }

  public static ProApplicationHandler provideProApplicationHandler(Context appContext) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideProApplicationHandler(appContext));
  }
}
