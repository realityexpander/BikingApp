// Generated by Dagger (https://dagger.dev).
package com.realityexpander.runningapp.ui;

import com.realityexpander.runningapp.repositories.MainRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class MainViewModel_Factory implements Factory<MainViewModel> {
  private final Provider<MainRepository> mainRepositoryProvider;

  public MainViewModel_Factory(Provider<MainRepository> mainRepositoryProvider) {
    this.mainRepositoryProvider = mainRepositoryProvider;
  }

  @Override
  public MainViewModel get() {
    return newInstance(mainRepositoryProvider.get());
  }

  public static MainViewModel_Factory create(Provider<MainRepository> mainRepositoryProvider) {
    return new MainViewModel_Factory(mainRepositoryProvider);
  }

  public static MainViewModel newInstance(MainRepository mainRepository) {
    return new MainViewModel(mainRepository);
  }
}
