package com.oovaa8.catto.opencl;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.AstTransformer;
import com.ishland.c2me.opts.dfc.common.ast.misc.DelegateNode;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.Objects;

public class CliffsInvertedNode extends DelegateNode {
    private final DensityFunction df;
    public AstNode height;
    public AstNode cliffs;
    public AstNode raw_height;
    public AstNode length;
    public AstNode flatness;
    public AstNode steepness;
    public AstNode noise;
    public int worldHeight;
    public double ocean;
    public int min_y;

    public CliffsInvertedNode(DensityFunction df, AstNode height, AstNode cliffs, AstNode raw_height, AstNode length, AstNode flatness, AstNode steepness, AstNode noise, int worldHeight, double ocean, int min_y) {
        super(df);
        this.df = df;
        this.height = height;
        this.cliffs = cliffs;
        this.raw_height = raw_height;
        this.length = length;
        this.flatness = flatness;
        this.steepness = steepness;
        this.noise = noise;
        this.worldHeight = worldHeight;
        this.ocean = ocean;
        this.min_y = min_y;
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[]{this.height, this.cliffs, this.raw_height, this.length, this.flatness, this.steepness, this.noise};
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        AstNode height = this.height.transform(transformer);
        AstNode cliffs = this.cliffs.transform(transformer);
        AstNode raw_height = this.raw_height.transform(transformer);
        AstNode length = this.length.transform(transformer);
        AstNode flatness = this.flatness.transform(transformer);
        AstNode steepness = this.steepness.transform(transformer);
        AstNode noise = this.noise.transform(transformer);
        if (this.height == height && this.length == length) {
            return transformer.transform(this);
        } else {
            return transformer.transform(new CliffsInvertedNode(df, height, cliffs, raw_height, length, flatness, steepness, noise, this.worldHeight, this.ocean, min_y));
        }
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CliffsInvertedNode that = (CliffsInvertedNode) o;
        return worldHeight == that.worldHeight && Double.compare(ocean, that.ocean) == 0 && min_y == that.min_y && Objects.equals(height, that.height) && Objects.equals(cliffs, that.cliffs) && Objects.equals(raw_height, that.raw_height) && Objects.equals(length, that.length) && Objects.equals(flatness, that.flatness) && Objects.equals(steepness, that.steepness) && Objects.equals(noise, that.noise);
    }

    /*
    AstNode height;
    AstNode cliffs;
    AstNode raw_height;
    AstNode length;
    AstNode flatness;
    AstNode steepness;
    AstNode noise;
    int worldHeight;
    double ocean;
    int min_y;*/

    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + this.height.hashCode();
        result = 31 * result + this.cliffs.hashCode();
        result = 31 * result + this.raw_height.hashCode();
        result = 31 * result + this.length.hashCode();
        result = 31 * result + this.flatness.hashCode();
        result = 31 * result + this.steepness.hashCode();
        result = 31 * result + this.noise.hashCode();
        result = 31 * result + Integer.hashCode(this.worldHeight);
        result = 31 * result + Double.hashCode(this.ocean);
        result = 31 * result + Double.hashCode(this.min_y);

        return result;
    }

    @Override
    public boolean relaxedEquals(AstNode o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CliffsInvertedNode that = (CliffsInvertedNode) o;
        return worldHeight == that.worldHeight && Double.compare(ocean, that.ocean) == 0 && min_y == that.min_y && height.relaxedEquals(that.height) && cliffs.relaxedEquals(that.cliffs) && raw_height.relaxedEquals(that.raw_height) && length.relaxedEquals(that.length) && flatness.relaxedEquals(that.flatness) && steepness.relaxedEquals(that.steepness) && noise.relaxedEquals(that.noise);
    }

    @Override
    public int relaxedHashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + this.height.relaxedHashCode();
        result = 31 * result + this.cliffs.relaxedHashCode();
        result = 31 * result + this.raw_height.relaxedHashCode();
        result = 31 * result + this.length.relaxedHashCode();
        result = 31 * result + this.flatness.relaxedHashCode();
        result = 31 * result + this.steepness.relaxedHashCode();
        result = 31 * result + this.noise.relaxedHashCode();
        result = 31 * result + Integer.hashCode(this.worldHeight);
        result = 31 * result + Double.hashCode(this.ocean);
        result = 31 * result + Double.hashCode(this.min_y);

        return result;
    }
}
