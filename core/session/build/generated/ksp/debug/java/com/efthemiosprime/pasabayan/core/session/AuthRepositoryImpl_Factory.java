package com.efthemiosprime.pasabayan.core.session;

import com.efthemiosprime.pasabayan.core.network.auth.AuthApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.serialization.json.Json;

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
public final class AuthRepositoryImpl_Factory implements Factory<AuthRepositoryImpl> {
  private final Provider<AuthApi> authApiProvider;

  private final Provider<Json> jsonProvider;

  private final Provider<TokenStore> tokenStoreProvider;

  public AuthRepositoryImpl_Factory(Provider<AuthApi> authApiProvider, Provider<Json> jsonProvider,
      Provider<TokenStore> tokenStoreProvider) {
    this.authApiProvider = authApiProvider;
    this.jsonProvider = jsonProvider;
    this.tokenStoreProvider = tokenStoreProvider;
  }

  @Override
  public AuthRepositoryImpl get() {
    return newInstance(authApiProvider.get(), jsonProvider.get(), tokenStoreProvider.get());
  }

  public static AuthRepositoryImpl_Factory create(Provider<AuthApi> authApiProvider,
      Provider<Json> jsonProvider, Provider<TokenStore> tokenStoreProvider) {
    return new AuthRepositoryImpl_Factory(authApiProvider, jsonProvider, tokenStoreProvider);
  }

  public static AuthRepositoryImpl newInstance(AuthApi authApi, Json json, TokenStore tokenStore) {
    return new AuthRepositoryImpl(authApi, json, tokenStore);
  }
}
