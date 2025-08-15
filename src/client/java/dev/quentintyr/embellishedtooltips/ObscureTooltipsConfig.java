package com.obscuria.tooltips;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.loading.FMLPaths;

public final class ObscureTooltipsConfig {
   public static void setup() {
      Path configPath = FMLPaths.CONFIGDIR.get();
      Path modConfigPath = Paths.get(configPath.toAbsolutePath().toString(), "Obscuria");

      try {
         Files.createDirectory(modConfigPath);
      } catch (FileAlreadyExistsException var3) {
      } catch (Exception var4) {
         ObscureTooltips.LOGGER.error("Failed to create Obscuria config directory", var4);
      }

      ModLoadingContext.get().registerConfig(Type.CLIENT, ObscureTooltipsConfig.Client.CLIENT_SPEC, "Obscuria/obscure-tooltips-client.toml");
   }

   public static class Client {
      public static final Builder BUILDER = new Builder();
      public static final ForgeConfigSpec CLIENT_SPEC;
      public static final BooleanValue displayArmorModels;
      public static final BooleanValue displayToolModels;

      static {
         BUILDER.push("Settings");
         displayArmorModels = BUILDER.worldRestart().define("displayArmorModels", true);
         displayToolModels = BUILDER.worldRestart().define("displayToolsModels", true);
         BUILDER.pop();
         CLIENT_SPEC = BUILDER.build();
      }
   }
}
