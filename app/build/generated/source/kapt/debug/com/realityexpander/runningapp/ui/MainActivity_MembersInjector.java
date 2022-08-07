// Generated by Dagger (https://dagger.dev).
package com.realityexpander.runningapp.ui;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.inject.Provider;

@QualifierMetadata
@DaggerGenerated
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<String> nameProvider;

  public MainActivity_MembersInjector(Provider<String> nameProvider) {
    this.nameProvider = nameProvider;
  }

  public static MembersInjector<MainActivity> create(Provider<String> nameProvider) {
    return new MainActivity_MembersInjector(nameProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectName(instance, nameProvider.get());
  }

  @InjectedFieldSignature("com.androiddevs.runningapp.ui.MainActivity.name")
  public static void injectName(MainActivity instance, String name) {
    instance.name = name;
  }
}
