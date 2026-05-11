package com.efthemiosprime.pasabayan.core.network.di

import com.efthemiosprime.pasabayan.core.network.AuthInterceptor
import com.efthemiosprime.pasabayan.core.network.UnauthorizedClearingInterceptor
import com.efthemiosprime.pasabayan.core.network.BuildConfig
import com.efthemiosprime.pasabayan.core.network.SupplementalApi
import com.efthemiosprime.pasabayan.core.network.profile.ProfileApi
import com.efthemiosprime.pasabayan.core.network.auth.AuthApi
import com.efthemiosprime.pasabayan.core.network.bookings.BookingsApi
import com.efthemiosprime.pasabayan.core.network.chat.ChatApi
import com.efthemiosprime.pasabayan.core.network.packages.PackagesApi
import com.efthemiosprime.pasabayan.core.network.payments.PaymentApi
import com.efthemiosprime.pasabayan.core.network.payments.PaymentMethodsApi
import com.efthemiosprime.pasabayan.core.network.payments.ReceiptApi
import com.efthemiosprime.pasabayan.core.network.payments.StripeConfigApi
import com.efthemiosprime.pasabayan.core.network.payments.StripeConnectApi
import com.efthemiosprime.pasabayan.core.network.trips.TripsApi
import com.efthemiosprime.pasabayan.core.network.favorites.FavoritesApi
import com.efthemiosprime.pasabayan.core.network.legal.LegalApi
import com.efthemiosprime.pasabayan.core.network.location.LocationCatalogApi
import com.efthemiosprime.pasabayan.core.network.notifications.NotificationApi
import com.efthemiosprime.pasabayan.core.network.ratings.RatingsApi
import com.efthemiosprime.pasabayan.core.network.shipper.ShipperApi
import com.efthemiosprime.pasabayan.core.network.support.SupportApi
import com.efthemiosprime.pasabayan.core.network.system.SystemApi
import com.efthemiosprime.pasabayan.core.network.verification.VerificationApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        unauthorizedClearingInterceptor: UnauthorizedClearingInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(unauthorizedClearingInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit {
        val base = BuildConfig.API_BASE_URL.trimEnd('/') + "/"
        return Retrofit.Builder()
            .baseUrl(base)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideSupplementalApi(retrofit: Retrofit): SupplementalApi =
        retrofit.create(SupplementalApi::class.java)

    @Provides
    @Singleton
    fun provideProfileApi(retrofit: Retrofit): ProfileApi =
        retrofit.create(ProfileApi::class.java)

    @Provides
    @Singleton
    fun provideTripsApi(retrofit: Retrofit): TripsApi =
        retrofit.create(TripsApi::class.java)

    @Provides
    @Singleton
    fun providePackagesApi(retrofit: Retrofit): PackagesApi =
        retrofit.create(PackagesApi::class.java)

    @Provides
    @Singleton
    fun provideBookingsApi(retrofit: Retrofit): BookingsApi =
        retrofit.create(BookingsApi::class.java)

    @Provides
    @Singleton
    fun provideChatApi(retrofit: Retrofit): ChatApi =
        retrofit.create(ChatApi::class.java)

    @Provides
    @Singleton
    fun providePaymentApi(retrofit: Retrofit): PaymentApi =
        retrofit.create(PaymentApi::class.java)

    @Provides
    @Singleton
    fun provideVerificationApi(retrofit: Retrofit): VerificationApi =
        retrofit.create(VerificationApi::class.java)

    @Provides
    @Singleton
    fun provideFavoritesApi(retrofit: Retrofit): FavoritesApi =
        retrofit.create(FavoritesApi::class.java)

    @Provides
    @Singleton
    fun provideRatingsApi(retrofit: Retrofit): RatingsApi =
        retrofit.create(RatingsApi::class.java)

    @Provides
    @Singleton
    fun provideStripeConfigApi(retrofit: Retrofit): StripeConfigApi =
        retrofit.create(StripeConfigApi::class.java)

    @Provides
    @Singleton
    fun provideStripeConnectApi(retrofit: Retrofit): StripeConnectApi =
        retrofit.create(StripeConnectApi::class.java)

    @Provides
    @Singleton
    fun providePaymentMethodsApi(retrofit: Retrofit): PaymentMethodsApi =
        retrofit.create(PaymentMethodsApi::class.java)

    @Provides
    @Singleton
    fun provideReceiptApi(retrofit: Retrofit): ReceiptApi =
        retrofit.create(ReceiptApi::class.java)

    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi =
        retrofit.create(NotificationApi::class.java)

    @Provides
    @Singleton
    fun provideLegalApi(retrofit: Retrofit): LegalApi = retrofit.create(LegalApi::class.java)

    @Provides
    @Singleton
    fun provideSupportApi(retrofit: Retrofit): SupportApi = retrofit.create(SupportApi::class.java)

    @Provides
    @Singleton
    fun provideLocationCatalogApi(retrofit: Retrofit): LocationCatalogApi =
        retrofit.create(LocationCatalogApi::class.java)

    @Provides
    @Singleton
    fun provideShipperApi(retrofit: Retrofit): ShipperApi =
        retrofit.create(ShipperApi::class.java)

    @Provides
    @Singleton
    fun provideSystemApi(retrofit: Retrofit): SystemApi =
        retrofit.create(SystemApi::class.java)
}
