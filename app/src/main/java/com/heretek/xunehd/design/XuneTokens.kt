package com.heretek.xunehd.design

/**
 * Design tokens from docs/design-tokens.md. Screens must consume these;
 * raw literals in screen code violate the design-invariant audit.
 */
object XuneTokens {
    // Canvas
    const val CANVAS_WIDTH = 480
    const val CANVAS_HEIGHT = 272

    // Spacing
    const val EDGE = 16
    const val CROSSBAR_LEAD = 24
    const val CROSSBAR_HEIGHT = 34
    const val ROW_HEIGHT = 40
    const val ALBUM_TILE = 92
    const val GRID_GUTTER = 8
    const val MINI_PLAYER_HEIGHT = 32

    // Type (design-unit dp)
    const val TYPE_MENU_ITEM = 34
    const val TYPE_HEADER_CROPPED = 40
    const val TYPE_HEADER_CROP_VISIBLE = 22
    const val TYPE_CROSSBAR = 18
    const val TYPE_NOW_TITLE = 26
    const val TYPE_NOW_META = 15
    const val TYPE_LIST = 14
    const val TYPE_LIST_SECONDARY = 11
    const val TYPE_CAPTION = 9
    const val TYPE_ALPHABET = 10
}
