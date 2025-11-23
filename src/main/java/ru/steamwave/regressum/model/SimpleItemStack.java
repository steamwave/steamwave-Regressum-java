package ru.steamwave.regressum.model;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SimpleItemStack {
    private final String itemId;
    private final int count;

    public SimpleItemStack(ItemStack stack) {
        // Получаем ResourceLocation для предмета и преобразуем в строку
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        this.itemId = key != null ? key.toString() : "minecraft:air";
        this.count = stack.getCount();
    }

    public SimpleItemStack(CompoundTag tag) {
        this.itemId = tag.getString("itemId");
        this.count = tag.getInt("count");
    }

    public ItemStack toItemStack() {
        ResourceLocation key = ResourceLocation.tryParse(this.itemId);

        // Если что-то не так с itemId — возвращаем пустой стак AIR
        if (key == null) {
            return new ItemStack(Items.AIR, this.count);
        }

        // В 1.21.1 get(ResourceLocation) может не сработать — используем getOptional
        Item item = BuiltInRegistries.ITEM.getOptional(key).orElse(Items.AIR);
        return new ItemStack(item, this.count);
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("itemId", this.itemId);
        tag.putInt("count", this.count);
        return tag;
    }

    public String getItemId() {
        return this.itemId;
    }

    public int getCount() {
        return this.count;
    }
}
