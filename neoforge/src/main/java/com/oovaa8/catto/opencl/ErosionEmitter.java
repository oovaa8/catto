package com.oovaa8.catto.opencl;

import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;

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

    public static final Object MARKER_cacheLike_erosion = new Object();

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
            context.allocGlobalDynamicData(MARKER_cacheLike_erosion);
        }

        int offset = context.getGlobalDynamicDataOffset(MARKER_cacheLike_erosion);

        return "if (ctx.rw_data){\n" +
                "    global const worldgen_params_t * restrict params = ctx.rw_data;\n" +
                "    global const float4 * restrict data = df_data_offset_global(ctx.rw_data, " + offset + ");\n" +
                "    const erosion_result_t res = df_cachelike_erosion(params, data, ctx.x, ctx.z);\n" +
                "    if (res.cached) {\n" +
                "       return " + switch (node.mode) {
                        case "ridge"   -> "res.res.y;\n";
                        case "slope_length" -> "res.res.z;\n";
                        case "slope_z" -> "res.res.w;\n";
                        default        -> "res.res.x;\n";} +
                "    }\n" +
                "}" +
                "float4 base = erosion_base(ctx);\n" +
                "if (ctx.rw_data) {\n" +
                "    global const worldgen_params_t * restrict params = ctx.rw_data;\n" +
                "    global float4 * restrict data = df_data_offset_global(ctx.rw_data, " + offset + ");\n" +
                "    df_write_erosion(params, base, data, ctx.x, ctx.z);\n" +
                "}\n" +
                "return " + switch (node.mode) {
                    case "ridge"   -> "base.y;\n";
                    case "slope_length" -> "base.z;\n";
                    case "slope_z" -> "base.w;\n";
                    default        -> "base.x;\n";
        };/*
        return  "float4 base = erosion_base(ctx);\n" +
                "return " + switch (node.mode) {
                    case "ridge"   -> "base.y;\n";
                    case "slope_x" -> "base.z;\n";
                    case "slope_z" -> "base.w;\n";
                    default        -> "base.x;\n";};*/


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


    private static String HELPER_FUNCTIONS = """
typedef struct erosion_result {
    bool cached;
    float4 res;
} erosion_result_t;

static inline bool cache_contains(
        global const worldgen_params_t *params,
        int x,
        int z)
{
    return (uint32_t)(x - params->cache2d_startX) < (uint32_t)params->cache2d_sizeX &&
           (uint32_t)(z - params->cache2d_startZ) < (uint32_t)params->cache2d_sizeZ;
}

static inline size_t cache_index(
        global const worldgen_params_t *params,
        int x,
        int z)
{
    const size_t localX = (size_t)(x - params->cache2d_startX);
    const size_t localZ = (size_t)(z - params->cache2d_startZ);

    return localZ * (size_t)params->cache2d_sizeX + localX;
}

erosion_result_t df_cachelike_erosion(
        global const worldgen_params_t *restrict params,
        global const float4 *restrict data,
        int32_t x,
        int32_t z)
{
    if (!params)
        return (erosion_result_t){false, nan((uint64_t)0)};

    if (!cache_contains(params, x, z))
        return (erosion_result_t){false, nan((uint64_t)0)};
        
    float4 result = data[cache_index(params, x, z)];
    
    if(result.x == 0.0 && result.y == 0.0)
        return (erosion_result_t){false, nan((uint64_t)0)};

    return (erosion_result_t){
        true,
        result
    };
}

void df_write_erosion(
        global const worldgen_params_t *restrict params,
        float4 value,
        global float4 *restrict data,
        int32_t x,
        int32_t z)
{
    if (!params)
        return;

    if (!cache_contains(params, x, z))
        return;

    data[cache_index(params, x, z)] = value;
}

#define TAU 6.28318530717959f

static float2 hash_erosion(float2 x){
    float2 k = (float2)(0.3183099f, 0.3678794f);
    x = x * k + k.yx;
    float2 l;
    return -1.0f + 2.0f * fract(16.0f * k * fract(x.x * x.y * (x.x + x.y), &l), &l);
}
""" +
// Phacelle Noise function copyright (c) 2025 Rune Skovbo Johansen
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
            """
float4 PhacelleNoise(float2 p, float2 normDir, float freq, float offset, float normalization) {
    float2 sideDir = normDir.yx * (float2)(-1.0f, 1.0f) * freq * TAU;
    offset *= TAU;
    float2 pInt;
    float2 pFrac = fract(p, &pInt);
    float2 phaseDir = (float2)(0.0f);
    float weightSum = 0.0f;
    for (int i = -1; i <= 2; i++){
        for (int j = -1; j <= 2; j++){
            float2 gridOffset = (float2)(i, j);
            float2 gridPoint = pInt + gridOffset;
            float2 randomOffset = hash_erosion(gridPoint) * 0.5f;
            float2 vectorFromCellPoint = pFrac - gridOffset - randomOffset;
            float sqrDist = dot(vectorFromCellPoint, vectorFromCellPoint);
            float weight = native_exp(-sqrDist * 2.0f);
            weight = max(0.0f, weight - 0.01111f);
            weightSum += weight;
            float waveInput = dot(vectorFromCellPoint, sideDir) + offset;
            phaseDir += (float2)(native_cos(waveInput), native_sin(waveInput)) * weight;
        }
    }
    float2 interpolated = phaseDir/weightSum;
    float magnitude = native_sqrt(dot(interpolated, interpolated));
    magnitude = max(1.0f - normalization, magnitude);
    return (float4)(interpolated / magnitude, sideDir);
}

#define clamp01(x) clamp((x), 0.0f, 1.0f)

static float pow_inv(float t, float power) {
    t = clamp01(t);
    return 1.0f - pow(1.0f - t, power);
}

static float ease_out(float t) {
    float v = 1.0f - clamp01(t);
    return 1.0f - v * v;
}

static float smooth_start(float t, float smoothing) {
    if (t >= smoothing)
        return t - 0.5f * smoothing;
    return 0.5f * t * t / smoothing;
}

static float2 safe_normalize(float2 n) {
    float l = length(n);
    return n / (l + 1e-10f);
}
""" +
// Advanced Terrain Erosion Filter copyright (c) 2025 Rune Skovbo Johansen
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
            """
float4 ErosionFilter(
    float2 p, float3 heightAndSlope, float fadeTarget,
    float strength, float gullyWeight, float detail, float4 rounding, float4 onset, float2 assumedSlope,
    float scale, int octaves, float lacunarity,
    float gain, float cellScale, float normalization,
    float *ridgeMap
){
    fadeTarget = clamp(fadeTarget, -1.0f, 1.0f);
    float3 inputHeightAndSlope = heightAndSlope;
    float freq = 1.0f/(scale*cellScale);
    float slopeLength = max(length(heightAndSlope.yz), 1e-10f);
    float magnitude = 0.0f;
    float roundingMult = 1.0f;
    float roundingForInput = mix(rounding.y, rounding.x, clamp01(fadeTarget + 0.5f)) * rounding.z;
    float combiMask = ease_out(smooth_start(slopeLength * onset.x, roundingForInput * onset.x));
    float ridgeMapCombiMask = ease_out(slopeLength * onset.z);
    float ridgeMapFadeTarget = fadeTarget;
    float2 gullySlope = mix(heightAndSlope.yz, heightAndSlope.yz / slopeLength * assumedSlope.x, assumedSlope.y);
    for(int i = 0; i < octaves; i++){
        float4 phacelle = PhacelleNoise(p * freq, safe_normalize(gullySlope), cellScale, 0.25f, normalization);
        phacelle.zw *= -freq;
        float sloping = fabs(phacelle.y);
        gullySlope += sign(phacelle.y) * phacelle.zw * strength * gullyWeight;
        float3 gullies = (float3)(phacelle.x, phacelle.y * phacelle.zw);
        float3 fadedGullies = mix((float3)(fadeTarget, 0.0f, 0.0f), gullies * gullyWeight, combiMask);
        heightAndSlope += fadedGullies * strength;
        magnitude += strength;
        fadeTarget = fadedGullies.x;
        float roundingForOctave = mix(rounding.y, rounding.x, clamp01(phacelle.x + 0.5f)) * roundingMult;
        float newMask = ease_out(smooth_start(sloping * onset.y, roundingForOctave * onset.y));
        combiMask = pow_inv(combiMask, detail) * newMask;
        if(i<2){
            ridgeMapFadeTarget = mix(ridgeMapFadeTarget, gullies.x, ridgeMapCombiMask);
            float newRidgeMapMask = ease_out(sloping * onset.w);
            ridgeMapCombiMask = ridgeMapCombiMask * newRidgeMapMask;
        }
        strength *= gain;
        freq *= lacunarity;
        roundingMult *= rounding.w;
    }
    *ridgeMap = ridgeMapFadeTarget * (1.0f-ridgeMapCombiMask);
    float3 heightAndSlopeDelta = heightAndSlope - inputHeightAndSlope;
    return (float4)(heightAndSlopeDelta, magnitude);
}
""";
}
