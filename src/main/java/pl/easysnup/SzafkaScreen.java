package pl.easysnup;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.function.IntConsumer;

/** Skrzynkowe GUI szafki. */
public class SzafkaScreen extends Screen {
    public record Entry(int slot, ItemStack icon, List<Text> tooltip, IntConsumer action) {}

    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/generic_54.png");

    private final int rows;
    private final int bgHeight;
    private final List<Entry> entries;
    private int left, top;

    public SzafkaScreen(Text title, int rows, List<Entry> entries) {
        super(title);
        this.rows = rows;
        this.bgHeight = 114 + rows * 18;
        this.entries = entries;
    }

    @Override
    protected void init() {
        this.left = (this.width - 176) / 2;
        this.top = (this.height - bgHeight) / 2;
    }

    private int slotX(int slot) { return left + 8 + (slot % 9) * 18; }

    private int slotY(int slot) { return top + 18 + (slot / 9) * 18; }

    private int mainTop() { return top + 103 + (rows - 4) * 18; }

    private int hotbarTop() { return top + 161 + (rows - 4) * 18; }

    private static boolean isOver(int x, int y, double mx, double my) {
        return mx >= x && mx < x + 16 && my >= y && my < y + 16;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawTexture(RenderLayer::getGuiTextured, TEXTURE, left, top, 0f, 0f, 176, rows * 18 + 17, 256, 256);
        ctx.drawTexture(RenderLayer::getGuiTextured, TEXTURE, left, top + rows * 18 + 17, 0f, 126f, 176, 96, 256, 256);
        ctx.drawText(this.textRenderer, this.title, left + 8, top + 6, 0x404040, false);
        ctx.drawText(this.textRenderer, Text.translatable("container.inventory"), left + 8, top + bgHeight - 94, 0x404040, false);
        drawPlayerInventory(ctx);

        Entry hovered = null;
        for (Entry e : entries) {
            int x = slotX(e.slot()), y = slotY(e.slot());
            ctx.drawItem(e.icon(), x, y);
            if (isOver(x, y, mouseX, mouseY)) {
                ctx.fill(x, y, x + 16, y + 16, 0x80FFFFFF);
                hovered = e;
            }
        }
        if (hovered != null && !hovered.tooltip().isEmpty()) {
            ctx.drawTooltip(this.textRenderer, hovered.tooltip(), mouseX, mouseY);
        }
    }

    private void drawPlayerInventory(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        PlayerInventory inv = mc.player.getInventory();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                ItemStack st = inv.getStack(9 + row * 9 + col);
                int x = left + 8 + col * 18, y = mainTop() + row * 18;
                ctx.drawItem(st, x, y);
                ctx.drawStackOverlay(this.textRenderer, st, x, y);
            }
        }
        for (int col = 0; col < 9; col++) {
            ItemStack st = inv.getStack(col);
            int x = left + 8 + col * 18, y = hotbarTop();
            ctx.drawItem(st, x, y);
            ctx.drawStackOverlay(this.textRenderer, st, x, y);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Entry e : entries) {
            if (e.action() != null && isOver(slotX(e.slot()), slotY(e.slot()), mouseX, mouseY)) {
                MinecraftClient.getInstance().getSoundManager()
                        .play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                e.action().accept(e.slot());
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
