package com.noisefit.hybrid.utils;

import com.noisefit.hybrid.base.VisionCommands;
import com.noisefit.hybrid.dataconversions.DataConverter;
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
public final class BitwiseHelperUtils_Factory implements Factory<BitwiseHelperUtils> {
  private final Provider<BitwiseUtils> bitwiseUtilsProvider;

  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<VisionCommands> visionCommandsProvider;

  public BitwiseHelperUtils_Factory(Provider<BitwiseUtils> bitwiseUtilsProvider,
      Provider<DataConverter> dataConverterProvider,
      Provider<VisionCommands> visionCommandsProvider) {
    this.bitwiseUtilsProvider = bitwiseUtilsProvider;
    this.dataConverterProvider = dataConverterProvider;
    this.visionCommandsProvider = visionCommandsProvider;
  }

  @Override
  public BitwiseHelperUtils get() {
    return newInstance(bitwiseUtilsProvider.get(), dataConverterProvider.get(), visionCommandsProvider.get());
  }

  public static BitwiseHelperUtils_Factory create(Provider<BitwiseUtils> bitwiseUtilsProvider,
      Provider<DataConverter> dataConverterProvider,
      Provider<VisionCommands> visionCommandsProvider) {
    return new BitwiseHelperUtils_Factory(bitwiseUtilsProvider, dataConverterProvider, visionCommandsProvider);
  }

  public static BitwiseHelperUtils newInstance(BitwiseUtils bitwiseUtils,
      DataConverter dataConverter, VisionCommands visionCommands) {
    return new BitwiseHelperUtils(bitwiseUtils, dataConverter, visionCommands);
  }
}
