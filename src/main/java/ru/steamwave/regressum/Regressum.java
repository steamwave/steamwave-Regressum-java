package ru.steamwave.regressum;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.bus.api.IEventBus; // Правильный импорт для NeoForge 1.21.1

import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;
import ru.steamwave.regressum.commands.*;

import ru.steamwave.regressum.events.*;
import ru.steamwave.regressum.storage.CacheManager;
import ru.steamwave.regressum.storage.AsyncDbWriter;
import ru.steamwave.regressum.db.DbManager;



@Mod(Regressum.MOD_ID)
public class Regressum {
    public static final String MOD_ID = "regressum";

    private final DbManager dbManager;
    private final AsyncDbWriter dbWriter;
    public static final Logger LOGGER = LogManager.getLogger();

    public Regressum(IEventBus modEventBus) {



        // Регистрация событий мода
        modEventBus.addListener(this::setup); // Регистрация клиентской настройки
        NeoForge.EVENT_BUS.addListener(this::onServerStarting); // Регистрация команд на старте сервера
        NeoForge.EVENT_BUS.addListener(this::onServerStarted); // Регистрация события старта сервера
        NeoForge.EVENT_BUS.register(new BlockEvents());
        NeoForge.EVENT_BUS.register(new ContainerCleanupEvents());
        NeoForge.EVENT_BUS.register(new EntityEvents());
        NeoForge.EVENT_BUS.register(new ItemEvents());
        NeoForge.EVENT_BUS.register(new InspectorEvents());


        // Регистрация обработчика событий Block

        // Инициализация базы данных и writer
        dbManager = new DbManager();
        dbWriter = new AsyncDbWriter(dbManager);
        dbWriter.start();

        // Инициализация кеша (макс. 50 записей на позицию, 100 на игрока, TTL 10 мин, очистка каждые 1 мин)
        CacheManager.init(50, 100, 10 * 60 * 1000, 60 * 1000);
    }

    // Клиентская настройка
    private void setup(FMLClientSetupEvent event) {
        System.out.println("Client setup done");
    }

    // Регистрация команд при старте сервера
    private void onServerStarting(ServerStartingEvent event) {
        InspectCommand.register(event.getServer().getCommands().getDispatcher());
        DbCommands.register(event.getServer().getCommands().getDispatcher(), dbManager);
        SetupCommand.register(event.getServer().getCommands().getDispatcher(), dbManager);
        ClearDbCommand.register(event.getServer().getCommands().getDispatcher(), dbManager);
        InspectPageCommand.register(event.getServer().getCommands().getDispatcher());
        LOGGER.info("All commands registered!");
    }

    // Событие старта сервера
    private void onServerStarted(ServerStartedEvent event) {
        LOGGER.info("Server started!");
    }
    private void onServerStopping(ServerStoppingEvent event) {
        dbManager.close();
        LOGGER.info("Server stopped!");
    }

    // Остановка мода
    public void stop() {
        if (dbWriter != null) dbWriter.shutdown();
        if (CacheManager.getInstance() != null) CacheManager.getInstance().shutdown();
        LOGGER.info("Regressum stopped!");
    }

}