package com.oovaa8.catto.opencl.doubleprecission;

import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import com.oovaa8.catto.opencl.ErosionNode;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import static com.oovaa8.catto.opencl.ErosionEmitter.MARKER_cacheLike_erosion;

//So we have come full circle
public class ErosionEmitterDouble implements OpenCLCEmitter<ErosionNode>{
    public static final ErosionEmitterDouble INSTANCE = new ErosionEmitterDouble();

    private final Set<OpenCLCGenContext> helpersEmitted =
            Collections.newSetFromMap(new WeakHashMap<>());

    private ErosionEmitterDouble() {
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

        if(ErosionEmitterDouble.INSTANCE.helpersEmitted.add(context)){
            context.appendRaw(HELPER_FUNCTIONS);
            emitBase(context, hCall, strengthCall, scaleCall, ridgeRoundingCall, creaseRoundingCall, gainCall, node);
            context.allocGlobalDynamicData(MARKER_cacheLike_erosion);
        }

        int offset = context.getGlobalDynamicDataOffset(MARKER_cacheLike_erosion);

        return "if (ctx.rw_data){\n" +
                "    global const worldgen_params_t * restrict params = ctx.rw_data;\n" +
                "    global const double4 * restrict data = df_data_offset_global(ctx.rw_data, " + offset + ");\n" +
                "    const erosion_result_t res = df_cachelike_erosion(params, data, ctx.x, ctx.z);\n" +
                "    if (res.cached) {\n" +
                "       return " + switch (node.mode) {
                        case "ridge"   -> "res.res.y;\n";
                        case "slope_x" -> "res.res.z;\n";
                        case "slope_z" -> "res.res.w;\n";
                        default        -> "res.res.x;\n";} +
                "    }\n" +
                "}" +
                "double4 base = erosion_base(ctx);\n" +
                "if (ctx.rw_data) {\n" +
                "    global const worldgen_params_t * restrict params = ctx.rw_data;\n" +
                "    global double4 * restrict data = df_data_offset_global(ctx.rw_data, " + offset + ");\n" +
                "    df_write_erosion(params, base, data, ctx.x, ctx.z);\n" +
                "}\n" +
                "return " + switch (node.mode) {
                    case "ridge"   -> "base.y;\n";
                    case "slope_x" -> "base.z;\n";
                    case "slope_z" -> "base.w;\n";
                    default        -> "base.x;\n";
        };/*
        return  "double4 base = erosion_base(ctx);\n" +
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
                    "static __attribute__((pure)) double4 erosion_base(sample_int32_ctx_t ctx) {\n" +
                            "double height = " + hCall + ";\n" +
                            "sample_int32_ctx_t ctx_dx = make_sample_int32_ctx(ctx.const_data, ctx.rw_data, ctx.x + 1, ctx.y, ctx.z, ctx.sample_flags);\n" +
                            "double hdx = " + hCallDx + ";\n" +
                            "sample_int32_ctx_t ctx_dz = make_sample_int32_ctx(ctx.const_data, ctx.rw_data, ctx.x, ctx.y, ctx.z + 1, ctx.sample_flags);\n" +
                            "double hdz = " + hCallDz + ";\n" +
                            "double2 slope = (double2)(hdx - height, hdz - height);\n" +
                            "double3 heightAndSlope = (double3)(height, slope * " + Double.toHexString(node.slopeScale) + "f);\n" +
                            "double fadeTarget = clamp(height / 0.6, -1.0, 1.0);\n" +
                            "double ridgeMap = 0.0;\n" +
                            "double4 erosion = ErosionFilter(\n" +
                            "(double2)(ctx.x, ctx.z), heightAndSlope, fadeTarget,\n" +
                            strengthCall + ", 0.5, " + Double.toHexString(node.detail) + ",\n" +
                            "(double4)(" + ridgeRoundingCall + ", " + creaseRoundingCall + ", " +
                            Double.toHexString(node.rounding_z) + ", " + Double.toHexString(node.rounding_w) + "),\n" +
                            "(double4)(1.25, 1.25, 2.8, 1.5),\n" +
                            "(double2)(" + Double.toHexString(node.assumedSlope) + ", " +
                            Double.toHexString(node.assumedSlope_weight) + "),\n" +
                            scaleCall + ", " + node.octaves + ", " +
                            Double.toHexString(node.lacunarity) + ", " + gainCall + ",\n" +
                            "0.8, " + Double.toHexString(node.normalization) + ", &ridgeMap);\n" +
                            "return (double4)(height + erosion.x + erosion.w * 0.75, ridgeMap, slope.x + erosion.y, slope.y + erosion.z);\n" +
                    "}\n"
        );
    }


    private static String HELPER_FUNCTIONS = """
typedef struct erosion_result {
    bool cached;
    double4 res;
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
        global const double4 *restrict data,
        int32_t x,
        int32_t z)
{
    if (!params)
        return (erosion_result_t){false, nan((uint64_t)0)};

    if (!cache_contains(params, x, z))
        return (erosion_result_t){false, nan((uint64_t)0)};
        
    double4 result = data[cache_index(params, x, z)];
    
    if(result.x == 0.0 && result.y == 0.0)
        return (erosion_result_t){false, nan((uint64_t)0)};

    return (erosion_result_t){
        true,
        result
    };
}

void df_write_erosion(
        global const worldgen_params_t *restrict params,
        double4 value,
        global double4 *restrict data,
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

static double2 hash_erosion(double2 x){
    double2 k = (double2)(0.3183099, 0.3678794);
    x = x * k + k.yx;
    double2 l;
    return -1.0 + 2.0 * fract(16.0 * k * fract(x.x * x.y * (x.x + x.y), &l), &l);
}
""" +
// Phacelle Noise function copyright (c) 2025 Rune Skovbo Johansen
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
            """
double4 PhacelleNoise(double2 p, double2 normDir, double freq, double offset, double normalization) {
    double2 sideDir = normDir.yx * (double2)(-1.0, 1.0) * freq * TAU;
    offset *= TAU;
    double2 pInt;
    double2 pFrac = fract(p, &pInt);
    double2 phaseDir = (double2)(0.0);
    double weightSum = 0.0;
    for (int i = -1; i <= 2; i++){
        for (int j = -1; j <= 2; j++){
            double2 gridOffset = (double2)(i, j);
            double2 gridPoint = pInt + gridOffset;
            double2 randomOffset = hash_erosion(gridPoint) * 0.5;
            double2 vectorFromCellPoint = pFrac - gridOffset - randomOffset;
            double sqrDist = dot(vectorFromCellPoint, vectorFromCellPoint);
            double weight = native_exp(-sqrDist * 2.0);
            weight = max(0.0, weight - 0.01111);
            weightSum += weight;
            double waveInput = dot(vectorFromCellPoint, sideDir) + offset;
            phaseDir += (double2)(native_cos(waveInput), native_sin(waveInput)) * weight;
        }
    }
    double2 interpolated = phaseDir/weightSum;
    double magnitude = native_sqrt(dot(interpolated, interpolated));
    magnitude = max(1.0 - normalization, magnitude);
    return (double4)(interpolated / magnitude, sideDir);
}

#define clamp01(x) clamp((x), 0.0, 1.0)

static double pow_inv(double t, double power) {
    t = clamp01(t);
    return 1.0 - pow(1.0 - t, power);
}

static double ease_out(double t) {
    double v = 1.0 - clamp01(t);
    return 1.0 - v * v;
}

static double smooth_start(double t, double smoothing) {
    if (t >= smoothing)
        return t - 0.5 * smoothing;
    return 0.5 * t * t / smoothing;
}

static double2 safe_normalize(double2 n) {
    double l = length(n);
    return n / (l + 1e-10);
}
""" +
// Advanced Terrain Erosion Filter copyright (c) 2025 Rune Skovbo Johansen
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
            """
double4 ErosionFilter(
    double2 p, double3 heightAndSlope, double fadeTarget,
    double strength, double gullyWeight, double detail, double4 rounding, double4 onset, double2 assumedSlope,
    double scale, int octaves, double lacunarity,
    double gain, double cellScale, double normalization,
    double *ridgeMap
){
    fadeTarget = clamp(fadeTarget, -1.0, 1.0);
    double3 inputHeightAndSlope = heightAndSlope;
    double freq = 1.0/(scale*cellScale);
    double slopeLength = max(length(heightAndSlope.yz), 1e-10);
    double magnitude = 0.0;
    double roundingMult = 1.0;
    double roundingForInput = mix(rounding.y, rounding.x, clamp01(fadeTarget + 0.5)) * rounding.z;
    double combiMask = ease_out(smooth_start(slopeLength * onset.x, roundingForInput * onset.x));
    double ridgeMapCombiMask = ease_out(slopeLength * onset.z);
    double ridgeMapFadeTarget = fadeTarget;
    double2 gullySlope = mix(heightAndSlope.yz, heightAndSlope.yz / slopeLength * assumedSlope.x, assumedSlope.y);
    for(int i = 0; i < octaves; i++){
        double4 phacelle = PhacelleNoise(p * freq, safe_normalize(gullySlope), cellScale, 0.25, normalization);
        phacelle.zw *= -freq;
        double sloping = fabs(phacelle.y);
        gullySlope += sign(phacelle.y) * phacelle.zw * strength * gullyWeight;
        double3 gullies = (double3)(phacelle.x, phacelle.y * phacelle.zw);
        double3 fadedGullies = mix((double3)(fadeTarget, 0.0, 0.0), gullies * gullyWeight, combiMask);
        heightAndSlope += fadedGullies * strength;
        magnitude += strength;
        fadeTarget = fadedGullies.x;
        double roundingForOctave = mix(rounding.y, rounding.x, clamp01(phacelle.x + 0.5)) * roundingMult;
        double newMask = ease_out(smooth_start(sloping * onset.y, roundingForOctave * onset.y));
        combiMask = pow_inv(combiMask, detail) * newMask;
        if(i<2){
            ridgeMapFadeTarget = mix(ridgeMapFadeTarget, gullies.x, ridgeMapCombiMask);
            double newRidgeMapMask = ease_out(sloping * onset.w);
            ridgeMapCombiMask = ridgeMapCombiMask * newRidgeMapMask;
        }
        strength *= gain;
        freq *= lacunarity;
        roundingMult *= rounding.w;
    }
    *ridgeMap = ridgeMapFadeTarget * (1.0-ridgeMapCombiMask);
    double3 heightAndSlopeDelta = heightAndSlope - inputHeightAndSlope;
    return (double4)(heightAndSlopeDelta, magnitude);
}
""";
}
