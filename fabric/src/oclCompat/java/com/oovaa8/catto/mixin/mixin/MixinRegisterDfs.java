package com.oovaa8.catto.mixin.ocl;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.McToAst;
import com.ishland.c2me.opts.dfc.common.ast.misc.CoordinateNode;
import com.oovaa8.catto.density_functions.*;
import com.oovaa8.catto.opencl.CliffsInvertedNode;
import com.oovaa8.catto.opencl.CliffsNode;
import com.oovaa8.catto.opencl.ErosionNode;
import com.oovaa8.catto.opencl.SteepnessNode;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.ishland.c2me.opts.dfc.common.ast.McToAst.toAst;

@Mixin(McToAst.class)
public class MixinRegisterDfs {
    @Inject(method = "toAst", at = @At("HEAD"), cancellable = true)
    private static void onToAst(DensityFunction df, CallbackInfoReturnable<AstNode> cir) {
        switch (df) {
            case Erosion f ->
                    cir.setReturnValue(new ErosionNode(df, toAst(f.height), toAst(f.strength), toAst(f.scale), (float) f.detail, toAst(f.ridgeRounding), toAst(f.creaseRounding), (float) f.rounding_z, (float) f.rounding_w, (float) f.assumedSlope,  (float) f.assumedSlope_weight, (float) f.normalization,  f.octaves, (float) f.lacunarity, toAst(f.gain), (float) f.slopeScale, f.mode));
            case Steepness(
                    DensityFunction height, DensityFunction steepness, DensityFunction flatness, DensityFunction length,
                    DensityFunction noise, int worldHeight, double ocean
            ) -> cir.setReturnValue(new SteepnessNode(df, toAst(height), toAst(steepness), toAst(flatness), toAst(length), toAst(noise), worldHeight, (float) ocean));
            case Cliffs(DensityFunction height, DensityFunction length, int worldHeight, double ocean) -> cir.setReturnValue(new CliffsNode(df, toAst(height), toAst(length), worldHeight, (float) ocean));
            case CliffsInverted(
                    DensityFunction height, DensityFunction cliffs, DensityFunction rawHeight, DensityFunction length,
                    DensityFunction flatness, DensityFunction steepness, DensityFunction noise, double ocean,
                    int worldHeight, int minY
            ) -> cir.setReturnValue(new CliffsInvertedNode(df, toAst(height), toAst(cliffs), toAst(rawHeight), toAst(length), toAst(flatness), toAst(steepness), toAst(noise), worldHeight, (float) ocean, minY));
            case CoordinateX() -> cir.setReturnValue(new CoordinateNode(CoordinateNode.Axis.X));
            case CoordinateY() -> cir.setReturnValue(new CoordinateNode(CoordinateNode.Axis.Y));
            case CoordinateZ() -> cir.setReturnValue(new CoordinateNode(CoordinateNode.Axis.Z));
            default -> {}
        }
    }
}
