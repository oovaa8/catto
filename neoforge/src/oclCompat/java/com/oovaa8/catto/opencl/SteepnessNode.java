package com.oovaa8.catto.opencl;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.AstTransformer;
import com.ishland.c2me.opts.dfc.common.ast.misc.DelegateNode;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.Objects;

public class SteepnessNode extends DelegateNode {

    private final DensityFunction df;
    public AstNode height;
    public AstNode steepness;
    public AstNode flatness;
    public AstNode length;
    public AstNode noise;
    public int worldHeight;
    public float ocean;

    public SteepnessNode(DensityFunction df, AstNode height, AstNode steepness, AstNode flatness, AstNode length, AstNode noise, int worldHeight, float ocean) {
        super(df);
        this.df = df;
        this.height = height;
        this.steepness = steepness;
        this.flatness = flatness;
        this.length = length;
        this.noise = noise;
        this.worldHeight = worldHeight;
        this.ocean = ocean;
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[]{this.height, this.steepness, this.flatness, this.length, this.noise};
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        AstNode height = this.height.transform(transformer);
        AstNode steepness = this.steepness.transform(transformer);
        AstNode flatness = this.flatness.transform(transformer);
        AstNode length = this.length.transform(transformer);
        AstNode noise = this.noise.transform(transformer);
        if (this.height == height && this.steepness == steepness && this.flatness == flatness && this.length == length && this.noise == noise) {
            return transformer.transform(this);
        } else {
            return transformer.transform(new SteepnessNode(this.df, height, steepness, flatness, length, noise, this.worldHeight, this.ocean));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SteepnessNode that = (SteepnessNode) o;
        return worldHeight == that.worldHeight && Float.compare(ocean, that.ocean) == 0
                && Objects.equals(height, that.height) && Objects.equals(steepness, that.steepness) && Objects.equals(flatness, that.flatness)
                && Objects.equals(length, that.length) && Objects.equals(noise, that.noise);
    }


    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + this.height.hashCode();
        result = 31 * result + this.steepness.hashCode();
        result = 31 * result + this.flatness.hashCode();
        result = 31 * result + this.length.hashCode();
        result = 31 * result + this.noise.hashCode();
        result = 31 * result + Integer.hashCode(this.worldHeight);
        result = 31 * result + Float.hashCode(this.ocean);

        return result;
    }

    @Override
    public boolean relaxedEquals(AstNode o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SteepnessNode that = (SteepnessNode) o;
        return worldHeight == that.worldHeight && Float.compare(ocean, that.ocean) == 0
                && height.relaxedEquals(that.height) && steepness.relaxedEquals(that.steepness) && flatness.relaxedEquals(that.flatness)
                && length.relaxedEquals(that.length) && noise.relaxedEquals(that.noise);
    }

    @Override
    public int relaxedHashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + this.height.relaxedHashCode();
        result = 31 * result + this.steepness.relaxedHashCode();
        result = 31 * result + this.flatness.relaxedHashCode();
        result = 31 * result + this.length.relaxedHashCode();
        result = 31 * result + this.noise.relaxedHashCode();
        result = 31 * result + Integer.hashCode(this.worldHeight);
        result = 31 * result + Float.hashCode(this.ocean);

        return result;
    }
}
