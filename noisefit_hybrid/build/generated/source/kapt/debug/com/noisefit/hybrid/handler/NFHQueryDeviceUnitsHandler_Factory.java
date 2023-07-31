package com.noisefit.hybrid.handler;

import android.content.Context;
import com.noisefit.hybrid.base.VisionCommands;
import com.noisefit.hybrid.dataconversions.DataConverter;
import com.noisefit.hybrid.utils.BitwiseHelperUtils;
import com.noisefit.hybrid.utils.BitwiseUtils;
import com.noisefit.hybrid.utils.BluetoothSDK_Exp;
import com.noisefit_commans.data.local.abstraction.WatchDataStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class NFHQueryDeviceUnitsHandler_Factory implements Factory<NFHQueryDeviceUnitsHandler> {
  private final Provider<BitwiseUtils> bitwiseUtilsProvider;

  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<BitwiseHelperUtils> bitwiseHelperUtilsProvider;

  private final Provider<WatchDataStore> watchDataStoreProvider;

  private final Provider<VisionCommands> visionCommandsProvider;

  private final Provider<Context> contextProvider;

  private final Provider<BluetoothSDK_Exp> bluetoothsdkExpProvider;

  public NFHQueryDeviceUnitsHandler_Factory(Provider<BitwiseUtils> bitwiseUtilsProvider,
      Provider<DataConverter> dataConverterProvider,
      Provider<BitwiseHelperUtils> bitwiseHelperUtilsProvider,
      Provider<WatchDataStore> watchDataStoreProvider,
      Provider<VisionCommands> visionCommandsProvider, Provider<Context> contextProvider,
      Provider<BluetoothSDK_Exp> bluetoothsdkExpProvider) {
    this.bitwiseUtilsProvider = bitwiseUtilsProvider;
    this.dataConverterProvider = dataConverterProvider;
    this.bitwiseHelperUtilsProvider = bitwiseHelperUtilsProvider;
    this.watchDataStoreProvider = watchDataStoreProvider;
    this.visionCommandsProvider = visionCommandsProvider;
    this.contextProvider = contextProvider;
    this.bluetoothsdkExpProvider = bluetoothsdkExpProvider;
  }

  @Override
  public NFHQueryDeviceUnitsHandler get() {
    return newInstance(bitwiseUtilsProvider.get(), dataConverterProvider.get(), bitwiseHelperUtilsProvider.get(), watchDataStoreProvider.get(), visionCommandsProvider.get(), contextProvider.get(), bluetoothsdkExpProvider.get());
  }

  public static NFHQueryDeviceUnitsHandler_Factory create(
      Provider<BitwiseUtils> bitwiseUtilsProvider, Provider<DataConverter> dataConverterProvider,
      Provider<BitwiseHelperUtils> bitwiseHelperUtilsProvider,
      Provider<WatchDataStore> watchDataStoreProvider,
      Provider<VisionCommands> visionCommandsProvider, Provider<Context> contextProvider,
      Provider<BluetoothSDK_Exp> bluetoothsdkExpProvider) {
    return new NFHQueryDeviceUnitsHandler_Factory(bitwiseUtilsProvider, dataConverterProvider, bitwiseHelperUtilsProvider, watchDataStoreProvider, visionCommandsProvider, contextProvider, bluetoothsdkExpProvider);
  }

  public static NFHQueryDeviceUnitsHandler newInstance(BitwiseUtils bitwiseUtils,
      DataConverter dataConverter, BitwiseHelperUtils bitwiseHelperUtils,
      WatchDataStore watchDataStore, VisionCommands visionCommands, Context context,
      BluetoothSDK_Exp bluetoothsdkExp) {
    return new NFHQueryDeviceUnitsHandler(bitwiseUtils, dataConverter, bitwiseHelperUtils, watchDataStore, visionCommands, context, bluetoothsdkExp);
  }
}
