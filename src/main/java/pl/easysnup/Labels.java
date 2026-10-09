package pl.easysnup;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/** Napis "Kostium snupa" nad kostiumem leżącym na ziemi. */
public final class Labels {
    private static final double MAX_DISTANCE = 24.0;
    private static final double HEIGHT = 0.9;
    private static final float SCALE = 0.025f;

    private Labels() {}

    public static void render(WorldRenderContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = ctx.matrixStack();
        VertexConsumerProvider vcp = ctx.consumers();
        if (m == null || vcp == null) return;
        Vec3d cam = ctx.camera().getPos();
        float td = ctx.tickCounter().getTickDelta(false);
        boolean drew = false;
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof ItemEntity item) || item.isRemoved()) continue;
            Costume c = Wardrobe.groundCostume(item.getStack());
            if (c == null) continue;
            if (e.squaredDistanceTo(mc.player) > MAX_DISTANCE * MAX_DISTANCE) continue;
            draw(mc, m, vcp, cam, e.getLerpedPos(td), c.displayName());
            drew = true;
        }
        if (drew && vcp instanceof VertexConsumerProvider.Immediate imm) imm.draw();
    }

    private static void draw(MinecraftClient mc, MatrixStack m, VertexConsumerProvider vcp, Vec3d cam, Vec3d at, Text text) {
        TextRenderer font = mc.textRenderer;
        m.push();
        m.translate(at.x - cam.x, at.y + HEIGHT - cam.y, at.z - cam.z);
        m.multiply(mc.getEntityRenderDispatcher().getRotation());
        m.scale(SCALE, -SCALE, SCALE);
        Matrix4f mat = m.peek().getPositionMatrix();
        float left = -font.getWidth(text) / 2f;
        int bg = (int) (mc.options.getTextBackgroundOpacity(0.25f) * 255f) << 24;
        font.draw(text, left, 0f, 0xFFFFFFFF, false, mat, vcp, TextRenderer.TextLayerType.SEE_THROUGH, bg, 15728880);
        m.pop();
    }
}
