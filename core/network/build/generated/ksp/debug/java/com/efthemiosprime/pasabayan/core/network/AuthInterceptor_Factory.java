package com.efthemiosprime.pasabayan.core.network;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
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
public final class AuthInterceptor_Factory implements Factory<AuthInterceptor> {
  private final Provider<AuthTokenProvider> tokenProvider;

  public AuthInterceptor_Factory(Provider<AuthTokenProvider> tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  @Override
  public AuthInterceptor get() {
    return newInstance(tokenProvider.get());
  }

  public static AuthInterceptor_Factory create(Provider<AuthTokenProvider> tokenProvider) {
    return new AuthInterceptor_Factory(tokenProvider);
  }

  public static AuthInterceptor newInstance(AuthTokenProvider tokenProvider) {
    return new AuthInterceptor(tokenProvider);
  }
}
