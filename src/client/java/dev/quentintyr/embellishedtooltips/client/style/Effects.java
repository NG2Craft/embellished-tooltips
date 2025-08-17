package dev.quentintyr.embellishedtooltips.client.style;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Defines the rendering layers for tooltip effects
 */
@Environment(EnvType.CLIENT)
public enum Effects {
    /**
     * Background layer, rendered first
     */
    BACKGROUND,

    /**
     * Between background and text
     */
    TEXT_BACKGROUND,

    /**
     * Between text and frame
     */
    TEXT_FRAME,

    /**
     * Between frame and icon
     */
    FRAME,

    /**
     * Front layer, rendered last
     */
    FRONT
}
