package com.noisefit.hybrid.di;

import com.noisefit.hybrid.utils.OnlineWatchFacesVision;
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
public final class WatchModule_ProvideOnlineWatchFacesVisionFactory implements Factory<OnlineWatchFacesVision> {
  @Override
  public OnlineWatchFacesVision get() {
    return provideOnlineWatchFacesVision();
  }

  public static WatchModule_ProvideOnlineWatchFacesVisionFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static OnlineWatchFacesVision provideOnlineWatchFacesVision() {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideOnlineWatchFacesVision());
  }

  private static final class InstanceHolder {
    private static final WatchModule_ProvideOnlineWatchFacesVisionFactory INSTANCE = new WatchModule_ProvideOnlineWatchFacesVisionFactory();
  }
}
