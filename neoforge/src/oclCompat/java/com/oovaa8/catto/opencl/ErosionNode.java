package com.oovaa8.catto.opencl;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.AstTransformer;
import com.ishland.c2me.opts.dfc.common.ast.misc.DelegateNode;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.Objects;

public class ErosionNode extends DelegateNode {
    private final DensityFunction df;
    public AstNode height;
    public AstNode strength;
    public AstNode scale;
    public double detail;
    public AstNode ridgeRounding;
    public AstNode creaseRounding;
    public double rounding_z;
    public double rounding_w;
    public double assumedSlope;
    public double assumedSlope_weight;
    public double normalization;
    public int octaves;
    public double lacunarity;
    public AstNode gain;
    public double slopeScale;
    public String mode;

    public ErosionNode(DensityFunction df, AstNode height, AstNode strength, AstNode scale, double detail, AstNode ridgeRounding, AstNode creaseRounding, double rounding_z, double rounding_w, double assumedSlope, double assumedSlope_weight, double normalization, int octaves, double lacunarity, AstNode gain, double slopeScale, String mode) {
        super(df);
        this.df = df;
        this.height = Objects.requireNonNull(height);
        this.strength = Objects.requireNonNull(strength);
        this.scale = Objects.requireNonNull(scale);
        this.detail = detail;
        this.ridgeRounding = Objects.requireNonNull(ridgeRounding);
        this.creaseRounding = Objects.requireNonNull(creaseRounding);
        this.rounding_z = rounding_z;
        this.rounding_w = rounding_w;
        this.assumedSlope = assumedSlope;
        this.assumedSlope_weight = assumedSlope_weight;
        this.normalization = normalization;
        this.octaves = octaves;
        this.lacunarity = lacunarity;
        this.gain = Objects.requireNonNull(gain);
        this.slopeScale = slopeScale;
        this.mode = mode;
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[]{this.height, this.strength, this.scale, this.ridgeRounding, this.creaseRounding, this.gain};
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        AstNode height = this.height.transform(transformer);
        AstNode strength = this.strength.transform(transformer);
        AstNode scale = this.scale.transform(transformer);
        AstNode ridgeRounding = this.ridgeRounding.transform(transformer);
        AstNode creaseRounding = this.creaseRounding.transform(transformer);
        AstNode gain = this.gain.transform(transformer);
        if (this.height == height && this.strength == strength && this.scale == scale && this.ridgeRounding == ridgeRounding && this.creaseRounding == creaseRounding && this.gain == gain) {
            return transformer.transform(this);
        } else {
            return transformer.transform(new ErosionNode(df, height, strength, scale, detail, ridgeRounding, creaseRounding, rounding_z, rounding_w, assumedSlope, assumedSlope_weight, normalization, octaves, lacunarity, gain, slopeScale, mode));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErosionNode that = (ErosionNode) o;
        return Double.compare(detail, that.detail) == 0 && Double.compare(rounding_z, that.rounding_z) == 0
                && Double.compare(rounding_w, that.rounding_w) == 0 && Double.compare(assumedSlope, that.assumedSlope) == 0
                && Double.compare(assumedSlope_weight, that.assumedSlope_weight) == 0 && Double.compare(normalization, that.normalization) == 0
                && octaves == that.octaves && Double.compare(lacunarity, that.lacunarity) == 0
                && Double.compare(slopeScale, that.slopeScale) == 0 && Objects.equals(mode, that.mode)
                && Objects.equals(height, that.height) && Objects.equals(strength, that.strength) && Objects.equals(scale, that.scale)
                && Objects.equals(ridgeRounding, that.ridgeRounding) && Objects.equals(creaseRounding, that.creaseRounding) && Objects.equals(gain, that.gain);
    }


    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + this.height.hashCode();
        result = 31 * result + this.strength.hashCode();
        result = 31 * result + this.scale.hashCode();
        result = 31 * result + Double.hashCode(this.detail);
        result = 31 * result + this.ridgeRounding.hashCode();
        result = 31 * result + this.creaseRounding.hashCode();
        result = 31 * result + Double.hashCode(this.rounding_z);
        result = 31 * result + Double.hashCode(this.rounding_w);
        result = 31 * result + Double.hashCode(this.assumedSlope);
        result = 31 * result + Double.hashCode(this.assumedSlope_weight);
        result = 31 * result + Double.hashCode(this.normalization);
        result = 31 * result + Integer.hashCode(this.octaves);
        result = 31 * result + Double.hashCode(this.lacunarity);
        result = 31 * result + this.gain.hashCode();
        result = 31 * result + Double.hashCode(this.slopeScale);
        result = 31 * result + this.mode.hashCode();

        return result;
    }

    @Override
    public boolean relaxedEquals(AstNode o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErosionNode that = (ErosionNode) o;
        return Double.compare(detail, that.detail) == 0 && Double.compare(rounding_z, that.rounding_z) == 0
                && Double.compare(rounding_w, that.rounding_w) == 0 && Double.compare(assumedSlope, that.assumedSlope) == 0
                && Double.compare(assumedSlope_weight, that.assumedSlope_weight) == 0 && Double.compare(normalization, that.normalization) == 0
                && octaves == that.octaves && Double.compare(lacunarity, that.lacunarity) == 0
                && Double.compare(slopeScale, that.slopeScale) == 0 && Objects.equals(mode, that.mode)
                && height.relaxedEquals(that.height) && strength.relaxedEquals(that.strength) && scale.relaxedEquals(that.scale)
                && ridgeRounding.relaxedEquals(that.ridgeRounding) && creaseRounding.relaxedEquals(that.creaseRounding) && gain.relaxedEquals(that.gain);
    }

    @Override
    public int relaxedHashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + this.height.relaxedHashCode();
        result = 31 * result + this.strength.relaxedHashCode();
        result = 31 * result + this.scale.relaxedHashCode();
        result = 31 * result + Double.hashCode(this.detail);
        result = 31 * result + this.ridgeRounding.relaxedHashCode();
        result = 31 * result + this.creaseRounding.relaxedHashCode();
        result = 31 * result + Double.hashCode(this.rounding_z);
        result = 31 * result + Double.hashCode(this.rounding_w);
        result = 31 * result + Double.hashCode(this.assumedSlope);
        result = 31 * result + Double.hashCode(this.assumedSlope_weight);
        result = 31 * result + Double.hashCode(this.normalization);
        result = 31 * result + Integer.hashCode(this.octaves);
        result = 31 * result + Double.hashCode(this.lacunarity);
        result = 31 * result + this.gain.relaxedHashCode();
        result = 31 * result + Double.hashCode(this.slopeScale);
        result = 31 * result + this.mode.hashCode();

        return result;
    }
}
