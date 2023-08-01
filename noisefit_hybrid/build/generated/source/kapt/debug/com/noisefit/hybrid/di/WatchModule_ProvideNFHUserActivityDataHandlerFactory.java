package com.noisefit.hybrid.di;

import com.noisefit.hybrid.base.NFHybridApplicationHandler;
import com.noisefit.hybrid.dataconversions.DataConverter;
import com.noisefit.hybrid.handler.NFHUserActivityHandler;
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
public final class WatchModule_ProvideNFHUserActivityDataHandlerFactory implements Factory<NFHUserActivityHandler> {
  private final Provider<NFHybridApplicationHandler> applicationHandlerProvider;

  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<VisionHelperMethods> visionHelperMethodsProvider;

  public WatchModule_ProvideNFHUserActivityDataHandlerFactory(
      Provider<NFHybridApplicationHandler> applicationHandlerProvider,
      Provider<DataConverter> dataConverterProvider,
      Provider<VisionHelperMethods> visionHelperMethodsProvider) {
    this.applicationHandlerProvider = applicationHandlerProvider;
    this.dataConverterProvider = dataConverterProvider;
    this.visionHelperMethodsProvider = visionHelperMethodsProvider;
  }

  @Override
  public NFHUserActivityHandler get() {
    return provideNFHUserActivityDataHandler(applicationHandlerProvider.get(), dataConverterProvider.get(), visionHelperMethodsProvider.get());
  }

  public static WatchModule_ProvideNFHUserActivityDataHandlerFactory create(
      Provider<NFHybridApplicationHandler> applicationHandlerProvider,
      Provider<DataConverter> dataConverterProvider,
      Provider<VisionHelperMethods> visionHelperMethodsProvider) {
    return new WatchModule_ProvideNFHUserActivityDataHandlerFactory(applicationHandlerProvider, dataConverterProvider, visionHelperMethodsProvider);
  }

  public static NFHUserActivityHandler provideNFHUserActivityDataHandler(
      NFHybridApplicationHandler applicationHandler, DataConverter dataConverter,
      VisionHelperMethods visionHelperMethods) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideNFHUserActivityDataHandler(applicationHandler, dataConverter, visionHelperMethods));
  }
}
