package com.aicandy.imageclassification.mobilenet

import android.graphics.Bitmap
import android.provider.SyncStateContract
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils

class Classifier(modelPath: String?) {
    private var model: Module
    private var mean = floatArrayOf(0.485f, 0.456f, 0.406f)
    private var std = floatArrayOf(0.229f, 0.224f, 0.225f)

    init {
        model = Module.load(modelPath)
    }

    fun setMeanAndStd(mean: FloatArray, std: FloatArray) {
        this.mean = mean
        this.std = std
    }

    private fun preprocess(bitmap: Bitmap?, size: Int): Tensor {
        var bitmap = bitmap
        bitmap = Bitmap.createScaledBitmap(bitmap!!, size, size, false)
        return TensorImageUtils.bitmapToFloat32Tensor(bitmap, mean, std)
    }

    fun argMax(inputs: FloatArray): Int {
        var maxIndex = -1
        var maxvalue = 0.0f
        for (i in inputs.indices) {
            if (inputs[i] > maxvalue) {
                maxIndex = i
                maxvalue = inputs[i]
            }
        }
        return maxIndex
    }

    fun predict(bitmap: Bitmap?): String {
        val tensor = preprocess(bitmap, 224)
        val inputs = IValue.from(tensor)
        val outputs = model.forward(inputs).toTensor()
        val scores = outputs.dataAsFloatArray
        val classIndex = argMax(scores)
        return Constants.IMAGENET_CLASSES[classIndex]
    }
}
