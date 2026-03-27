package com.efthemiosprime.pasabayan.core.network;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
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
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class UnauthorizedClearingInterceptor_Factory implements Factory<UnauthorizedClearingInterceptor> {
  private final Provider<SessionInvalidationHandler> handlerProvider;

  public UnauthorizedClearingInterceptor_Factory(
      Provider<SessionInvalidationHandler> handlerProvider) {
    this.handlerProvider = handlerProvider;
  }

  @Override
  public UnauthorizedClearingInterceptor get() {
    return newInstance(handlerProvider.get());
  }

  public static UnauthorizedClearingInterceptor_Factory create(
      Provider<SessionInvalidationHandler> handlerProvider) {
    return new UnauthorizedClearingInterceptor_Factory(handlerProvider);
  }

  public static UnauthorizedClearingInterceptor newInstance(SessionInvalidationHandler handler) {
    return new UnauthorizedClearingInterceptor(handler);
  }
}
