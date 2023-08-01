package com.noisefit.hybrid.di;

import com.noisefit.hybrid.dataconversions.DataConverter;
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
public final class WatchModule_ProvideNFHybridDataConverterFactory implements Factory<DataConverter> {
  @Override
  public DataConverter get() {
    return provideNFHybridDataConverter();
  }

  public static WatchModule_ProvideNFHybridDataConverterFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DataConverter provideNFHybridDataConverter() {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideNFHybridDataConverter());
  }

  private static final class InstanceHolder {
    private static final WatchModule_ProvideNFHybridDataConverterFactory INSTANCE = new WatchModule_ProvideNFHybridDataConverterFactory();
  }
}
