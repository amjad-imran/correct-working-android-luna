package com.noisefit_evolve2.di;

import android.content.Context;
import com.google.gson.Gson;
import com.noisefit_evolve2.dataConversion.DataConverter;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class WatchModule_ProvideDataConverterFactory implements Factory<DataConverter> {
  private final Provider<Context> appContextProvider;

  private final Provider<Gson> gsonProvider;

  public WatchModule_ProvideDataConverterFactory(Provider<Context> appContextProvider,
      Provider<Gson> gsonProvider) {
    this.appContextProvider = appContextProvider;
    this.gsonProvider = gsonProvider;
  }

  @Override
  public DataConverter get() {
    return provideDataConverter(appContextProvider.get(), gsonProvider.get());
  }

  public static WatchModule_ProvideDataConverterFactory create(Provider<Context> appContextProvider,
      Provider<Gson> gsonProvider) {
    return new WatchModule_ProvideDataConverterFactory(appContextProvider, gsonProvider);
  }

  public static DataConverter provideDataConverter(Context appContext, Gson gson) {
    return Preconditions.checkNotNullFromProvides(WatchModule.INSTANCE.provideDataConverter(appContext, gson));
  }
}
