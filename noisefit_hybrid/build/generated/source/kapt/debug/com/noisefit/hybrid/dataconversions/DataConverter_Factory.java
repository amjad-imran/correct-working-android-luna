package com.noisefit.hybrid.dataconversions;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class DataConverter_Factory implements Factory<DataConverter> {
  @Override
  public DataConverter get() {
    return newInstance();
  }

  public static DataConverter_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DataConverter newInstance() {
    return new DataConverter();
  }

  private static final class InstanceHolder {
    private static final DataConverter_Factory INSTANCE = new DataConverter_Factory();
  }
}
