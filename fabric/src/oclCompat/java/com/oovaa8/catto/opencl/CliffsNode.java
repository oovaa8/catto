package com.oovaa8.catto.opencl;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.AstTransformer;
import com.ishland.c2me.opts.dfc.common.ast.misc.DelegateNode;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.Objects;

public class CliffsNode extends DelegateNode {
    private final DensityFunction df;
    public AstNode height;
    public AstNode length;
    public int worldHeight;
    public double ocean;

    public CliffsNode(DensityFunction df, AstNode height, AstNode length, int worldHeight, double ocean) {
        super(df);
        this.df = df;
        this.height = height;
        this.length = length;
        this.worldHeight = worldHeight;
        this.ocean = ocean;
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[]{this.height, this.length};
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        AstNode height = this.height.transform(transformer);
        AstNode length = this.length.transform(transformer);
        if (this.height == height && this.length == length) {
            return transformer.transform(this);
        } else {
            return transformer.transform(new CliffsNode(df, height, length, this.worldHeight, this.ocean));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CliffsNode that = (CliffsNode) o;
        return worldHeight == that.worldHeight && Double.compare(ocean, that.ocean) == 0
                && Objects.equals(height, that.height) && Objects.equals(length, that.length);
    }


    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + this.height.hashCode();
        result = 31 * result + this.length.hashCode();
        result = 31 * result + Integer.hashCode(this.worldHeight);
        result = 31 * result + Double.hashCode(this.ocean);

        return result;
    }

    @Override
    public boolean relaxedEquals(AstNode o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CliffsNode that = (CliffsNode) o;
        return worldHeight == that.worldHeight && Double.compare(ocean, that.ocean) == 0
                && height.relaxedEquals(that.height) && length.relaxedEquals(that.length);
    }

    @Override
    public int relaxedHashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + this.height.relaxedHashCode();
        result = 31 * result + this.length.relaxedHashCode();
        result = 31 * result + Integer.hashCode(this.worldHeight);
        result = 31 * result + Double.hashCode(this.ocean);

        return result;
    }
}
