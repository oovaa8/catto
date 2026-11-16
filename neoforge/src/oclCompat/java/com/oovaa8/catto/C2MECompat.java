package com.oovaa8.catto;

import com.ishland.c2me.opts.dfc.common.ast.McToAst;
import com.ishland.c2me.opts.dfc.common.gen.dot.DotEmitter;
import com.ishland.c2me.opts.dfc.common.gen.dot.DotGenRegistry;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGenRegistry;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.DelegateNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenData;
import com.oovaa8.catto.density_functions.*;
import com.oovaa8.catto.misc.SeedHolder;
import com.oovaa8.catto.opencl.*;

import static com.ishland.c2me.opts.dfc.common.ast.McToAst.toAst;

public final class C2MECompat {

    private C2MECompat() {
    }

    public static void register() {
        OpenCLCGenData.REGISTRY.registerExactMatch(ErosionNode.class, ErosionEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(SteepnessNode.class, SteepnessEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(CliffsNode.class, CliffsEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(CliffsInvertedNode.class, CliffsInvertedEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(ContinentNode.class, ContinentEmitter.INSTANCE);

        BytecodeGenRegistry.REGISTRY.registerExactMatch(ErosionNode.class, DelegateNodeBytecodeEmitter.instance());
        BytecodeGenRegistry.REGISTRY.registerExactMatch(SteepnessNode.class, DelegateNodeBytecodeEmitter.instance());
        BytecodeGenRegistry.REGISTRY.registerExactMatch(CliffsNode.class, DelegateNodeBytecodeEmitter.instance());
        BytecodeGenRegistry.REGISTRY.registerExactMatch(CliffsInvertedNode.class, DelegateNodeBytecodeEmitter.instance());
        BytecodeGenRegistry.REGISTRY.registerExactMatch(ContinentNode.class, DelegateNodeBytecodeEmitter.instance());

        McToAst.REGISTRY.registerExactMatch(Erosion.class, f -> new ErosionNode(f, toAst(f.height), toAst(f.strength), toAst(f.scale), f.detail, toAst(f.ridgeRounding), toAst(f.creaseRounding), f.rounding_z, f.rounding_w, f.assumedSlope, f.assumedSlope_weight, f.normalization, f.octaves, f.lacunarity, toAst(f.gain), f.slopeScale, f.mode));
        McToAst.REGISTRY.registerExactMatch(Steepness.class, f -> new SteepnessNode(f, toAst(f.height()), toAst(f.steepness()), toAst(f.flatness()), toAst(f.length()), toAst(f.noise()), f.worldHeight(), f.ocean()));
        McToAst.REGISTRY.registerExactMatch(Cliffs.class, f -> new CliffsNode(f, toAst(f.height()), toAst(f.length()), f.worldHeight(), f.ocean()));
        McToAst.REGISTRY.registerExactMatch(CliffsInverted.class, f -> new CliffsInvertedNode(f, toAst(f.height()),toAst(f.cliffs()),toAst(f.raw_height()), toAst(f.length()), toAst(f.flatness()), toAst(f.steepness()), toAst(f.noise()), f.worldHeight(), f.ocean(), f.min_y()));
        McToAst.REGISTRY.registerExactMatch(Continent.class, f -> new ContinentNode(f, toAst(f.noise()), SeedHolder.seed));

        DotGenRegistry.REGISTRY.registerExactMatch(
                ErosionNode.class,
                (DotEmitter<ErosionNode>) (node, context, builder) ->
                        builder.trapeziumShape()
                                .label("Erosion delegate " + node.getDelegate())
                                .build()
        );

        DotGenRegistry.REGISTRY.registerExactMatch(
                SteepnessNode.class,
                (DotEmitter<SteepnessNode>) (node, context, builder) ->
                        builder.trapeziumShape()
                                .label("Steepness delegate " + node.getDelegate())
                                .build()
        );

        DotGenRegistry.REGISTRY.registerExactMatch(
                CliffsNode.class,
                (DotEmitter<CliffsNode>) (node, context, builder) ->
                        builder.trapeziumShape()
                                .label("Cliffs delegate " + node.getDelegate())
                                .build()
        );

        DotGenRegistry.REGISTRY.registerExactMatch(
                CliffsInvertedNode.class,
                (DotEmitter<CliffsInvertedNode>) (node, context, builder) ->
                        builder.trapeziumShape()
                                .label("CliffsInverted delegate " + node.getDelegate())
                                .build()
        );

        DotGenRegistry.REGISTRY.registerExactMatch(
                ContinentNode.class,
                (DotEmitter<ContinentNode>) (node, context, builder) ->
                        builder.trapeziumShape()
                                .label("ContinentNode delegate " + node.getDelegate())
                                .build()
        );
    }
}