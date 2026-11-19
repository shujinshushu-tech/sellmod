package com.example.sellmod;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import java.util.List;

public class SellPrices {

    public record SellEntry(Item item, int requiredAmount, int emeraldReward) {}

    // Edit your item prices here:
    public static final List<SellEntry> ENTRIES = List.of(
        new SellEntry(Items.COBBLESTONE, 64, 1), // 64 Cobblestone -> 1 Emerald
        new SellEntry(Items.OAK_LOG, 32, 1),     // 32 Oak Logs -> 1 Emerald
        new SellEntry(Items.WHEAT, 32, 1),       // 32 Wheat -> 1 Emerald
        new SellEntry(Items.COAL, 16, 1),        // 16 Coal -> 1 Emerald
        new SellEntry(Items.IRON_INGOT, 8, 1),   // 8 Iron Ingots -> 1 Emerald
        new SellEntry(Items.GOLD_INGOT, 4, 1),   // 4 Gold Ingots -> 1 Emerald
        new SellEntry(Items.DIAMOND, 1, 2)       // 1 Diamond -> 2 Emeralds
    );
}
