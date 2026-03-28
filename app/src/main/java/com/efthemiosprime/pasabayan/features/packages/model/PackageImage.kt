package com.efthemiosprime.pasabayan.features.packages.model

data class PackageImage(
    val id: Int,
    val packageRequestId: Int,
    val imagePath: String,
    val displayOrder: Int,
    val originalFilename: String?,
    val url: String,
    val createdAt: String?,
)
