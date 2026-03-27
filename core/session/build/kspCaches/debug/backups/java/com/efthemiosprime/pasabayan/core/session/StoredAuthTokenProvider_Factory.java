package com.efthemiosprime.pasabayan.core.session;

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
public final class StoredAuthTokenProvider_Factory implements Factory<StoredAuthTokenProvider> {
  private final Provider<TokenStore> tokenStoreProvider;

  public StoredAuthTokenProvider_Factory(Provider<TokenStore> tokenStoreProvider) {
    this.tokenStoreProvider = tokenStoreProvider;
  }

  @Override
  public StoredAuthTokenProvider get() {
    return newInstance(tokenStoreProvider.get());
  }

  public static StoredAuthTokenProvider_Factory create(Provider<TokenStore> tokenStoreProvider) {
    return new StoredAuthTokenProvider_Factory(tokenStoreProvider);
  }

  public static StoredAuthTokenProvider newInstance(TokenStore tokenStore) {
    return new StoredAuthTokenProvider(tokenStore);
  }
}
