package com.noisefit_nav_plus.di;

import com.noisefit_nav_plus.base.NavPlusApplicationHandler;
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
public final class WatchModule_ProvideNavPlusApplicationHandlerFactory implements Factory<NavPlusApplicationHandler> {
  @Override
  public NavPlusApplicationHandler get() {
    return provideNavPlusApplicationHandler();
  }

  public static WatchModule_ProvideNavPlusApplicationHandlerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NavPlusApplicationHandler provideNavPlusApplicationHandler() {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideNavPlusApplicationHandler());
  }

  private static final class InstanceHolder {
    private static final WatchModule_ProvideNavPlusApplicationHandlerFactory INSTANCE = new WatchModule_ProvideNavPlusApplicationHandlerFactory();
  }
}
