package com.noisefit.colorfit_pro.dataConversion;

import android.content.Context;
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
public final class DataConverter_Factory implements Factory<DataConverter> {
  private final Provider<Context> contextProvider;

  public DataConverter_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DataConverter get() {
    return newInstance(contextProvider.get());
  }

  public static DataConverter_Factory create(Provider<Context> contextProvider) {
    return new DataConverter_Factory(contextProvider);
  }

  public static DataConverter newInstance(Context context) {
    return new DataConverter(context);
  }
}
