package com.noisefit_evolve2.base;

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
public final class Evolve2ApplicationHandler_Factory implements Factory<Evolve2ApplicationHandler> {
  private final Provider<Context> contextProvider;

  public Evolve2ApplicationHandler_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public Evolve2ApplicationHandler get() {
    return newInstance(contextProvider.get());
  }

  public static Evolve2ApplicationHandler_Factory create(Provider<Context> contextProvider) {
    return new Evolve2ApplicationHandler_Factory(contextProvider);
  }

  public static Evolve2ApplicationHandler newInstance(Context context) {
    return new Evolve2ApplicationHandler(context);
  }
}
