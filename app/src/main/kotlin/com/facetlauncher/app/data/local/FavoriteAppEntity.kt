package com.facetlauncher.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.facetlauncher.app.data.model.AppProfile

@Entity(
    tableName = "favorite_apps",
    foreignKeys = [
        ForeignKey(
            entity = FacetEntity::class,
            parentColumns = ["id"],
            childColumns = ["facetId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["facetId", "packageName", "activityName", "userId"], unique = true)],
)
data class FavoriteAppEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val facetId: Long,
    val packageName: String,
    val activityName: String,
    val position: Int,
    val profile: AppProfile = AppProfile.PERSONAL,
    /** The real `UserHandle.hashCode()` this app came from (`getIdentifier()` itself is hidden API, not in the public SDK — see [Migrations.MIGRATION_19_20]) — the actual identity key; [profile] is display-only. */
    val userId: Int = -1,
)
