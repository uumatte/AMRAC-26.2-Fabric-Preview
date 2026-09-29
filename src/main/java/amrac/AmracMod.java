package amrac;

import net.fabricmc.api.ModInitializer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Register only inside onInitialize, straight into the vanilla registries (they freeze after
 * loading). Order is fixed: blocks, block entities, items (block entity types and BlockItems
 * reference blocks).
 */
public class AmracMod implements ModInitializer {
    public static final String MODID = "amrac";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public static void sendOverlay(Player player, Component message,
                                   boolean overlay) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(message, overlay);
        }
    }

    public static final String FLIGHT_MODEL_DIRECTORY = MODID + "/flightmodel";

    private static void loadFlightModels() {
        amrac.physics.aircraft.FlightModelRegistry
            .setErrorHandler((message, error) -> LOGGER.error(message, error));
        amrac.physics.aircraft.FlightModelRegistry.instance()
            .configure(net.fabricmc.loader.api.FabricLoader.getInstance()
                .getConfigDir().resolve(FLIGHT_MODEL_DIRECTORY));
        amrac.entities.ai.AiPilotSettingsFiles.register(
            net.fabricmc.loader.api.FabricLoader.getInstance()
                .getConfigDir().resolve(FLIGHT_MODEL_DIRECTORY));
    }

    @Override
    public void onInitialize() {
        AmracConfig.load();
        loadFlightModels();
        int flightModelDocuments = amrac.physics.aircraft
            .FlightModelRegistry.instance().documentsForSync().size();
        LOGGER.info("Flight-model document sync ready: {} document(s)",
            flightModelDocuments);
        AmracDataSerializers.register();
        AmracEntities.register();
        AmracBlocks.register();
        AmracBlockEntities.register();
        AmracMenus.register();
        amrac.entities.AircraftRegistry.register();
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry
            .register(AmracEntities.AI_PILOT,
                amrac.entities.ai.AiPilotEntity.createAttributes());
        AmracItems.register();
        AmracSounds.register();
        AmracCommands.register();
        amrac.network.PlaneNetworking.registerServerReceivers();
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN
            .register((handler, sender, server) ->
                amrac.network.PlaneNetworking
                    .sendFlightModel(handler.getPlayer()));
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT
            .register((handler, server) -> {
                java.util.UUID id = handler.getPlayer().getUUID();
                amrac.gps.GpsService.forget(id);
                amrac.runway.RunwayBuilder.forget(id);
                amrac.hangar.HangarBuilder.forget(id);
                amrac.structure.StructurePreview.forget(id);
            });
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED
            .register(amrac.gps.PinService::load);
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING
            .register(stopping -> amrac.gps.PinService.save());
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED
            .register(started -> amrac.physics.aircraft
                .FlightModelRegistry.instance().setChangeListener(() ->
                    started.execute(() -> amrac.network
                        .PlaneNetworking.broadcastFlightModel(started))));
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING
            .register(stopping -> amrac.physics.aircraft
                .FlightModelRegistry.instance().setChangeListener(null));
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK
            .register(server -> amrac.physics.aircraft
                .FlightModelRegistry.instance().refresh());

        amrac.entities.PlaneRadarService.register();
        amrac.weapons.VirtualMissileService.register();
        amrac.weapons.MissileWarningService.register();
        amrac.weapons.MissileTrackService.register();
        amrac.weapons.CountermeasureService.register();
        amrac.weapons.MissileSeekerService.register();
        amrac.trace.BvrTraceRecorder.register();
        amrac.entities.ai.AiPilotService.register();
        amrac.entities.ai.AiCommandLaunchService.register();
        amrac.entities.ai.WorldBoundaryWarningService.register();
        amrac.entities.AircraftVirtualService.register();
        amrac.entities.AircraftTrackService.register();
        amrac.entities.AircraftUpkeepService.register();
        amrac.entities.AircraftPurgeService.register();
        amrac.runway.RunwayBuilder.register();
        amrac.hangar.HangarBuilder.register();
        amrac.structure.StructurePreview.register();
        LOGGER.info("AMRAC initialised");
    }
}
