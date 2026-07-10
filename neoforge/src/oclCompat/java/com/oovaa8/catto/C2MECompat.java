package com.oovaa8.catto;

import com.ishland.c2me.opts.dfc.common.gen.dot.DotEmitter;
import com.ishland.c2me.opts.dfc.common.gen.dot.DotGenRegistry;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGenRegistry;
import com.ishland.c2me.opts.dfc.common.gen.jvm.emitters.misc.DelegateNodeBytecodeEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenData;
import com.oovaa8.catto.opencl.*;

public final class C2MECompat {

    private C2MECompat() {
    }

    public static void register() {
        OpenCLCGenData.REGISTRY.registerExactMatch(ErosionNode.class, ErosionEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(SteepnessNode.class, SteepnessEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(CliffsNode.class, CliffsEmitter.INSTANCE);
        OpenCLCGenData.REGISTRY.registerExactMatch(CliffsInvertedNode.class, CliffsInvertedEmitter.INSTANCE);

        BytecodeGenRegistry.REGISTRY.registerExactMatch(ErosionNode.class, DelegateNodeBytecodeEmitter.instance());
        BytecodeGenRegistry.REGISTRY.registerExactMatch(SteepnessNode.class, DelegateNodeBytecodeEmitter.instance());
        BytecodeGenRegistry.REGISTRY.registerExactMatch(CliffsNode.class, DelegateNodeBytecodeEmitter.instance());
        BytecodeGenRegistry.REGISTRY.registerExactMatch(CliffsInvertedNode.class, DelegateNodeBytecodeEmitter.instance());

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
    }
}