/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.android.wallpaper.picker.customization.domain.interactor

import android.graphics.Bitmap
import com.android.wallpaper.picker.customization.data.repository.WallpaperRepository
import com.android.wallpaper.picker.customization.shared.model.WallpaperDestination
import com.android.wallpaper.picker.customization.shared.model.WallpaperModel
import javax.inject.Provider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/** Handles business logic for wallpaper-related use-cases. */
class WallpaperInteractor(
    private val repository: WallpaperRepository,
    private val snapshotRestorer: Provider<WallpaperSnapshotRestorer>,
) {
    /** Returns the ID of the currently-selected wallpaper. */
    fun selectedWallpaperId(
        destination: WallpaperDestination,
    ): StateFlow<String> {
        return repository.selectedWallpaperId(destination = destination)
    }

    /**
     * Returns the ID of the wallpaper that is in the process of becoming the selected wallpaper or
     * `null` if no such transaction is currently taking place.
     */
    fun selectingWallpaperId(
        destination: WallpaperDestination,
    ): Flow<String?> {
        return repository.selectingWallpaperId.map { it[destination] }
    }

    /**
     * Lists the [maxResults] most recent wallpapers.
     *
     * The first one is the most recent (current) wallpaper.
     */
    fun previews(
        destination: WallpaperDestination,
        maxResults: Int,
    ): Flow<List<WallpaperModel>> {
        return repository
            .recentWallpapers(
                destination = destination,
                limit = maxResults,
            )
            .map { previews ->
                if (previews.size > maxResults) {
                    previews.subList(0, maxResults)
                } else {
                    previews
                }
            }
    }

    /** Sets the wallpaper to the one with the given ID. */
    suspend fun setWallpaper(
        destination: WallpaperDestination,
        wallpaperId: String,
    ) {
        repository.setWallpaper(
            destination = destination,
            wallpaperId = wallpaperId,
        )
        snapshotRestorer
            .get()
            .storeSnapshot(
                destination = destination,
                selectedWallpaperId = wallpaperId,
            )
    }

    /** Returns a thumbnail for the wallpaper with the given ID. */
    suspend fun loadThumbnail(wallpaperId: String): Bitmap? {
        return repository.loadThumbnail(
            wallpaperId = wallpaperId,
        )
    }
}