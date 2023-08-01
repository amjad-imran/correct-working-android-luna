package com.noisefit.hybrid.handler;

import android.content.Context;
import com.noisefit.hybrid.base.VisionCommands;
import com.noisefit.hybrid.dataconversions.DataConverter;
import com.noisefit.hybrid.utils.BitwiseHelperUtils;
import com.noisefit.hybrid.utils.BitwiseUtils;
import com.noisefit.hybrid.utils.BluetoothSDK_Exp;
import com.noisefit.hybrid.utils.OnlineWatchFacesVision;
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
public final class NFHUpdateDeviceUnitsHandler_Factory implements Factory<NFHUpdateDeviceUnitsHandler> {
  private final Provider<Context> contextProvider;

  private final Provider<BitwiseUtils> bitwiseUtilsProvider;

  private final Provider<DataConverter> dataConverterProvider;

  private final Provider<BitwiseHelperUtils> bitwiseHelperUtilsProvider;

  private final Provider<WatchDataStore> watchDataStoreProvider;

  private final Provider<OnlineWatchFacesVision> onlineWatchFacesVisionProvider;

  private final Provider<BluetoothSDK_Exp> bluetoothsdkExpProvider;

  private final Provider<VisionCommands> visionCommandsProvider;

  public NFHUpdateDeviceUnitsHandler_Factory(Provider<Context> contextProvider,
      Provider<BitwiseUtils> bitwiseUtilsProvider, Provider<DataConverter> dataConverterProvider,
      Provider<BitwiseHelperUtils> bitwiseHelperUtilsProvider,
      Provider<WatchDataStore> watchDataStoreProvider,
      Provider<OnlineWatchFacesVision> onlineWatchFacesVisionProvider,
      Provider<BluetoothSDK_Exp> bluetoothsdkExpProvider,
      Provider<VisionCommands> visionCommandsProvider) {
    this.contextProvider = contextProvider;
    this.bitwiseUtilsProvider = bitwiseUtilsProvider;
    this.dataConverterProvider = dataConverterProvider;
    this.bitwiseHelperUtilsProvider = bitwiseHelperUtilsProvider;
    this.watchDataStoreProvider = watchDataStoreProvider;
    this.onlineWatchFacesVisionProvider = onlineWatchFacesVisionProvider;
    this.bluetoothsdkExpProvider = bluetoothsdkExpProvider;
    this.visionCommandsProvider = visionCommandsProvider;
  }

  @Override
  public NFHUpdateDeviceUnitsHandler get() {
    return newInstance(contextProvider.get(), bitwiseUtilsProvider.get(), dataConverterProvider.get(), bitwiseHelperUtilsProvider.get(), watchDataStoreProvider.get(), onlineWatchFacesVisionProvider.get(), bluetoothsdkExpProvider.get(), visionCommandsProvider.get());
  }

  public static NFHUpdateDeviceUnitsHandler_Factory create(Provider<Context> contextProvider,
      Provider<BitwiseUtils> bitwiseUtilsProvider, Provider<DataConverter> dataConverterProvider,
      Provider<BitwiseHelperUtils> bitwiseHelperUtilsProvider,
      Provider<WatchDataStore> watchDataStoreProvider,
      Provider<OnlineWatchFacesVision> onlineWatchFacesVisionProvider,
      Provider<BluetoothSDK_Exp> bluetoothsdkExpProvider,
      Provider<VisionCommands> visionCommandsProvider) {
    return new NFHUpdateDeviceUnitsHandler_Factory(contextProvider, bitwiseUtilsProvider, dataConverterProvider, bitwiseHelperUtilsProvider, watchDataStoreProvider, onlineWatchFacesVisionProvider, bluetoothsdkExpProvider, visionCommandsProvider);
  }

  public static NFHUpdateDeviceUnitsHandler newInstance(Context context, BitwiseUtils bitwiseUtils,
      DataConverter dataConverter, BitwiseHelperUtils bitwiseHelperUtils,
      WatchDataStore watchDataStore, OnlineWatchFacesVision onlineWatchFacesVision,
      BluetoothSDK_Exp bluetoothsdkExp, VisionCommands visionCommands) {
    return new NFHUpdateDeviceUnitsHandler(context, bitwiseUtils, dataConverter, bitwiseHelperUtils, watchDataStore, onlineWatchFacesVision, bluetoothsdkExp, visionCommands);
  }
}
