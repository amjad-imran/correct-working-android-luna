package com.noisefit_ryeex_sdk.di;

import android.content.Context;
import com.noisefit_ryeex_sdk.base.RyeexApplicationHandler;
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
public final class WatchModule_ProvideRyeexApplicationHandlerFactory implements Factory<RyeexApplicationHandler> {
  private final Provider<Context> contextProvider;

  public WatchModule_ProvideRyeexApplicationHandlerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public RyeexApplicationHandler get() {
    return provideRyeexApplicationHandler(contextProvider.get());
  }

  public static WatchModule_ProvideRyeexApplicationHandlerFactory create(
      Provider<Context> contextProvider) {
    return new WatchModule_ProvideRyeexApplicationHandlerFactory(contextProvider);
  }

  public static RyeexApplicationHandler provideRyeexApplicationHandler(Context context) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideRyeexApplicationHandler(context));
  }
}
