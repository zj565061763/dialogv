package com.sd.lib.dialog.animator;

/**
 * 缩放xy
 */
public class ScaleXYCreator extends CombineCreator {
    public ScaleXYCreator() {
        super(new ScaleXCreator(), new ScaleYCreator());
    }
}
