package com.noisefit_ryeex_sdk.dataConversion

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.net.Uri
import android.text.TextUtils
import com.jieli.bmp_convert.BmpConvert
import com.jieli.bmp_convert.OnConvertListener
import com.noisefit_commans.common.convertCorner

import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.DiyCustomWatchFace
import com.noisefit_commans.utils.LOGS

import com.noisefit_ryeex_sdk.utils.GZipUtil
import com.ryeex.ble.common.tar.RyeexTar
import com.ryeex.ble.common.utils.FileUtil
import com.ryeex.ble.connector.callback.AsyncBleCallback
import com.ryeex.ble.connector.error.BleError
import com.ryeex.ble.connector.utils.ByteUtil
import com.ryeex.watch.adapter.device.WatchDevice
import com.ryeex.watch.adapter.model.entity.DeviceSurfaceInfo
import com.ryeex.watch.adapter.utils.ImgUtil
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteOrder


fun WatchDevice.deleteWatchface(
    isDynamic: Boolean,
    deviceSurface: DeviceSurfaceInfo,
    callback: AsyncBleCallback<Int, BleError>
) {
    if (isDynamic) {
        val surface = deviceSurface.surfaceList.firstOrNull {
            it.surfaceType == DeviceSurfaceInfo.Surface.SurfaceType.DYNAMIC
        }
        if (surface != null) {
            deleteSurface(surface.id,
                object : AsyncBleCallback<Void, BleError>() {
                    override fun onSuccess(p0: Void?) {
                        callback.sendSuccessMessage(surface.id)
                    }

                    override fun onFailure(error: BleError?) {
                        callback.sendFailureMessage(error)
                    }
                })
            return
        }
    }
    if ((deviceSurface.staticInstalledCount + deviceSurface.dynamicInstalledCount) == (deviceSurface.staticSupportCount + deviceSurface.dynamicSupportCount)) {
        if (deviceSurface.surfaceList.isNotEmpty()) {
            val surface = deviceSurface.surfaceList.last()
            surface?.let {
                deleteSurface(it.id,
                    object : AsyncBleCallback<Void, BleError>() {
                        override fun onSuccess(p0: Void?) {
                            callback.sendSuccessMessage(it.id)
                        }

                        override fun onFailure(error: BleError?) {
                            callback.sendFailureMessage(error)
                        }
                    })
            }
        }
    } else {
        callback.sendSuccessMessage(-1)
    }
}

fun DeviceSurfaceInfo.Surface.prepareTarResource(
    context: Context,
    noiseFitDevice: ColorFitDevice?,
    newId: Int,
    customWatchFace: DiyCustomWatchFace,
    tempDir: File
) {
    kotlin.runCatching {
        LOGS.i( "setDiyWatchFaceCustom surfaceInfo=$noiseFitDevice ")
        LOGS.i( "setDiyWatchFaceCustom surfaceInfo=$newId ")
        LOGS.i( "setDiyWatchFaceCustom surfaceInfo=$customWatchFace ")
        val tarFile = File(Uri.parse(customWatchFace.binFile).path!!)
        RyeexTar.unTar(tarFile.absolutePath, tempDir.absolutePath)
        val fileName = tarFile.nameWithoutExtension
        var oldId: String? = null
        if (fileName.contains("_")) {
            val names = fileName.split("_")
            if (names.size > 1) {
                oldId = names[1]
            }

            //旧tar包目录
            val tarFolder = tempDir.absolutePath + File.separator + fileName + File.separator
            val previewFolder = tarFolder + "preview" + File.separator
            val resFolder = tarFolder + "res" + File.separator
            val appFolder = tarFolder + "app" + File.separator
            FileUtil.createDirIfNotExists(previewFolder)
            FileUtil.createDirIfNotExists(resFolder)
            val configFile = File("${tarFolder}${oldId}.json")
            val positionFile = File("${appFolder}position.json")
            if (configFile.canRead() && positionFile.canRead()) {
                val configText = configFile.readText()
                val positionText = positionFile.readText()
                if (configText.isEmpty() || positionText.isEmpty()) {
                    return
                }
                val configJson = JSONObject(configText)
                val positionObject = JSONObject(positionText)
                val index = convertPosition(noiseFitDevice, customWatchFace.textLayerName).toString()
                val layoutObject = positionObject.getJSONObject(index)
                configJson.updateConfig(layoutObject, newId, customWatchFace.color)
                customWatchFace.convertImage(
                    context,
                    noiseFitDevice,
                    previewFolder,
                    resFolder
                )

                val tarName = names[0] + "_" + newId
                val newTarPath = tempDir.absolutePath + File.separator + tarName
                FileUtil.deleteDirectory(newTarPath)
                val newTarFolder = File(newTarPath)
                newTarFolder.mkdirs()
                val preview = File(previewFolder)
                preview.renameTo(File(newTarPath, "preview"))
                val res = File(resFolder)
                res.renameTo(File(newTarPath, "res"))
                val jsonContent = configJson.toString().replace("\\\\/".toRegex(), "/")
                FileUtil.appendString(
                    newTarPath + File.separator + newId + ".json",
                    jsonContent
                )

                val newTarFile = newTarFolder.parent!! + File.separator + tarName + ".tar"
                RyeexTar.tar(newTarPath, newTarFile, true)
                val gzipFile = "$newTarFile.gz"
                GZipUtil.compressFile(newTarFile, gzipFile)
                val bytes = if (FileUtil.fileExists(gzipFile)) {
                    FileUtil.readBytes(gzipFile)
                } else {
                    FileUtil.readBytes(newTarFile)
                }

                this.id = newId
                this.version = 2
                this.isSelected = true
                this.surfaceType = DeviceSurfaceInfo.Surface.SurfaceType.STATIC

                val resource = DeviceSurfaceInfo.Surface.Resource()
                resource.name = "watchface_$newId"
                resource.bytes = bytes
                resource.isLatest = true
                resource.type = DeviceSurfaceInfo.Surface.Resource.Type.TAR
                this.resources = mutableListOf(resource)
            }
        }
    }
}

private fun convertPosition(
    noiseFitDevice: ColorFitDevice?,
    style: String?
): Int {
    return when (style?.lowercase()) {
        "top-left" -> 0
        "top-centre" -> if (noiseFitDevice?.deviceType == DeviceType.COLORFIT_MIGHTY.deviceType) {
            1
        } else {
            0
        }
        "top-right" -> 2
        "bottom-left" -> 3
        "bottom-centre" -> if (noiseFitDevice?.deviceType == DeviceType.COLORFIT_MIGHTY.deviceType) {
            4
        } else {
            1
        }
        "bottom-right" -> 5
        else -> 1
    }
}

private fun Int.copyToFile(context: Context, dir: File, ext: String): File {
    val resName = context.resources.getResourceEntryName(this) + ".$ext"
    val file = File(dir, resName)
    val inputStream = context.resources.openRawResource(this)
    val fos = FileOutputStream(file)
    val buffer = ByteArray(1024)
    var byteCount: Int
    while (inputStream.read(buffer).also { byteCount = it } != -1) {
        fos.write(buffer, 0, byteCount)
    }
    fos.flush()
    inputStream.close()
    fos.close()
    return file
}


private fun JSONObject.updateConfig(jsonObject: JSONObject, newId: Int, fontColor: Int?) {
    if (has("basic")) {
        val basic = getJSONObject("basic")
        basic.put("id", newId)
        basic.put("name", "ryeex.watchface.$newId")
        basic.put("face_type", "photo")
        put("basic", basic)
    }
    if (has("layout")) {
        val layout = getJSONObject("layout")
        if (layout.has("items")) {
            val layoutItems = layout.getJSONArray("items")
            //从app的position.json取出对应布局的坐标
            if (jsonObject.has("items")) {
                val positionItems = jsonObject.getJSONArray("items")
                for (i in 0 until positionItems.length()) {
                    val positionItem = positionItems.getJSONObject(i)
                    if (positionItem.has("description")) {
                        val description = positionItem.getString("description")
                        for (j in 0 until layoutItems.length()) {
                            val layoutItem = layoutItems.getJSONObject(j)
                            //改坐标
                            if (layoutItem.has("description") && TextUtils.equals(
                                    layoutItem.getString("description"), description
                                )
                            ) {
                                val iterator = positionItem.keys()
                                while (iterator.hasNext()) {
                                    val key = iterator.next()
                                    layoutItem.put(key, positionItem[key])
                                }
                                break
                            }
                        }
                    }
                }
            }
            fontColor?.let {
                for (i in 0 until layoutItems.length()) {
                    val item = layoutItems.getJSONObject(i)
                    //改字体颜色
                    if (item.has("item_type") && TextUtils.equals(
                            item.getString("item_type"),
                            "text"
                        )
                    ) {
                        val textFont = item.getJSONObject("font")
                        textFont.put(
                            "color", if (it == 0 || it == -1) {
                                "#ffffff"
                            } else {
                                it.colorToHex()
                            }
                        )
                    }
                }
            }
        }
        put("layout", layout)
    }
}

fun Int.colorToHex(): String {
    val sb = StringBuffer()
    var r: String = Integer.toHexString(Color.red(this))
    var g: String = Integer.toHexString(Color.green(this))
    var b: String = Integer.toHexString(Color.blue(this))
    r = if (r.length == 1) "0$r" else r
    g = if (g.length == 1) "0$g" else g
    b = if (b.length == 1) "0$b" else b
    sb.append("#")
    sb.append(r)
    sb.append(g)
    sb.append(b)
    return sb.toString()
}

private fun DiyCustomWatchFace.convertImage(
    context: Context,
    noiseFitDevice: ColorFitDevice?,
    previewFolder: String,
    resFolder: String
) {
    val isCircle = when (screenType.lowercase()) {
        "circular" -> {
            true
        }
        else -> false
    }

    //deal background image
    val backgroundLayerBitmap = if (isCircle) {
        image?.oval()
    } else {
        image?.convertCorner(12f)
    }

    backgroundLayerBitmap?.let {
        FileUtil.deleteFile(resFolder + "bg_1.ryfb")
        val bgPath = "${RyeexConst.getWatchFaceCacheDir(context)}/bg.png"
        backgroundLayerBitmap.saveToStorage(bgPath)
        generateJieliResource(
            context,
            bgPath,
            resFolder + "bg_1.ryfb",
            intArrayOf(backgroundLayerBitmap.width, backgroundLayerBitmap.height)
        )
    }

    //deal preview image
    FileUtil.deleteFile(previewFolder + "dev.ryfb")
    val previewSizes = RyeexConst.getPreviewSize(noiseFitDevice)
    val bgPath = "${RyeexConst.getWatchFaceCacheDir(context)}/bg.png"
    backgroundLayerBitmap?.scale(previewSizes[0], previewSizes[1])?.saveToStorage(bgPath)


    val textLayer1 =  textLayer?.convertCorner(0f)

    val textLayerBitmap = textLayer1?.scale(previewSizes[0], previewSizes[1])

    val textLayerPath = "${RyeexConst.getWatchFaceCacheDir(context)}/textLayer.png"
    textLayerBitmap?.saveToStorage(textLayerPath)
    val previewBitmap = BitmapFactory.decodeFile(bgPath).overlay(
        BitmapFactory.decodeFile(textLayerPath).changeColor(color ?: Color.WHITE),
        previewSizes[0],
        previewSizes[1]
    )
    val previewPath = "${RyeexConst.getWatchFaceCacheDir(context)}/preview.png"
    previewBitmap.saveToStorage(previewPath)
    generateJieliResource(
        context, previewPath,
        previewFolder + "dev.ryfb",
        intArrayOf(previewBitmap.width, previewBitmap.height)
    )
}



fun changeBitmapSize(bitmap: Bitmap, width: Int, height: Int): Bitmap {
    val bitmapWidth = bitmap.width
    val bitmapHeight = bitmap.height
    val scaleWidth = width.toFloat() / bitmapWidth
    val scaleHeight = height.toFloat() / bitmapHeight
    val matrix = Matrix()
    matrix.postScale(scaleWidth, scaleHeight)
    return Bitmap.createBitmap(
        bitmap, 0, 0,
        bitmapWidth, bitmapHeight, matrix, false
    )
    
    
}

private fun generateJieliResource(
    context: Context,
    input: String,
    output: String,
    size: IntArray
): Boolean {
    val syncLock = Object()
    val bmpConvert = BmpConvert()
    val temp =
        context.cacheDir.absolutePath + File.separator + "watch_faces" + File.separator + "jieli_temp"
    val booleanWrapper = BooleanWrapper()
    booleanWrapper.result = false
    bmpConvert.bitmapConvert(BmpConvert.TYPE_BR_28, input, temp, object : OnConvertListener {
        override fun onStart(s: String?) {}
        override fun onStop(b: Boolean, s: String) {
            booleanWrapper.result = b
            synchronized(syncLock) { syncLock.notify() }
        }
    })
    synchronized(syncLock) {
        runCatching {
            syncLock.wait()
        }
    }
    bmpConvert.release()
    if (booleanWrapper.result) {
        runCatching {
            val bytesFile = FileUtil.readBytes(temp)
            if (bytesFile != null) {
                var bytes: ByteArray
                var index = 20
                bytes = ByteUtil.getBytes(bytesFile, index, index + 3)
                index = ByteUtil.bytesToInt(bytes, ByteOrder.LITTLE_ENDIAN)
                index += 12
                bytes = ByteUtil.getBytes(bytesFile, index, index + 3)
                index = ByteUtil.bytesToInt(bytes, ByteOrder.LITTLE_ENDIAN)
                index += 12
                //取图片的长度和偏移
                bytes = ByteUtil.getBytes(bytesFile, index, index + 3)
                val len: Int = ByteUtil.bytesToInt(bytes, ByteOrder.LITTLE_ENDIAN)
                index += 4
                bytes = ByteUtil.getBytes(bytesFile, index, index + 3)
                val offset: Int = ByteUtil.bytesToInt(bytes, ByteOrder.LITTLE_ENDIAN)
                val payloadBytes = ByteUtil.getBytes(bytesFile, offset, offset + len - 1)
                val type = -0x7ffcfffe
                ImgUtil.convertRyfb(type, payloadBytes, size, output)
                return true
            }
        }
    }
    return false
}

private class BooleanWrapper {
    var result = false
}



private fun Bitmap.scale(width: Float, height: Float): Bitmap {
    val w = this.width
    val h = this.height
    val scaleW = width / w
    val scaleH = height / h
    val matrix = Matrix()
    matrix.postScale(scaleW, scaleH)
    return Bitmap.createBitmap(this, 0, 0, w, h, matrix, true)
}

private fun Bitmap.overlay(foreground: Bitmap, with: Float, height: Float): Bitmap {
    val bitmap = Bitmap.createBitmap(with.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawBitmap(this, (with - this.width) / 2f, (height - this.height) / 2f, null)
    canvas.drawBitmap(
        foreground,
        (with - foreground.width) / 2f,
        (height - foreground.height) / 2f,
        null
    )
    canvas.save()
    canvas.restore()
    return bitmap
}

private fun Bitmap.saveToStorage(path: String) {
    kotlin.runCatching {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        } else {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        val out = FileOutputStream(file)
        if (this.compress(Bitmap.CompressFormat.PNG, 100, out)) {
            out.flush()
            out.close()
        }
    }
}

fun Bitmap.changeColor(color: Int): Bitmap {
    val p = Paint()
    p.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawBitmap(this, 0f, 0f, p)
    return bitmap
}

fun Bitmap.oval(): Bitmap {
    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val color = -0xbdbdbe
    val paint = Paint()
    val rect = Rect(0, 0, width, height)
    val rectF = RectF(rect)
    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawOval(rectF, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(this, rect, rect, paint)
    return output
}


fun ColorFitDevice.removeBond() {
    kotlin.runCatching {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return
        @SuppressLint("MissingPermission") val bluetoothDeviceList = bluetoothAdapter.bondedDevices
        var bluetoothDevice: BluetoothDevice? = null
        for (device in bluetoothDeviceList) {
            if (device.address == address) {
                bluetoothDevice = device
                break
            }
        }
        val bondDevice = BluetoothDevice::class.java.getMethod("removeBond")
        bondDevice.invoke(bluetoothDevice)
    }
}