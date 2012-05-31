package org.anddev.andengine.entity.modifier;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.CubicBezierMoveModifier;
import org.anddev.andengine.util.MathUtils;
import org.anddev.andengine.util.modifier.ease.IEaseFunction;

/**
 * <p>A {@link CubicBezierMoveModifier} that rotates the entity so it faces the direction of travel.</p>
 * 
 * <p>(c) 2010 Nicolas Gramlich<br>
 * (c) 2011 Zynga Inc.</p>
 * 
 * @author Scott Kennedy
 * @author Pawel Plewa
 * @author Nicolas Gramlich
 * @since 12:54:00 - 17.08.2011
 */
public class RotateCubicBezierMoveModifier extends CubicBezierMoveModifier {
    public RotateCubicBezierMoveModifier(final float pDuration, final float pX1, final float pY1, final float pX2, final float pY2, final float pX3,
            final float pY3, final float pX4, final float pY4, final IEaseFunction pEaseFunction) {
        super(pDuration, pX1, pY1, pX2, pY2, pX3, pY3, pX4, pY4, pEaseFunction);
    }

    @Override
    protected void onManagedUpdate(final float pSecondsElapsed, final IEntity pEntity) {
        final float startX = pEntity.getX();
        final float startY = pEntity.getY();
        
        super.onManagedUpdate(pSecondsElapsed, pEntity);
        
        final float deltaX = pEntity.getX() - startX;
        final float deltaY = pEntity.getY() - startY;
        
        pEntity.setRotation(MathUtils.radToDeg(MathUtils.atan2(deltaY, deltaX)) + 90);
    }
}
