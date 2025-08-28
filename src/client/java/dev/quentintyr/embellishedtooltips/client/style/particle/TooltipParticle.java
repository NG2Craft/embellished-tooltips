package dev.quentintyr.embellishedtooltips.client.style.particle;

import dev.quentintyr.embellishedtooltips.client.render.TooltipContext;
import net.minecraft.util.math.Vec2f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class TooltipParticle {
    protected final long START_TIME;
    protected final float MAX_LIFETIME;
    protected Vec2f position;

    public TooltipParticle(float lifetime) {
        this.position = new Vec2f(0.0F, 0.0F);
        this.START_TIME = System.currentTimeMillis();
        this.MAX_LIFETIME = lifetime;
    }

    public abstract void renderParticle(TooltipContext context, float time);

    public final void render(TooltipContext context) {
        this.renderParticle(context, (float) (System.currentTimeMillis() - this.START_TIME) / 1000.0F);
    }

    public final boolean shouldRemove() {
        return (float) (System.currentTimeMillis() - this.START_TIME) / 1000.0F > this.MAX_LIFETIME;
    }

    public Vec2f getPosition() {
        return this.position;
    }

    public void setPosition(Vec2f position) {
        this.position = position;
    }
}
