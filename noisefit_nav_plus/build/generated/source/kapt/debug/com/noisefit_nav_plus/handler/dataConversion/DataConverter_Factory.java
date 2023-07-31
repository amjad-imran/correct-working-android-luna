package com.noisefit_nav_plus.handler.dataConversion;

import android.content.Context;
import com.google.gson.Gson;
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

  private final Provider<Gson> gsonProvider;

  public DataConverter_Factory(Provider<Context> contextProvider, Provider<Gson> gsonProvider) {
    this.contextProvider = contextProvider;
    this.gsonProvider = gsonProvider;
  }

  @Override
  public DataConverter get() {
    return newInstance(contextProvider.get(), gsonProvider.get());
  }

  public static DataConverter_Factory create(Provider<Context> contextProvider,
      Provider<Gson> gsonProvider) {
    return new DataConverter_Factory(contextProvider, gsonProvider);
  }

  public static DataConverter newInstance(Context context, Gson gson) {
    return new DataConverter(context, gson);
  }
}
