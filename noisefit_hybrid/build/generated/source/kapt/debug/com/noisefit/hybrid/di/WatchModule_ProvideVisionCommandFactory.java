package com.noisefit.hybrid.di;

import com.noisefit.hybrid.base.VisionCommands;
import com.noisefit.hybrid.utils.BitwiseUtils;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class WatchModule_ProvideVisionCommandFactory implements Factory<VisionCommands> {
  private final Provider<BitwiseUtils> bitwiseUtilsProvider;

  public WatchModule_ProvideVisionCommandFactory(Provider<BitwiseUtils> bitwiseUtilsProvider) {
    this.bitwiseUtilsProvider = bitwiseUtilsProvider;
  }

  @Override
  public VisionCommands get() {
    return provideVisionCommand(bitwiseUtilsProvider.get());
  }

  public static WatchModule_ProvideVisionCommandFactory create(
      Provider<BitwiseUtils> bitwiseUtilsProvider) {
    return new WatchModule_ProvideVisionCommandFactory(bitwiseUtilsProvider);
  }

  public static VisionCommands provideVisionCommand(BitwiseUtils bitwiseUtils) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideVisionCommand(bitwiseUtils));
  }
}
