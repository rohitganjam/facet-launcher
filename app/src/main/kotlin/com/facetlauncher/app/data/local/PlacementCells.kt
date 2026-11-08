package com.facetlauncher.app.data.local

import com.facetlauncher.app.domain.GridCell

/** The key a grid cell is saved under, matching [com.facetlauncher.app.data.model.gridKey]. */
internal fun AppPlacementEntity.gridKey(): String = "$packageName/$activityName/$userId"

internal fun FolderPlacementEntity.gridKey(): String = "folder_$folderId"

internal fun cellOf(row: Int?, column: Int?): GridCell? = if (row != null && column != null) GridCell(row, column) else null

internal fun AppPlacementEntity.cell(landscape: Boolean): GridCell? =
    if (landscape) cellOf(landscapeRow, landscapeColumn) else cellOf(portraitRow, portraitColumn)

internal fun FolderPlacementEntity.cell(landscape: Boolean): GridCell? =
    if (landscape) cellOf(landscapeRow, landscapeColumn) else cellOf(portraitRow, portraitColumn)

internal fun AppPlacementEntity.withCell(landscape: Boolean, cell: GridCell?): AppPlacementEntity =
    if (landscape) copy(landscapeRow = cell?.row, landscapeColumn = cell?.column) else copy(portraitRow = cell?.row, portraitColumn = cell?.column)

internal fun FolderPlacementEntity.withCell(landscape: Boolean, cell: GridCell?): FolderPlacementEntity =
    if (landscape) copy(landscapeRow = cell?.row, landscapeColumn = cell?.column) else copy(portraitRow = cell?.row, portraitColumn = cell?.column)

/** [this] with the other orientation's cell (and id) carried over from [existing], which a plain reorder must not lose. */
internal fun AppPlacementEntity.keepingCellsOf(existing: AppPlacementEntity?): AppPlacementEntity = if (existing == null) this else copy(
    id = existing.id,
    portraitRow = existing.portraitRow, portraitColumn = existing.portraitColumn,
    landscapeRow = existing.landscapeRow, landscapeColumn = existing.landscapeColumn,
)

internal fun FolderPlacementEntity.keepingCellsOf(existing: FolderPlacementEntity?): FolderPlacementEntity = if (existing == null) this else copy(
    id = existing.id,
    portraitRow = existing.portraitRow, portraitColumn = existing.portraitColumn,
    landscapeRow = existing.landscapeRow, landscapeColumn = existing.landscapeColumn,
)
