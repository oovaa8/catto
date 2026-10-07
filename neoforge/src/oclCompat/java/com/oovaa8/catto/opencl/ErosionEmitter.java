package com.oovaa8.catto.opencl;

import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF32;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenFunctionContext;
import com.oovaa8.catto.density_functions.ErosionFilterOCL;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import static com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen.literal;

//So we have come full circle
public class ErosionEmitter implements OpenCLCEmitter<ErosionNode>{
    public static final ErosionEmitter INSTANCE = new ErosionEmitter();

    private final Set<OpenCLCGenFunctionContext> helpersEmitted =
            Collections.newSetFromMap(new WeakHashMap<>());

    private ErosionEmitter() {
    }

    @Override
    public String doCLGen(ErosionNode node, OpenCLCGenFunctionContext context, String s) {
        ValuesMethodDefF64 height = context.newVarF64(node.height);
        ValuesMethodDefF64 strength = context.newVarF64(node.strength);
        ValuesMethodDefF64 scale = context.newVarF64(node.scale);
        ValuesMethodDefF64 ridgeRounding = context.newVarF64(node.ridgeRounding);
        ValuesMethodDefF64 creaseRounding = context.newVarF64(node.creaseRounding);
        ValuesMethodDefF64 gain = context.newVarF64(node.gain);

        String hCall = "(float)(" + context.getDelegateVar(height) + ")";
        String strengthCall = "(float)(" + context.getDelegateVar(strength) + ")";
        String scaleCall = "(float)(" + context.getDelegateVar(scale) + ")";
        String ridgeRoundingCall = "(float)(" + context.getDelegateVar(ridgeRounding) + ")";
        String creaseRoundingCall = "(float)(" + context.getDelegateVar(creaseRounding) + ")";
        String gainCall = "(float)(" + context.getDelegateVar(gain) + ")";

        if(INSTANCE.helpersEmitted.add(context)){
            context.getGlobalContext().appendRaw(HELPER_FUNCTIONS);
            emitBase(context.getGlobalContext(), hCall, strengthCall, scaleCall, ridgeRoundingCall, creaseRoundingCall, gainCall, node);
        }
        return  "float4 base = erosion_base(ctx);\n" +
                s + " = " + switch (node.mode) {
                    case "ridge"   -> "base.y;\n";
                    case "slope_length" -> "base.z;\n";
                    case "slope_z" -> "base.w;\n";
                    default        -> "base.x;\n";};


    }

    private void emitBase(OpenCLCGenContext context,
                            String hCall, String strengthCall, String scaleCall,
                            String ridgeRoundingCall, String creaseRoundingCall, String gainCall,
                            ErosionNode node) {

        // fun fact this is extremely unsafe and WILL cause a crash if I flat/interpolate anything under this function
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
                            "float3 heightAndSlope = (float3)(height, slope * " + literal(node.slopeScale) + ");\n" +
                            "float fadeTarget = clamp(height / 0.6, -1.0, 1.0);\n" +
                            "float ridgeMap = 0.0;\n" +
                            "float4 erosion = ErosionFilter(\n" +
                            "(float2)(ctx.x, ctx.z), heightAndSlope, fadeTarget,\n" +
                            strengthCall + ", 0.5, " + literal(node.detail) + ",\n" +
                            "(float4)(" + ridgeRoundingCall + ", " + creaseRoundingCall + ", " +
                            literal(node.rounding_z) + ", " + literal(node.rounding_w) + "),\n" +
                            "(float4)(1.25, 1.25, 2.8, 1.5),\n" +
                            "(float2)(" + literal(node.assumedSlope) + ", " +
                            literal(node.assumedSlope_weight) + "),\n" +
                            scaleCall + ", " + node.octaves + ", " +
                            literal(node.lacunarity) + ", " + gainCall + ",\n" +
                            "0.8f, " + literal(node.normalization) + ", &ridgeMap);\n" +
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
