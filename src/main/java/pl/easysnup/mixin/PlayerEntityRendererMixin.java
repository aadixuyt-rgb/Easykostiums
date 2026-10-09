package pl.easysnup.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.easysnup.Looks;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    private static final String UPDATE =
            "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V";

    @Inject(method = UPDATE, at = @At("HEAD"))
    private void easysnup$begin(AbstractClientPlayerEntity player, PlayerEntityRenderState state, float tickDelta, CallbackInfo ci) {
        Looks.rendering = player == MinecraftClient.getInstance().player;
    }

    @Inject(method = UPDATE, at = @At("TAIL"))
    private void easysnup$end(AbstractClientPlayerEntity player, PlayerEntityRenderState state, float tickDelta, CallbackInfo ci) {
        Looks.rendering = false;
    }
}
