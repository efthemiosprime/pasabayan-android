package com.efthemiosprime.pasabayan.core.session

import com.efthemiosprime.pasabayan.core.network.auth.BackendUserJson

data class AuthUser(
    val id: Long,
    val name: String,
    val email: String,
    val avatar: String?,
    val phone: String?,
    val phoneVerified: Boolean,
    val profileCompleted: Boolean,
    val provider: String,
    val userTypes: List<String>,
    val isActiveCarrier: Boolean,
    val isActiveShipper: Boolean,
)

fun BackendUserJson.toAuthUser(): AuthUser = AuthUser(
    id = id,
    name = name,
    email = email,
    avatar = avatar,
    phone = phone,
    phoneVerified = phoneVerified ?: false,
    profileCompleted = profileCompleted ?: false,
    provider = provider.orEmpty(),
    userTypes = userTypes ?: listOf("shipper"),
    isActiveCarrier = isActiveCarrier ?: false,
    isActiveShipper = isActiveShipper ?: true,
)
