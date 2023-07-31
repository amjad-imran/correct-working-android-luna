package com.noisefit.hybrid.di;

import com.noisefit.hybrid.utils.BitwiseUtils;
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
public final class WatchModule_ProvideBitwiseUtilsFactory implements Factory<BitwiseUtils> {
  @Override
  public BitwiseUtils get() {
    return provideBitwiseUtils();
  }

  public static WatchModule_ProvideBitwiseUtilsFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static BitwiseUtils provideBitwiseUtils() {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideBitwiseUtils());
  }

  private static final class InstanceHolder {
    private static final WatchModule_ProvideBitwiseUtilsFactory INSTANCE = new WatchModule_ProvideBitwiseUtilsFactory();
  }
}
