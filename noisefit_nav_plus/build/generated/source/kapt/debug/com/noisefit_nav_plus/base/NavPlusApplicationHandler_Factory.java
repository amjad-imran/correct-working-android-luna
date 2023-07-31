package com.noisefit_nav_plus.base;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class NavPlusApplicationHandler_Factory implements Factory<NavPlusApplicationHandler> {
  @Override
  public NavPlusApplicationHandler get() {
    return newInstance();
  }

  public static NavPlusApplicationHandler_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NavPlusApplicationHandler newInstance() {
    return new NavPlusApplicationHandler();
  }

  private static final class InstanceHolder {
    private static final NavPlusApplicationHandler_Factory INSTANCE = new NavPlusApplicationHandler_Factory();
  }
}
