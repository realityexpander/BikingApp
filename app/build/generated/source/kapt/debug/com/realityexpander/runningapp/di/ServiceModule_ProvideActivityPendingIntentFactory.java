// Generated by Dagger (https://dagger.dev).
package com.realityexpander.runningapp.di;

import android.app.PendingIntent;
import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.inject.Provider;

@ScopeMetadata("dagger.hilt.android.scopes.ServiceScoped")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class ServiceModule_ProvideActivityPendingIntentFactory implements Factory<PendingIntent> {
  private final Provider<Context> contextProvider;

  public ServiceModule_ProvideActivityPendingIntentFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PendingIntent get() {
    return provideActivityPendingIntent(contextProvider.get());
  }

  public static ServiceModule_ProvideActivityPendingIntentFactory create(
      Provider<Context> contextProvider) {
    return new ServiceModule_ProvideActivityPendingIntentFactory(contextProvider);
  }

  public static PendingIntent provideActivityPendingIntent(Context context) {
    return Preconditions.checkNotNullFromProvides(ServiceModule.INSTANCE.provideActivityPendingIntent(context));
  }
}
