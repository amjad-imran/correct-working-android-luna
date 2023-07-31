package com.noisefit.hybrid.di;

import com.noisefit.hybrid.base.VisionCommands;
import com.noisefit.hybrid.dataconversions.DataConverter;
import com.noisefit.hybrid.utils.BitwiseHelperUtils;
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
public final class WatchModule_ProvideBitwiseHelperUtilsFactory implements Factory<BitwiseHelperUtils> {
  private final Provider<BitwiseUtils> bitwiseUtilsProvider;

  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<VisionCommands> visionCommandsProvider;

  public WatchModule_ProvideBitwiseHelperUtilsFactory(Provider<BitwiseUtils> bitwiseUtilsProvider,
      Provider<DataConverter> dataConverterProvider,
      Provider<VisionCommands> visionCommandsProvider) {
    this.bitwiseUtilsProvider = bitwiseUtilsProvider;
    this.dataConverterProvider = dataConverterProvider;
    this.visionCommandsProvider = visionCommandsProvider;
  }

  @Override
  public BitwiseHelperUtils get() {
    return provideBitwiseHelperUtils(bitwiseUtilsProvider.get(), dataConverterProvider.get(), visionCommandsProvider.get());
  }

  public static WatchModule_ProvideBitwiseHelperUtilsFactory create(
      Provider<BitwiseUtils> bitwiseUtilsProvider, Provider<DataConverter> dataConverterProvider,
      Provider<VisionCommands> visionCommandsProvider) {
    return new WatchModule_ProvideBitwiseHelperUtilsFactory(bitwiseUtilsProvider, dataConverterProvider, visionCommandsProvider);
  }

  public static BitwiseHelperUtils provideBitwiseHelperUtils(BitwiseUtils bitwiseUtils,
      DataConverter dataConverter, VisionCommands visionCommands) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideBitwiseHelperUtils(bitwiseUtils, dataConverter, visionCommands));
  }
}
