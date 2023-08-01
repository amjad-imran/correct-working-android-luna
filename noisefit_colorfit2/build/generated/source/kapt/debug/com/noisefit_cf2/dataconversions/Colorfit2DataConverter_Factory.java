package com.noisefit_cf2.dataconversions;

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
public final class Colorfit2DataConverter_Factory implements Factory<Colorfit2DataConverter> {
  @Override
  public Colorfit2DataConverter get() {
    return newInstance();
  }

  public static Colorfit2DataConverter_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static Colorfit2DataConverter newInstance() {
    return new Colorfit2DataConverter();
  }

  private static final class InstanceHolder {
    private static final Colorfit2DataConverter_Factory INSTANCE = new Colorfit2DataConverter_Factory();
  }
}
