package pl.easysnup;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.boss.BossBar;
import pl.easysnup.mixin.BossBarHudAccessor;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/** Lokalny boss bar (widoczny tylko dla Ciebie). */
public final class Bar {
    private final UUID id;

    public Bar(String name) {
        this.id = UUID.nameUUIDFromBytes(("easysnup:" + name).getBytes(StandardCharsets.UTF_8));
    }

    private Map<UUID, ClientBossBar> bars() {
        return ((BossBarHudAccessor) MinecraftClient.getInstance().inGameHud.getBossBarHud()).easysnup$getBossBars();
    }

    public void show(String legacyText, float percent, BossBar.Color color) {
        Map<UUID, ClientBossBar> bars = bars();
        ClientBossBar bar = bars.get(id);
        if (bar == null) {
            bar = new ClientBossBar(id, Legacy.parse(legacyText), percent, color, BossBar.Style.PROGRESS, false, false, false);
            bars.put(id, bar);
        } else {
            bar.setName(Legacy.parse(legacyText));
            bar.setColor(color);
            bar.setPercent(Math.max(0f, Math.min(1f, percent)));
        }
    }

    public void hide() {
        bars().remove(id);
    }
}
