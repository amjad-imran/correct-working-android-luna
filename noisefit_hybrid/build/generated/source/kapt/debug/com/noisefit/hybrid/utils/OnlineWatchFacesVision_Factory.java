package com.noisefit.hybrid.utils;

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
public final class OnlineWatchFacesVision_Factory implements Factory<OnlineWatchFacesVision> {
  @Override
  public OnlineWatchFacesVision get() {
    return newInstance();
  }

  public static OnlineWatchFacesVision_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static OnlineWatchFacesVision newInstance() {
    return new OnlineWatchFacesVision();
  }

  private static final class InstanceHolder {
    private static final OnlineWatchFacesVision_Factory INSTANCE = new OnlineWatchFacesVision_Factory();
  }
}
