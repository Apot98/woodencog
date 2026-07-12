package net.chauvedev.woodencog.contraption;

import com.simibubi.create.api.contraption.BlockMovementChecks;
import net.dries007.tfc.common.blocks.CharcoalPileBlock;
import net.dries007.tfc.common.blocks.devices.LogPileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class WoodenCogBlockMovementChecks {

    public static class WoodenCogBrittleCheck implements BlockMovementChecks.BrittleCheck {
        @Override
        public BlockMovementChecks.CheckResult isBrittle(BlockState state) {
            if (state.getBlock() instanceof LogPileBlock) {
                return BlockMovementChecks.CheckResult.SUCCESS;
            }
            else if (state.getBlock() instanceof CharcoalPileBlock) {
                return BlockMovementChecks.CheckResult.SUCCESS;
            }
            else {
                return BlockMovementChecks.CheckResult.PASS;
            }
        }
    }

    public static class WoodenCogAttachedCheck implements BlockMovementChecks.AttachedCheck {
        @Override
        public BlockMovementChecks.CheckResult isBlockAttachedTowards(BlockState state, Level world, BlockPos pos, Direction direction) {
            if (state.getBlock() instanceof LogPileBlock) {
                return BlockMovementChecks.CheckResult.of(direction == Direction.DOWN);
            }
            else if (state.getBlock() instanceof CharcoalPileBlock) {
                return BlockMovementChecks.CheckResult.of(direction == Direction.DOWN);
            }
            else {
                return BlockMovementChecks.CheckResult.PASS;
            }
        }
    }

}
