package com.example.sellmod;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class SellMod implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            dispatcher.register(CommandManager.literal("sell")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) return 0;

                    player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                        (syncId, playerInventory, playerEntity) -> new SellGuiHandler(syncId, playerInventory),
                        Text.literal("Sell Shop (Hover for Prices)")
                    ));
                    return Command.SINGLE_SUCCESS;
                }));

            dispatcher.register(CommandManager.literal("sellprices")
                .executes(context -> {
                    context.getSource().sendMessage(Text.literal("§6§l=== Emerald Sell Prices ==="));
                    for (SellPrices.SellEntry entry : SellPrices.ENTRIES) {
                        context.getSource().sendMessage(Text.literal(
                            "§e" + entry.requiredAmount() + "x " + entry.item().getName().getString()
                            + " §7-> §a" + entry.emeraldReward() + " Emerald(s)"
                        ));
                    }
                    return Command.SINGLE_SUCCESS;
                }));
        });
    }
}
