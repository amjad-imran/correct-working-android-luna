package com.noisefit.hybrid.di;

import com.noisefit.hybrid.base.VisionCommands;
import com.noisefit.hybrid.utils.BitwiseHelperUtils;
import com.noisefit.hybrid.utils.VisionHelperMethods;
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
public final class WatchModule_ProvideVisionHelperMethodsFactory implements Factory<VisionHelperMethods> {
  private final Provider<BitwiseHelperUtils> bitwiseHelperUtilsProvider;

  private final Provider<VisionCommands> visionCommandsProvider;

  public WatchModule_ProvideVisionHelperMethodsFactory(
      Provider<BitwiseHelperUtils> bitwiseHelperUtilsProvider,
      Provider<VisionCommands> visionCommandsProvider) {
    this.bitwiseHelperUtilsProvider = bitwiseHelperUtilsProvider;
    this.visionCommandsProvider = visionCommandsProvider;
  }

  @Override
  public VisionHelperMethods get() {
    return provideVisionHelperMethods(bitwiseHelperUtilsProvider.get(), visionCommandsProvider.get());
  }

  public static WatchModule_ProvideVisionHelperMethodsFactory create(
      Provider<BitwiseHelperUtils> bitwiseHelperUtilsProvider,
      Provider<VisionCommands> visionCommandsProvider) {
    return new WatchModule_ProvideVisionHelperMethodsFactory(bitwiseHelperUtilsProvider, visionCommandsProvider);
  }

  public static VisionHelperMethods provideVisionHelperMethods(
      BitwiseHelperUtils bitwiseHelperUtils, VisionCommands visionCommands) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideVisionHelperMethods(bitwiseHelperUtils, visionCommands));
  }
}
