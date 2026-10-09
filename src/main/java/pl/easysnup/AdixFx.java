package pl.easysnup;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Bonusy kostiumu Adixa: kolorowe hitboxy + phonk po zabiciu gracza. */
public final class AdixFx {
    private static final class Hit {
        int tick; Vec3d pos; float health; double height;
    }

    private static final Map<Integer, Hit> HITS = new HashMap<>();
    private static int tick = 0;
    private static SoundInstance current;

    private AdixFx() {}

    public static void onAttack(Entity target) {
        if (Wardrobe.worn != Costume.ADIX || !(target instanceof PlayerEntity p)) return;
        Hit h = HITS.computeIfAbsent(target.getId(), k -> new Hit());
        h.tick = tick;
        h.pos = target.getPos();
        h.health = p.getHealth();
        h.height = target.getHeight();
    }

    public static void tick(MinecraftClient mc) {
        tick++;
        if (mc.world == null || mc.player == null || Wardrobe.worn != Costume.ADIX) {
            HITS.clear();
            return;
        }
        Iterator<Map.Entry<Integer, Hit>> it = HITS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Hit> e = it.next();
            Hit h = e.getValue();
            Entity ent = mc.world.getEntityById(e.getKey());
            if (ent instanceof PlayerEntity pl && !pl.isRemoved()) {
                h.pos = pl.getPos();
                h.health = pl.getHealth();
                h.height = pl.getHeight();
                if (pl.isDead() || pl.getHealth() <= 0f) {
                    kill(mc, h);
                    it.remove();
                    continue;
                }
            } else if (tick - h.tick <= 40 && h.health <= 6f) {
                // gracz zniknął zaraz po naszym trafieniu, a miał mało życia - uznajemy za zabójstwo
                kill(mc, h);
                it.remove();
                continue;
            }
            if (tick - h.tick > 100) it.remove();
        }
    }

    private static void kill(MinecraftClient mc, Hit h) {
        Fx.blast(h.pos.add(0, h.height / 2.0, 0), 0xFF1010);
        if (current != null) mc.getSoundManager().stop(current);
        current = PositionedSoundInstance.master(SoundEvent.of(Identifier.of("easysnup", "phonk")), 1.0f, 1.0f);
        mc.getSoundManager().play(current);
    }

    // ------------------------------------------------------------- kolorowe hitboxy

    public static void renderHitboxes(WorldRenderContext ctx) {
        if (Wardrobe.worn != Costume.ADIX) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = ctx.matrixStack();
        VertexConsumerProvider vcp = ctx.consumers();
        if (m == null || vcp == null) return;
        Vec3d cam = ctx.camera().getPos();
        float td = ctx.tickCounter().getTickDelta(false);
        VertexConsumer lc = vcp.getBuffer(RenderLayer.getLines());
        MatrixStack.Entry entry = m.peek();

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player || e.isRemoved()) continue;
            if (e.squaredDistanceTo(mc.player) > 96 * 96) continue;
            Vec3d diff = e.getLerpedPos(td).subtract(e.getPos());
            Box b = e.getBoundingBox().offset(diff).offset(-cam.x, -cam.y, -cam.z);
            int rgb = colorFor(e);
            float r = ((rgb >> 16) & 255) / 255f, g = ((rgb >> 8) & 255) / 255f, bl = (rgb & 255) / 255f;
            box(entry, lc, b, r, g, bl);
        }
        if (vcp instanceof VertexConsumerProvider.Immediate imm) imm.draw(RenderLayer.getLines());
    }

    private static int colorFor(Entity e) {
        if (e instanceof PlayerEntity) {
            float hue = (Math.abs(e.getUuid().hashCode()) % 360) / 360f;   // każdy gracz inny kolor
            return MathHelper.hsvToRgb(hue, 1f, 1f);
        }
        if (e instanceof Monster) return 0xFF8800;
        if (e instanceof PassiveEntity) return 0x33FF55;
        if (e instanceof ItemEntity) return 0xFFEE22;
        return 0x22DDFF;
    }

    private static void box(MatrixStack.Entry en, VertexConsumer c, Box b, float r, float g, float bl) {
        float x0 = (float) b.minX, y0 = (float) b.minY, z0 = (float) b.minZ;
        float x1 = (float) b.maxX, y1 = (float) b.maxY, z1 = (float) b.maxZ;
        // dół
        line(en, c, x0, y0, z0, x1, y0, z0, r, g, bl);
        line(en, c, x1, y0, z0, x1, y0, z1, r, g, bl);
        line(en, c, x1, y0, z1, x0, y0, z1, r, g, bl);
        line(en, c, x0, y0, z1, x0, y0, z0, r, g, bl);
        // góra
        line(en, c, x0, y1, z0, x1, y1, z0, r, g, bl);
        line(en, c, x1, y1, z0, x1, y1, z1, r, g, bl);
        line(en, c, x1, y1, z1, x0, y1, z1, r, g, bl);
        line(en, c, x0, y1, z1, x0, y1, z0, r, g, bl);
        // słupki
        line(en, c, x0, y0, z0, x0, y1, z0, r, g, bl);
        line(en, c, x1, y0, z0, x1, y1, z0, r, g, bl);
        line(en, c, x1, y0, z1, x1, y1, z1, r, g, bl);
        line(en, c, x0, y0, z1, x0, y1, z1, r, g, bl);
    }

    private static void line(MatrixStack.Entry en, VertexConsumer c, float x1, float y1, float z1,
                             float x2, float y2, float z2, float r, float g, float b) {
        float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-6f) return;
        dx /= len; dy /= len; dz /= len;
        c.vertex(en, x1, y1, z1).color(r, g, b, 1f).normal(en, dx, dy, dz);
        c.vertex(en, x2, y2, z2).color(r, g, b, 1f).normal(en, dx, dy, dz);
    }
}
