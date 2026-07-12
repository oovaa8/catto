package com.oovaa8.catto.opencl;

import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import com.oovaa8.catto.density_functions.ErosionFilterOCL;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

//So we have come full circle
public class ErosionEmitter implements OpenCLCEmitter<ErosionNode>{
    public static final ErosionEmitter INSTANCE = new ErosionEmitter();

    private final Set<OpenCLCGenContext> helpersEmitted =
            Collections.newSetFromMap(new WeakHashMap<>());

    private ErosionEmitter() {
    }

    @Override
    public String doCLGen(ErosionNode node, OpenCLCGenContext context) {
        ValuesMethodDefD height = context.newMethod(node.height);
        ValuesMethodDefD strength = context.newMethod(node.strength);
        ValuesMethodDefD scale = context.newMethod(node.scale);
        ValuesMethodDefD ridgeRounding = context.newMethod(node.ridgeRounding);
        ValuesMethodDefD creaseRounding = context.newMethod(node.creaseRounding);
        ValuesMethodDefD gain = context.newMethod(node.gain);

        String hCall = context.callDelegate(height);
        String strengthCall = context.callDelegate(strength);
        String scaleCall = context.callDelegate(scale);
        String ridgeRoundingCall = context.callDelegate(ridgeRounding);
        String creaseRoundingCall = context.callDelegate(creaseRounding);
        String gainCall = context.callDelegate(gain);

        if(INSTANCE.helpersEmitted.add(context)){
            context.appendRaw(HELPER_FUNCTIONS);
            emitBase(context, hCall, strengthCall, scaleCall, ridgeRoundingCall, creaseRoundingCall, gainCall, node);
        }
        return  "float4 base = erosion_base(ctx);\n" +
                "return " + switch (node.mode) {
                    case "ridge"   -> "base.y;\n";
                    case "slope_length" -> "base.z;\n";
                    case "slope_z" -> "base.w;\n";
                    default        -> "base.x;\n";};


    }

    private void emitBase(OpenCLCGenContext context,
                            String hCall, String strengthCall, String scaleCall,
                            String ridgeRoundingCall, String creaseRoundingCall, String gainCall,
                            ErosionNode node) {

        String hCallDx = hCall.replace("ctx", "ctx_dx");
        String hCallDz = hCall.replace("ctx", "ctx_dz");

        context.appendRaw(
                    "static __attribute__((pure)) float4 erosion_base(sample_int32_ctx_t ctx) {\n" +
                            "float height = " + hCall + ";\n" +
                            "sample_int32_ctx_t ctx_dx = make_sample_int32_ctx(ctx.const_data, ctx.rw_data, ctx.x + 1, ctx.y, ctx.z, ctx.sample_flags);\n" +
                            "float hdx = " + hCallDx + ";\n" +
                            "sample_int32_ctx_t ctx_dz = make_sample_int32_ctx(ctx.const_data, ctx.rw_data, ctx.x, ctx.y, ctx.z + 1, ctx.sample_flags);\n" +
                            "float hdz = " + hCallDz + ";\n" +
                            "float2 slope = (float2)(hdx - height, hdz - height);\n" +
                            "float3 heightAndSlope = (float3)(height, slope * " + Float.toHexString((float) node.slopeScale) + "f);\n" +
                            "float fadeTarget = clamp(height / 0.6, -1.0, 1.0);\n" +
                            "float ridgeMap = 0.0;\n" +
                            "float4 erosion = ErosionFilter(\n" +
                            "(float2)(ctx.x, ctx.z), heightAndSlope, fadeTarget,\n" +
                            strengthCall + ", 0.5, " + Float.toHexString((float) node.detail) + ",\n" +
                            "(float4)(" + ridgeRoundingCall + ", " + creaseRoundingCall + ", " +
                            Float.toHexString((float) node.rounding_z) + ", " + Float.toHexString((float) node.rounding_w) + "),\n" +
                            "(float4)(1.25, 1.25, 2.8, 1.5),\n" +
                            "(float2)(" + Float.toHexString((float) node.assumedSlope) + ", " +
                            Float.toHexString((float) node.assumedSlope_weight) + "),\n" +
                            scaleCall + ", " + node.octaves + ", " +
                            Float.toHexString((float) node.lacunarity) + ", " + gainCall + ",\n" +
                            "0.8f, " + Float.toHexString((float) node.normalization) + ", &ridgeMap);\n" +
                            "return (float4)(height + erosion.x + erosion.w * 0.75f, ridgeMap, length((float2)(slope.x + erosion.y, slope.y + erosion.z)), slope.y + erosion.z);\n" +
                    "}\n"
        );
    }


    private static final String HELPER_FUNCTIONS =
"""
#define TAU 6.28318530717959f

static float2 hash_erosion(float2 x){
    float2 k = (float2)(0.3183099f, 0.3678794f);
    x = x * k + k.yx;
    float2 l;
    return -1.0f + 2.0f * fract(16.0f * k * fract(x.x * x.y * (x.x + x.y), &l), &l);
}
""" + ErosionFilterOCL.EROSION;
}
