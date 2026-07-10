package com.oovaa8.catto.density_functions;

// Advanced Terrain Erosion Filter copyright (c) 2025 Rune Skovbo Johansen
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
public class ErosionFilter {
    // Java translation of Rune's Erosion Filter, you can find the original here: https://www.shadertoy.com/view/sf23W1
    static float[] ErosionFilter(
            int p_x, int p_y, float height, float slope_x, float slope_y, float fadeTarget,
            float scl, float stren, float ridge_rounding, float crease_rounding, float g,
            float cellScale, float onset_x, float onset_y, float onset_z, float onset_w, float gullyWeight,
            float assumedSlope, float assumedSlope_weight, float detail, float lacunarity, float rounding_z, float rounding_w, int octaves, float normalization
    ){
        float strengthLocal = stren;
        fadeTarget = Math.clamp(fadeTarget, -1,1);
        float inputHeight = height;
        float inputSlope_x = slope_x;
        float inputSlope_y = slope_y;

        float freq = 1.0f / (scl * cellScale);
        float slopeLength = Math.max(Erosion.length(slope_x, slope_y), 1e-10f);
        float magnitude = 0.0f;
        float roundingMult = 1.0f;

        float roundingForInput = Erosion.mix(crease_rounding, ridge_rounding, Math.clamp(fadeTarget + 0.5f, 0f, 1f)) * rounding_z;
        float combiMask = Erosion.ease_out(Erosion.smooth_start(slopeLength * onset_x, roundingForInput * onset_x));

        float ridgeMapCombiMask = Erosion.ease_out(slopeLength * onset_z);
        float ridgeMapFadeTarget = fadeTarget;

        float gullySlope_x = Erosion.mix(slope_x, slope_x / slopeLength * assumedSlope, assumedSlope_weight);
        float gullySlope_y = Erosion.mix(slope_y, slope_y / slopeLength * assumedSlope, assumedSlope_weight);

        for (int i = 0; i < octaves; i++) {
            float[] phacelle = PhacelleNoise(p_x * freq, p_y * freq, Erosion.safe_normalize_x(gullySlope_x, gullySlope_y), Erosion.safe_normalize_y(gullySlope_x, gullySlope_y), cellScale, 0.25f, normalization);
            float phacelle_x = phacelle[0];
            float phacelle_y = phacelle[1];
            float phacelle_z = -phacelle[2] * freq;
            float phacelle_w = -phacelle[3] * freq;

            float sloping = Math.abs(phacelle_y);

            gullySlope_x += phacelle_z * Math.signum(phacelle_y) * strengthLocal * gullyWeight;
            gullySlope_y += phacelle_w * Math.signum(phacelle_y) * strengthLocal * gullyWeight;

            float gullies_x = phacelle_x;
            float gullies_y = phacelle_y * phacelle_z;
            float gullies_z = phacelle_y * phacelle_w;


            float fadedGullies_x = Erosion.mix(fadeTarget, gullies_x * gullyWeight, combiMask);
            float fadedGullies_y = Erosion.mix(0, gullies_y * gullyWeight, combiMask);
            float fadedGullies_z = Erosion.mix(0, gullies_z * gullyWeight, combiMask);

            height += fadedGullies_x * strengthLocal;
            slope_x += fadedGullies_y * strengthLocal;
            slope_y += fadedGullies_z * strengthLocal;
            magnitude += strengthLocal;

            fadeTarget = fadedGullies_x;

            float roundingForOctaves = Erosion.mix(crease_rounding, ridge_rounding, Math.clamp(phacelle_x + 0.5f,0f,1f)) * roundingMult;
            float newMask = Erosion.ease_out(Erosion.smooth_start(sloping * onset_y, roundingForOctaves * onset_y));
            combiMask = Erosion.pow_inv(combiMask, detail) * newMask;

            if (i < 2){
                ridgeMapFadeTarget = Erosion.mix(ridgeMapFadeTarget, gullies_x, ridgeMapCombiMask);
                float newRidgeMapMask = Erosion.ease_out(sloping * onset_w);
                ridgeMapCombiMask *= newRidgeMapMask;
            }

            strengthLocal *= g;
            freq *= lacunarity;
            roundingMult *= rounding_w;
        }

        float ridgeMap = ridgeMapFadeTarget * (1-ridgeMapCombiMask);

        float heightDelta = height - inputHeight;
        float slopeDelta_x = slope_x - inputSlope_x;
        float slopeDelta_y = slope_y - inputSlope_y;

        return new float[]{heightDelta, slopeDelta_x, slopeDelta_y, magnitude, ridgeMap};
    }

    // Phacelle Noise function copyright (c) 2025 Rune Skovbo Johansen
    // This Source Code Form is subject to the terms of the Mozilla Public
    // License, v. 2.0. If a copy of the MPL was not distributed with this
    // file, You can obtain one at https://mozilla.org/MPL/2.0/.
    static float[] PhacelleNoise(float p_x, float p_y, float normDir_x, float normDir_y, float freq, float offset, float normalization){
        float sideDir_x = -normDir_y * freq * (float)Math.PI * 2.0f;
        float sideDir_y = normDir_x * freq * (float)Math.PI * 2.0f;

        float newOffset = offset * (float)Math.PI * 2f;

        int pInt_x = (int) Math.floor(p_x);
        int pInt_y = (int) Math.floor(p_y);
        float pFrac_x = p_x - pInt_x;
        float pFrac_y = p_y - pInt_y;
        float phaseDir_x = 0;
        float phaseDir_y = 0;
        float weightSum = 0;

        for (int i = -1; i <= 2; i++) {
            for (int j = -1; j <= 2; j++) {
                int gridPoint_x = pInt_x + i;
                int gridPoint_y = pInt_y + j;
                float randomOffset_x = Erosion.hash_x(gridPoint_x, gridPoint_y) * 0.5f;
                float randomOffset_y = Erosion.hash_y(gridPoint_x, gridPoint_y) * 0.5f;

                float vectorFromCellPoint_x = pFrac_x - i - randomOffset_x;
                float vectorFromCellPoint_y = pFrac_y - j - randomOffset_y;

                float sqrDist = Erosion.dot_self(vectorFromCellPoint_x, vectorFromCellPoint_y);
                float weight = (float)Math.exp(-sqrDist * 2.0D);

                weight = (float)Math.max(0.0, weight - 0.01111D);
                weightSum += weight;

                float waveInput = Erosion.dot(vectorFromCellPoint_x, vectorFromCellPoint_y, sideDir_x, sideDir_y) + newOffset;

                phaseDir_x += Math.cos(waveInput) * weight;
                phaseDir_y += Math.sin(waveInput) * weight;
            }
        }

        float interpolated_x = phaseDir_x / (float)Math.max(weightSum, 1e-10);
        float interpolated_y = phaseDir_y / (float)Math.max(weightSum, 1e-10);
        float magnitude = (float)Math.sqrt(Erosion.dot_self(interpolated_x, interpolated_y));
        magnitude = (float)Math.max(1.0D - normalization, magnitude);

        return new float[]{interpolated_x / magnitude, interpolated_y / magnitude, sideDir_x, sideDir_y};
    }
}
