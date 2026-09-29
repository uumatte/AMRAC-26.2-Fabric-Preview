package amrac.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.TntRenderer;
import amrac.AmracBlockEntities;
import amrac.AmracEntities;
import amrac.AmracMenus;
import amrac.client.gui.AiPilotScreen;
import amrac.client.gui.MissileLoaderScreen;
import amrac.client.gui.RefuelerScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import amrac.client.render.AiPilotRenderer;
import amrac.client.render.F15Renderer;
import amrac.client.render.F16Renderer;
import amrac.client.render.F4JRenderer;
import amrac.client.render.Mig21Renderer;
import amrac.client.render.J10cRenderer;
import amrac.client.render.J8IIRenderer;
import amrac.client.render.F15ERenderer;
import amrac.client.render.F18Renderer;
import amrac.client.render.Mig23Renderer;
import amrac.client.render.RafaleRenderer;
import amrac.client.render.Su30Renderer;
import amrac.client.render.Mig29Renderer;
import amrac.client.render.MachineGunBulletRenderer;
import amrac.client.render.MissileLoaderRenderer;
import amrac.client.render.MissileRackRenderer;
import amrac.client.render.MissileRenderer;
import amrac.client.render.RefuelerRenderer;
import amrac.client.render.MissileTrails;
import amrac.client.render.Su27Renderer;
import amrac.client.render.TyphoonRenderer;
import amrac.entities.PlaneEntity;

public class AmracClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(AmracEntities.F16, F16Renderer::new);
        EntityRendererRegistry.register(AmracEntities.F15, F15Renderer::new);
        EntityRendererRegistry.register(AmracEntities.SU27, Su27Renderer::new);
        EntityRendererRegistry.register(AmracEntities.F4J, F4JRenderer::new);
        EntityRendererRegistry.register(AmracEntities.MIG29, Mig29Renderer::new);
        EntityRendererRegistry.register(AmracEntities.J10C, J10cRenderer::new);
        EntityRendererRegistry.register(AmracEntities.TYPHOON,
            TyphoonRenderer::new);
        EntityRendererRegistry.register(AmracEntities.MIG21, Mig21Renderer::new);
        EntityRendererRegistry.register(AmracEntities.J8II, J8IIRenderer::new);
        EntityRendererRegistry.register(AmracEntities.F15E, F15ERenderer::new);
        EntityRendererRegistry.register(AmracEntities.F18, F18Renderer::new);
        EntityRendererRegistry.register(AmracEntities.MIG23, Mig23Renderer::new);
        EntityRendererRegistry.register(AmracEntities.RAFALE, RafaleRenderer::new);
        EntityRendererRegistry.register(AmracEntities.SU30, Su30Renderer::new);
        EntityRendererRegistry.register(AmracEntities.MACHINE_GUN_BULLET,
            MachineGunBulletRenderer::new);
        EntityRendererRegistry.register(AmracEntities.IMPACT_TNT,
            TntRenderer::new);
        EntityRendererRegistry.register(AmracEntities.MISSILE,
            MissileRenderer::new);

        EntityRendererRegistry.register(AmracEntities.AI_PILOT,
            AiPilotRenderer::new);
        EntityRendererRegistry.register(AmracEntities.MISSILE_LOADER,
            MissileLoaderRenderer::new);
        MenuScreens.register(AmracMenus.AI_PILOT, AiPilotScreen::new);
        MenuScreens.register(AmracMenus.REFUELER, RefuelerScreen::new);
        MenuScreens.register(AmracMenus.CONSOLE,
            amrac.client.gui.ConsoleScreen::new);
        MenuScreens.register(AmracMenus.MISSILE_LOADER,
            MissileLoaderScreen::new);

        BlockEntityRendererRegistry.register(
            AmracBlockEntities.MISSILE_RACK, MissileRackRenderer::new);
        BlockEntityRendererRegistry.register(
            AmracBlockEntities.REFUELER, RefuelerRenderer::new);

        MissileTrails.register();
        amrac.client.render.RemoteAircraft.register();
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
            .END_CLIENT_TICK.register(client -> {
                if (client.level == null) {
                    MissileSeekerView.clear();
                } else if (!client.isPaused()) {
                    MissileSeekerView.tick();
                }
            });
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
            amrac.AmracBlockEntities.SCREEN,
            amrac.client.render.PanelRenderer::new);
        amrac.client.render.StructurePreviewRenderer.register();
        PlaneClientControls.register();
        PlaneEntity.setEngineSoundHook(PlaneSound::tryToPlay);
        PlaneEntity.setAirflowSoundHook(WindSound::tryToPlay);
        PlaneEntity.setAfterburnerSoundHook(AfterburnerSound::tryToPlay);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) {
                PlaneSound.clear();
                WindSound.clear();
                AfterburnerSound.clear();
                MissileThreats.clear();
                TransonicEffects.reset();
                CockpitWarnings.clear();
                LaunchSounds.reset();
                CountermeasureSounds.reset();
                PilotTolerance.reset();
                return;
            }
            PilotTolerance.tick(client);
            TransonicEffects.tick(client);
            CockpitWarnings.tick(client);
            LaunchSounds.tick(client);
            CountermeasureSounds.tick(client);
        });
        // The flight HUD must be its own HUD layer: GuiCrosshairMixin cancels the vanilla crosshair
        // layer, and anything attached after it is cancelled too.
        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath("amrac", "flight_hud"),
            PlaneHud::render);
        CockpitHud.suppressPlayerStatusInCockpit();
    }
}
