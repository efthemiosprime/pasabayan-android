package com.efthemiosprime.pasabayan.core.network.di;

import com.efthemiosprime.pasabayan.core.network.AuthTokenProvider;
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
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class NetworkModule_ProvideAuthTokenProviderFactory implements Factory<AuthTokenProvider> {
  @Override
  public AuthTokenProvider get() {
    return provideAuthTokenProvider();
  }

  public static NetworkModule_ProvideAuthTokenProviderFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AuthTokenProvider provideAuthTokenProvider() {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideAuthTokenProvider());
  }

  private static final class InstanceHolder {
    static final NetworkModule_ProvideAuthTokenProviderFactory INSTANCE = new NetworkModule_ProvideAuthTokenProviderFactory();
  }
}
