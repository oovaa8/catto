package com.oovaa8.catto.density_functions;

// Advanced Terrain Erosion Filter copyright (c) 2025 Rune Skovbo Johansen
// Phacelle Noise function copyright (c) 2025 Rune Skovbo Johansen
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

// OpenCL translation of Rune's Erosion Filter, you can find the original here: https://www.shadertoy.com/view/sf23W1
public class ErosionFilterOCL {
    public static final String EROSION = """
// Phacelle Noise function copyright (c) 2025 Rune Skovbo Johansen
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
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

// Advanced Terrain Erosion Filter copyright (c) 2025 Rune Skovbo Johansen
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
