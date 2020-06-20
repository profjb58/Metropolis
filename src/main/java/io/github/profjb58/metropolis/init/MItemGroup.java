package io.github.profjb58.metropolis.init;

import io.github.profjb58.metropolis.Metropolis;
import io.github.profjb58.metropolis.Reference;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public final class MItemGroup extends ItemGroup {

    public MItemGroup(String label) {
        super(Metropolis.MOD_ID + "." + label);
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(Reference.QUARTZ_MARKER.asItem());
    }
}
