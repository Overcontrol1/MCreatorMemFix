package com.overcontrol1.mcreatormemfix;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Mod(MCreatorMemFix.MOD_ID)
public class MCreatorMemFix {
    public static final Logger LOGGER = LoggerFactory.getLogger("MCreatorMemFix");
    public static final String MOD_ID = "mcreator_mem_fix";


    public MCreatorMemFix(FMLJavaModLoadingContext ctx) {
        ctx.getModEventBus().addListener(this::onCommonSetup);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        if (ModConfig.REGENERATION_LOCKED) {
            ModConfig.REQUIRES_REGENERATION = false;
            return;
        }

        ModConfig config = ModConfig.instance();
        if (config.spec() != ModConfig.CURRENT_SPEC)
            ModConfig.REQUIRES_REGENERATION = true;

        long modHash = hashMods();

        if (config.hash() != modHash)
            ModConfig.REQUIRES_REGENERATION = true;

        if (ModConfig.REQUIRES_REGENERATION) {
            final List<ModConfigEntry> entries = findEntries();
            final ModConfig newConfig = new ModConfig(entries, ModConfig.CURRENT_SPEC, modHash);
            newConfig.save();

            LOGGER.warn("[MCreatorMemFix] Scanned {} MCreator mods in this environment. RESTART RECOMMENDED.",
                    entries.size());
        }
    }

    private static List<ModConfigEntry> findEntries() {
        final List<ModConfigEntry> entries = new ArrayList<>();
        ModList.get().forEachModContainer((id, container) -> {
            Object mod = container.getMod();
            Class<?> clazz = mod.getClass();
            String simpleName = clazz.getSimpleName();
            String packageName = clazz.getPackageName();
            String variableClassName = packageName + ".network." + simpleName + "Variables$PlayerVariables";

            try {
                MixinService.getService()
                        .getBytecodeProvider().getClassNode(variableClassName);

                String simpleNameWithoutMod = simpleName.substring(0, simpleName.length() - 3);

                entries.add(new ModConfigEntry(packageName, simpleNameWithoutMod));
            } catch (IOException | ClassNotFoundException ignored) {

            }
        });
        return entries;
    }

    private static long hashMods() {
        long result = 17;
        for (IModInfo info : ModList.get().getMods()) {
            result = 37*result + info.getModId().hashCode();
        }
        return result;
    }
}
