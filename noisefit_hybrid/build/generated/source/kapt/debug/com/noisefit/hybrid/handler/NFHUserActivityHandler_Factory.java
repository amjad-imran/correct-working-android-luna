package com.noisefit.hybrid.handler;

import com.noisefit.hybrid.base.NFHybridApplicationHandler;
import com.noisefit.hybrid.dataconversions.DataConverter;
import com.noisefit.hybrid.utils.VisionHelperMethods;
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
public final class NFHUserActivityHandler_Factory implements Factory<NFHUserActivityHandler> {
  private final Provider<NFHybridApplicationHandler> nfhApplicationHandlerProvider;

  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<VisionHelperMethods> visionHelperMethodsProvider;

  public NFHUserActivityHandler_Factory(
      Provider<NFHybridApplicationHandler> nfhApplicationHandlerProvider,
      Provider<DataConverter> dataConverterProvider,
      Provider<VisionHelperMethods> visionHelperMethodsProvider) {
    this.nfhApplicationHandlerProvider = nfhApplicationHandlerProvider;
    this.dataConverterProvider = dataConverterProvider;
    this.visionHelperMethodsProvider = visionHelperMethodsProvider;
  }

  @Override
  public NFHUserActivityHandler get() {
    return newInstance(nfhApplicationHandlerProvider.get(), dataConverterProvider.get(), visionHelperMethodsProvider.get());
  }

  public static NFHUserActivityHandler_Factory create(
      Provider<NFHybridApplicationHandler> nfhApplicationHandlerProvider,
      Provider<DataConverter> dataConverterProvider,
      Provider<VisionHelperMethods> visionHelperMethodsProvider) {
    return new NFHUserActivityHandler_Factory(nfhApplicationHandlerProvider, dataConverterProvider, visionHelperMethodsProvider);
  }

  public static NFHUserActivityHandler newInstance(NFHybridApplicationHandler nfhApplicationHandler,
      DataConverter dataConverter, VisionHelperMethods visionHelperMethods) {
    return new NFHUserActivityHandler(nfhApplicationHandler, dataConverter, visionHelperMethods);
  }
}
