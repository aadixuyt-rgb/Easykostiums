package pl.easysnup;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class EasySnupClient implements ClientModInitializer {
    public static KeyBinding openMenuKey;
    private static boolean pendingSzafka = false;

    @Override
    public void onInitializeClient() {
        Config.load();

        openMenuKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.easysnup.open_menu", InputUtil.Type.KEYSYM, 66 /* B */, "category.easysnup"));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            for (String name : new String[]{"szafka", "szafa"}) {
                dispatcher.register(ClientCommandManager.literal(name).executes(ctx -> {
                    pendingSzafka = true;
                    return 1;
                }));
            }
        });

        ClientTickEvents.START_CLIENT_TICK.register(Wardrobe::eatDropKey);

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            Looks.rendering = false;
            if (pendingSzafka && mc.currentScreen == null && mc.player != null) {
                pendingSzafka = false;
                Szafka.open();
            }
            while (openMenuKey.wasPressed()) {
                if (mc.currentScreen == null && mc.player != null) mc.setScreen(new SnupScreen());
            }
            Wardrobe.tick(mc);
            if (mc.player != null) Hearts.tick(mc.player);
            Abilities.tick(mc);
            AdixFx.tick(mc);
            Fx.tick();
        });

        UseItemCallback.EVENT.register((player, world, hand) ->
                world.isClient && Wardrobe.onRightClick(hand == Hand.OFF_HAND) ? ActionResult.FAIL : ActionResult.PASS);
        UseBlockCallback.EVENT.register((player, world, hand, hit) ->
                world.isClient && Wardrobe.onRightClick(hand == Hand.OFF_HAND) ? ActionResult.FAIL : ActionResult.PASS);
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient) AdixFx.onAttack(entity);
            return ActionResult.PASS;
        });

        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> {
            Labels.render(ctx);
            AdixFx.renderHitboxes(ctx);
        });
    }
}
