package com.jerrymod;

import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class StarterKit {
    private static final int[] BONE_SLOTS = {0, 3, 5, 8, 10, 16, 19, 22, 24, 26}; // 10 huesos repartidos

    public static void place(ServerPlayerEntity p) {
        ServerWorld w = p.getServerWorld();
        Direction d = p.getHorizontalFacing();
        BlockPos pos = p.getBlockPos().offset(d, 2);

        for (int i = 0; i < 6 && !w.getBlockState(pos).isReplaceable(); i++) pos = pos.up();
        for (int i = 0; i < 6 && w.getBlockState(pos.down()).isReplaceable(); i++) pos = pos.down();

        w.setBlockState(pos, Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, d.getOpposite()));

        // Antorchas a los dos lados del cofre
        for (Direction side : new Direction[]{d.rotateYClockwise(), d.rotateYCounterclockwise()}) {
            BlockPos t = pos.offset(side);
            if (w.getBlockState(t).isReplaceable()) {
                if (w.getBlockState(t.down()).isReplaceable()) {
                    w.setBlockState(t.down(), Blocks.COBBLESTONE.getDefaultState());
                }
                w.setBlockState(t, Blocks.TORCH.getDefaultState());
            }
        }

        if (w.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            chest.setStack(13, new ItemStack(JerryMod.JERRY_EGG));
            for (int slot : BONE_SLOTS) chest.setStack(slot, new ItemStack(Items.BONE));
            chest.markDirty();
        }
    }
}
