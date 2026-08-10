package com.seend.app.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger

object CoilConfig {
    fun getImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("seend_images"))
                    .maxSizePercent(0.05)
                    .build()
            }
            .logger(DebugLogger())
            .crossfade(true)
            .build()
    }
}
