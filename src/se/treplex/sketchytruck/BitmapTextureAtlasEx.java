package se.treplex.sketchytruck;


import android.content.Context;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

public class BitmapTextureAtlasEx extends BitmapTextureAtlas {
    int mUsedWidth;

    public BitmapTextureAtlasEx(int width, int height, TextureOptions options) {
        super(width, height, options);

        mUsedWidth = 0;
    }

    public TextureRegion appendTextureAsset(Context c, String assetPath) {
        TextureRegion region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                this, c, assetPath, mUsedWidth, 0);
        mUsedWidth += region.getWidth();

        return region;
    }

    public TiledTextureRegion appendTiledAsset(Context c, String assetPath, int tiledColumns, int tiledRows) {
        TiledTextureRegion region = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                this, c, assetPath, mUsedWidth, 0, tiledColumns, tiledRows);
        mUsedWidth += region.getWidth();

        return region;
    }
}
