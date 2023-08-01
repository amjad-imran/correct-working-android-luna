package com.noisefit.hybrid.di;

import com.noisefit.hybrid.base.NFHybridApplicationHandler;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class WatchModule_ProvideApplicationHandlerFactory implements Factory<NFHybridApplicationHandler> {
  @Override
  public NFHybridApplicationHandler get() {
    return provideApplicationHandler();
  }

  public static WatchModule_ProvideApplicationHandlerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NFHybridApplicationHandler provideApplicationHandler() {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideApplicationHandler());
  }

  private static final class InstanceHolder {
    private static final WatchModule_ProvideApplicationHandlerFactory INSTANCE = new WatchModule_ProvideApplicationHandlerFactory();
  }
}
