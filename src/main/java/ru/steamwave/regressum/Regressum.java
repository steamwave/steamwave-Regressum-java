package ru.steamwave.regressum;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.steamwave.regressum.commands.*;
import ru.steamwave.regressum.config.RegressumConfig;
import ru.steamwave.regressum.db.DbManager;
import ru.steamwave.regressum.events.*;
import ru.steamwave.regressum.storage.AsyncDbWriter;
import ru.steamwave.regressum.storage.CacheManager;

@Mod(Regressum.MOD_ID)
public class Regressum {
    public static final String MOD_ID = "regressum";
    public static final Logger LOGGER = LogManager.getLogger();

    private DbManager dbManager;
    private AsyncDbWriter dbWriter;

    public Regressum(IEventBus modEventBus, ModContainer modContainer) {
        // 1. Регистрация конфига (обязательно первой)
        modContainer.registerConfig(ModConfig.Type.COMMON, RegressumConfig.SPEC);

        // 2. Жизненный цикл мода
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::commonSetup);

        // 3. Системные события сервера
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // enqueueWork гарантирует поток безопасности при обращении к ресурсам игры
        event.enqueueWork(() -> {
            try {
                LOGGER.info("Initializing Regressum Database and Cache...");

                // Инициализация менеджера и попытка подключения
                // Если в DbConnect.init() стоит halt(1), сервер упадет тут при ошибке БД
                dbManager = new DbManager();
                dbManager.getDbConnect().init();

                // Запуск асинхронного писателя (теперь conn точно не null)
                dbWriter = new AsyncDbWriter(dbManager);
                dbWriter.start();

                // Инициализация кеша
                CacheManager.init(50, 100, 10 * 60 * 1000, 60 * 1000);

                // 4. РЕГИСТРАЦИЯ СОБЫТИЙ (теперь это безопасно)
                // Если твои эвенты требуют dbManager в конструкторе — передавай его здесь
                NeoForge.EVENT_BUS.register(new BlockEvents(dbManager));
                NeoForge.EVENT_BUS.register(new ContainerCleanupEvents(dbManager));
                NeoForge.EVENT_BUS.register(new EntityEvents(dbManager));
                NeoForge.EVENT_BUS.register(new ItemEvents(dbManager));
                NeoForge.EVENT_BUS.register(new InspectorEvents());

                LOGGER.info("Regressum logic initialized successfully!");
            } catch (Exception e) {
                LOGGER.fatal("Failed to initialize Regressum! Shutting down server...", e);
                Runtime.getRuntime().halt(1); // Жесткий стоп, если БД не завелась
            }
        });
    }

    private void setup(FMLClientSetupEvent event) {
        LOGGER.info("Client setup done");
    }

    private void onServerStarting(ServerStartingEvent event) {
        var dispatcher = event.getServer().getCommands().getDispatcher();

        InspectCommand.register(dispatcher);
        DbCommands.register(dispatcher, dbManager);
        SetupCommand.register(dispatcher, dbManager);
        ClearDbCommand.register(dispatcher, dbManager);
        InspectPageCommand.register(dispatcher);

        LOGGER.info("All commands registered!");
    }

    private void onServerStarted(ServerStartedEvent event) {
        LOGGER.info("Server started!");
    }

    private void onServerStopping(ServerStoppingEvent event) {
        stop();
    }

    public void stop() {
        LOGGER.info("Stopping Regressum...");
        if (dbWriter != null) dbWriter.shutdown();
        if (dbManager != null) dbManager.close();
        if (CacheManager.getInstance() != null) CacheManager.getInstance().shutdown();
        LOGGER.info("Regressum stopped!");
    }
}