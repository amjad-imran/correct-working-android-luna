package com.noisefit.hybrid.utils;

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
public final class BitwiseUtils_Factory implements Factory<BitwiseUtils> {
  @Override
  public BitwiseUtils get() {
    return newInstance();
  }

  public static BitwiseUtils_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static BitwiseUtils newInstance() {
    return new BitwiseUtils();
  }

  private static final class InstanceHolder {
    private static final BitwiseUtils_Factory INSTANCE = new BitwiseUtils_Factory();
  }
}
