package com.noisefit_evolve2.di;

import android.content.Context;
import com.noisefit_evolve2.base.Evolve2ApplicationHandler;
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
public final class WatchModule_ProvideEvolve2ApplicationHandlerFactory implements Factory<Evolve2ApplicationHandler> {
  private final Provider<Context> contextProvider;

  public WatchModule_ProvideEvolve2ApplicationHandlerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public Evolve2ApplicationHandler get() {
    return provideEvolve2ApplicationHandler(contextProvider.get());
  }

  public static WatchModule_ProvideEvolve2ApplicationHandlerFactory create(
      Provider<Context> contextProvider) {
    return new WatchModule_ProvideEvolve2ApplicationHandlerFactory(contextProvider);
  }

  public static Evolve2ApplicationHandler provideEvolve2ApplicationHandler(Context context) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideEvolve2ApplicationHandler(context));
  }
}
