//
// SPDX-FileCopyrightText: 2026 LumineDroid
// SPDX-License-Identifier: Apache-2.0
//

package org.luminedroid.extensions.about

enum class BadgeStyle {
    PINK,
    BLUE,
}

sealed class TeamItem {
    data class Person(
        val name: String,
        val role: String,
        val username: String,
        val link: String,
        val telegram: String,
        val badgeLabel: String,
        val badgeStyle: BadgeStyle,
    ) : TeamItem()
}
