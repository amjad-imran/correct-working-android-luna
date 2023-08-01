package com.noisefit_cf2.di;

import com.noisefit_cf2.dataconversions.Colorfit2DataConverter;
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
public final class WatchModule_ProvideDataConverterFactory implements Factory<Colorfit2DataConverter> {
  @Override
  public Colorfit2DataConverter get() {
    return provideDataConverter();
  }

  public static WatchModule_ProvideDataConverterFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static Colorfit2DataConverter provideDataConverter() {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideDataConverter());
  }

  private static final class InstanceHolder {
    private static final WatchModule_ProvideDataConverterFactory INSTANCE = new WatchModule_ProvideDataConverterFactory();
  }
}
