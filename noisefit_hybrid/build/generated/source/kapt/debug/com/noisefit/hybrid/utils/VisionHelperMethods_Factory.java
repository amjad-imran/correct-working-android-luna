package com.noisefit.hybrid.utils;

import com.noisefit.hybrid.base.VisionCommands;
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
public final class VisionHelperMethods_Factory implements Factory<VisionHelperMethods> {
  private final Provider<BitwiseHelperUtils> bitwiseHelperUtilsProvider;

  private final Provider<VisionCommands> visionCommandsProvider;

  public VisionHelperMethods_Factory(Provider<BitwiseHelperUtils> bitwiseHelperUtilsProvider,
      Provider<VisionCommands> visionCommandsProvider) {
    this.bitwiseHelperUtilsProvider = bitwiseHelperUtilsProvider;
    this.visionCommandsProvider = visionCommandsProvider;
  }

  @Override
  public VisionHelperMethods get() {
    return newInstance(bitwiseHelperUtilsProvider.get(), visionCommandsProvider.get());
  }

  public static VisionHelperMethods_Factory create(
      Provider<BitwiseHelperUtils> bitwiseHelperUtilsProvider,
      Provider<VisionCommands> visionCommandsProvider) {
    return new VisionHelperMethods_Factory(bitwiseHelperUtilsProvider, visionCommandsProvider);
  }

  public static VisionHelperMethods newInstance(BitwiseHelperUtils bitwiseHelperUtils,
      VisionCommands visionCommands) {
    return new VisionHelperMethods(bitwiseHelperUtils, visionCommands);
  }
}
