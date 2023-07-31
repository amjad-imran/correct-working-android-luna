package com.noisefit.hybrid.base;

import com.noisefit.hybrid.utils.BitwiseUtils;
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
public final class VisionCommands_Factory implements Factory<VisionCommands> {
  private final Provider<BitwiseUtils> bitwiseUtilsProvider;

  public VisionCommands_Factory(Provider<BitwiseUtils> bitwiseUtilsProvider) {
    this.bitwiseUtilsProvider = bitwiseUtilsProvider;
  }

  @Override
  public VisionCommands get() {
    return newInstance(bitwiseUtilsProvider.get());
  }

  public static VisionCommands_Factory create(Provider<BitwiseUtils> bitwiseUtilsProvider) {
    return new VisionCommands_Factory(bitwiseUtilsProvider);
  }

  public static VisionCommands newInstance(BitwiseUtils bitwiseUtils) {
    return new VisionCommands(bitwiseUtils);
  }
}
