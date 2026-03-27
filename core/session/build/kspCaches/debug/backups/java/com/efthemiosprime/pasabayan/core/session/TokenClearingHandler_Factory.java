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
public final class TokenClearingHandler_Factory implements Factory<TokenClearingHandler> {
  private final Provider<TokenStore> tokenStoreProvider;

  public TokenClearingHandler_Factory(Provider<TokenStore> tokenStoreProvider) {
    this.tokenStoreProvider = tokenStoreProvider;
  }

  @Override
  public TokenClearingHandler get() {
    return newInstance(tokenStoreProvider.get());
  }

  public static TokenClearingHandler_Factory create(Provider<TokenStore> tokenStoreProvider) {
    return new TokenClearingHandler_Factory(tokenStoreProvider);
  }

  public static TokenClearingHandler newInstance(TokenStore tokenStore) {
    return new TokenClearingHandler(tokenStore);
  }
}
