package com.sd.lib.dialog.impl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.sd.lib.dialog.IDialog;
import com.sd.lib.dialog.ITargetDialog;
import com.sd.lib.dialog.R;
import com.sd.lib.dialog.animator.AlphaCreator;
import com.sd.lib.dialog.animator.AnimatorCreator;
import com.sd.lib.dialog.animator.ObjectAnimatorCreator;
import com.sd.lib.dialog.animator.SlideBottomTopParentCreator;
import com.sd.lib.dialog.animator.SlideLeftRightParentCreator;
import com.sd.lib.dialog.animator.SlideRightLeftParentCreator;
import com.sd.lib.dialog.animator.SlideTopBottomParentCreator;

import java.util.List;

public class FDialog implements IDialog
{
    private final Activity mActivity;

    private final InternalDialogView _dialogView;
    private final LinearLayout _containerView;
    private final View _backgroundView;
    private View mContentView;

    private State _state = State.Dismissed;

    private int mGravity = Gravity.NO_GRAVITY;
    private boolean mCanceledOnTouchOutside = true;
    private boolean mIsBackgroundDim;

    private FVisibilityAnimatorHandler mAnimatorHandler;
    private AnimatorCreator mAnimatorCreator;
    private AnimatorCreator mBackgroundViewAnimatorCreator;
    private long mAnimatorDuration;

    private boolean _lockDialog;
    private boolean _tryStartShowAnimator;
    private boolean _isAnimatorCreatorModifiedInternal;

    private OnDismissListener mOnDismissListener;
    private OnShowListener mOnShowListener;

    private boolean mIsDebug;

    public FDialog(Activity activity)
    {
        if (activity == null)
        {
            throw new NullPointerException("activity is null");
        }

        mActivity = activity;

        _dialogView = new InternalDialogView(activity);
        _containerView = _dialogView.mContainerView;
        _backgroundView = _dialogView.mBackgroundView;

        final int defaultPadding = (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.1f);
        setPadding(defaultPadding, 0, defaultPadding, 0);

        setBackgroundDim(true);
        setGravity(Gravity.CENTER);
    }

    @Override
    public void setDebug(boolean debug)
    {
        mIsDebug = debug;
    }

    @Override
    public Context getContext()
    {
        return mActivity;
    }

    @Override
    public Activity getOwnerActivity()
    {
        return mActivity;
    }

    @Override
    public View getContentView()
    {
        return mContentView;
    }

    @Override
    public void setContentView(int layoutId)
    {
        final View view = LayoutInflater.from(mActivity).inflate(layoutId, _containerView, false);
        setContentView(view);
    }

    @Override
    public void setContentView(View view)
    {
        setContentViewInternal(view);
    }

    @Override
    public void setBackgroundDim(boolean backgroundDim)
    {
        if (mIsBackgroundDim != backgroundDim)
        {
            mIsBackgroundDim = backgroundDim;
            if (backgroundDim)
            {
                final int color = mActivity.getResources().getColor(R.color.lib_dialog_background_dim);
                _backgroundView.setBackgroundColor(color);
            } else
            {
                _backgroundView.setBackgroundColor(0);
            }
        }
    }

    private void setContentViewInternal(View view)
    {
        final View old = mContentView;
        if (old != view)
        {
            mContentView = view;

            if (old != null)
            {
                _containerView.removeView(old);
            }

            if (view != null)
            {
                final ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                final ViewGroup.LayoutParams params = view.getLayoutParams();
                if (params != null)
                {
                    p.width = params.width;
                    p.height = params.height;
                }

                _containerView.addView(view, p);
            }

            if (mIsDebug)
            {
                Log.i(IDialog.class.getSimpleName(), "onContentViewChanged:" + old + " , " + view);
            }

            onContentViewChanged(old, view);
        }
    }

    protected void onContentViewChanged(View oldView, View contentView)
    {
    }

    @Override
    public <T extends View> T findViewById(int id)
    {
        if (mContentView == null)
        {
            return null;
        }
        return mContentView.findViewById(id);
    }

    @Override
    public void setCanceledOnTouchOutside(boolean cancel)
    {
        mCanceledOnTouchOutside = cancel;
    }

    @Override
    public void setOnDismissListener(OnDismissListener listener)
    {
        mOnDismissListener = listener;
    }

    @Override
    public void setOnShowListener(OnShowListener listener)
    {
        mOnShowListener = listener;
    }

    @Override
    public void setAnimatorCreator(AnimatorCreator creator)
    {
        mAnimatorCreator = creator;
        _isAnimatorCreatorModifiedInternal = false;
    }

    @Override
    public AnimatorCreator getAnimatorCreator()
    {
        return mAnimatorCreator;
    }

    @Override
    public void setAnimatorDuration(long duration)
    {
        mAnimatorDuration = duration;
    }

    @Override
    public void setGravity(int gravity)
    {
        _containerView.setGravity(gravity);
    }

    @Override
    public int getGravity()
    {
        return mGravity;
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom)
    {
        _containerView.setPadding(left, top, right, bottom);
    }

    @Override
    public int getPaddingLeft()
    {
        return _containerView.getPaddingLeft();
    }

    @Override
    public int getPaddingTop()
    {
        return _containerView.getPaddingTop();
    }

    @Override
    public int getPaddingRight()
    {
        return _containerView.getPaddingRight();
    }

    @Override
    public int getPaddingBottom()
    {
        return _containerView.getPaddingBottom();
    }

    @Override
    public boolean isShowing()
    {
        return _state == State.Shown;
    }

    @Override
    public void show()
    {
        if (Looper.myLooper() == Looper.getMainLooper())
        {
            mShowRunnable.run();
        } else
        {
            getDialogHandler().removeCallbacks(mShowRunnable);
            getDialogHandler().post(mShowRunnable);
        }
    }

    @Override
    public void dismiss()
    {
        if (Looper.myLooper() == Looper.getMainLooper())
        {
            mDismissRunnable.run();
        } else
        {
            getDialogHandler().removeCallbacks(mDismissRunnable);
            getDialogHandler().post(mDismissRunnable);
        }
    }

    private final Runnable mShowRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            final boolean isFinishing = mActivity.isFinishing();
            if (mIsDebug)
            {
                Log.i(IDialog.class.getSimpleName(), "try show isFinishing:" + isFinishing);
            }

            if (isFinishing)
            {
                return;
            }

            if (_state.isShowPart())
            {
                return;
            }

            setState(State.TryShow);

            if (getAnimatorHandler().isHideAnimatorStarted())
            {
                if (mIsDebug)
                {
                    Log.i(IDialog.class.getSimpleName(), "cancel HideAnimator before show");
                }
                getAnimatorHandler().cancelHideAnimator();
            }

            showDialog();
        }
    };

    private final Runnable mDismissRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            final boolean isFinishing = mActivity.isFinishing();
            if (mIsDebug)
            {
                Log.i(IDialog.class.getSimpleName(), "try dismiss isFinishing:" + isFinishing);
            }

            if (isFinishing)
            {
                if (getAnimatorHandler().isShowAnimatorStarted())
                {
                    getAnimatorHandler().cancelShowAnimator();
                }

                if (getAnimatorHandler().isHideAnimatorStarted())
                {
                    getAnimatorHandler().cancelHideAnimator();
                }

                setLockDialog(true);
                dismissDialog(false);
                return;
            }

            if (_state.isDismissPart())
            {
                return;
            }

            setState(State.TryDismiss);

            if (getAnimatorHandler().isShowAnimatorStarted())
            {
                if (mIsDebug)
                {
                    Log.i(IDialog.class.getSimpleName(), "cancel ShowAnimator before dismiss");
                }
                getAnimatorHandler().cancelShowAnimator();
            }

            setLockDialog(true);

            getAnimatorHandler().setHideAnimator(createAnimator(false));
            if (getAnimatorHandler().startHideAnimator())
            {
                // 等待动画结束后让窗口消失
            } else
            {
                dismissDialog(false);
            }
        }
    };

    private void setLockDialog(boolean lock)
    {
        if (_lockDialog != lock)
        {
            _lockDialog = lock;
            if (mIsDebug)
            {
                Log.i(IDialog.class.getSimpleName(), "setLockDialog:" + lock);
            }
        }
    }

    private void setTryStartShowAnimator(boolean tryShow)
    {
        if (_tryStartShowAnimator != tryShow)
        {
            _tryStartShowAnimator = tryShow;
            if (mIsDebug)
            {
                Log.i(IDialog.class.getSimpleName(), "setTryStartShowAnimator:" + tryShow);
            }
        }
    }

    private void startShowAnimator()
    {
        if (_tryStartShowAnimator)
        {
            final int width = _containerView.getWidth();
            final int height = _containerView.getHeight();
            if (mIsDebug)
            {
                Log.i(IDialog.class.getSimpleName(), "startShowAnimator width:" + width + " height:" + height);
            }

            if (width > 0 && height > 0)
            {
                setTryStartShowAnimator(false);
                getAnimatorHandler().setShowAnimator(createAnimator(true));
                getAnimatorHandler().startShowAnimator();
            }
        }
    }

    private void setState(State state)
    {
        if (state == null)
        {
            throw new IllegalArgumentException("state is null");
        }

        if (_state != state)
        {
            _state = state;
            if (mIsDebug)
            {
                Log.e(IDialog.class.getSimpleName(), "setState:" + state);
            }

            if (state.isDismissPart())
            {
                setTryStartShowAnimator(false);
            }
        }
    }

    @Override
    public void startDismissRunnable(long delay)
    {
        stopDismissRunnable();
        getDialogHandler().postDelayed(mDelayedDismissRunnable, delay);
    }

    @Override
    public void stopDismissRunnable()
    {
        getDialogHandler().removeCallbacks(mDelayedDismissRunnable);
    }

    private Handler mDialogHandler;

    private Handler getDialogHandler()
    {
        if (mDialogHandler == null)
        {
            mDialogHandler = new Handler(Looper.getMainLooper());
        }
        return mDialogHandler;
    }

    private final Runnable mDelayedDismissRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            dismiss();
        }
    };

    private void setDefaultConfigBeforeShow()
    {
        if (mAnimatorCreator == null)
        {
            switch (mGravity)
            {
                case Gravity.CENTER:
                    setAnimatorCreator(new AlphaCreator());
                    _isAnimatorCreatorModifiedInternal = true;
                    break;
                case Gravity.LEFT:
                case Gravity.LEFT | Gravity.CENTER:
                    setAnimatorCreator(new SlideRightLeftParentCreator());
                    _isAnimatorCreatorModifiedInternal = true;
                    break;
                case Gravity.TOP:
                case Gravity.TOP | Gravity.CENTER:
                    setAnimatorCreator(new SlideBottomTopParentCreator());
                    _isAnimatorCreatorModifiedInternal = true;
                    break;
                case Gravity.RIGHT:
                case Gravity.RIGHT | Gravity.CENTER:
                    setAnimatorCreator(new SlideLeftRightParentCreator());
                    _isAnimatorCreatorModifiedInternal = true;
                    break;
                case Gravity.BOTTOM:
                case Gravity.BOTTOM | Gravity.CENTER:
                    setAnimatorCreator(new SlideTopBottomParentCreator());
                    _isAnimatorCreatorModifiedInternal = true;
                    break;
            }
        }
    }

    private FVisibilityAnimatorHandler getAnimatorHandler()
    {
        if (mAnimatorHandler == null)
        {
            mAnimatorHandler = new FVisibilityAnimatorHandler();
            mAnimatorHandler.setShowAnimatorListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationStart(Animator animation)
                {
                    super.onAnimationStart(animation);
                    if (mIsDebug)
                    {
                        Log.i(IDialog.class.getSimpleName(), "show onAnimationStart ");
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation)
                {
                    super.onAnimationCancel(animation);
                    if (mIsDebug)
                    {
                        Log.i(IDialog.class.getSimpleName(), "show onAnimationCancel ");
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation)
                {
                    super.onAnimationEnd(animation);
                    if (mIsDebug)
                    {
                        Log.i(IDialog.class.getSimpleName(), "show onAnimationEnd ");
                    }
                }
            });
            mAnimatorHandler.setHideAnimatorListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationStart(Animator animation)
                {
                    super.onAnimationStart(animation);
                    if (mIsDebug)
                    {
                        Log.i(IDialog.class.getSimpleName(), "dismiss onAnimationStart ");
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation)
                {
                    super.onAnimationCancel(animation);
                    if (mIsDebug)
                    {
                        Log.i(IDialog.class.getSimpleName(), "dismiss onAnimationCancel ");
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation)
                {
                    super.onAnimationEnd(animation);
                    if (mIsDebug)
                    {
                        Log.i(IDialog.class.getSimpleName(), "dismiss onAnimationEnd ");
                    }
                    dismissDialog(true);
                }
            });
        }
        return mAnimatorHandler;
    }

    private Animator createAnimator(boolean show)
    {
        Animator animator = null;

        Animator animatorBackground = null;
        if (show)
        {
            if (mIsBackgroundDim)
            {
                animatorBackground = getBackgroundViewAnimatorCreator().createAnimator(true, _backgroundView);
            }
        } else
        {
            if (mIsBackgroundDim)
            {
                animatorBackground = getBackgroundViewAnimatorCreator().createAnimator(false, _backgroundView);
            }
        }

        final Animator animatorContent = (mAnimatorCreator == null || mContentView == null) ?
                null : mAnimatorCreator.createAnimator(show, mContentView);

        if (animatorBackground != null && animatorContent != null)
        {
            final long duration = Utils.getAnimatorDuration(animatorContent);
            if (duration < 0)
            {
                throw new RuntimeException("Illegal duration:" + duration);
            }
            animatorBackground.setDuration(duration);

            final AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(animatorBackground).with(animatorContent);
            animator = animatorSet;
        } else
        {
            animator = animatorContent;
        }

        if (mAnimatorDuration > 0)
        {
            if (animator != null)
            {
                animator.setDuration(mAnimatorDuration);
            }
        }

        if (mIsDebug)
        {
            Log.i(IDialog.class.getSimpleName(), "createAnimator " + (show ? "show" : "dismiss") + " animator " + (animator == null ? "null" : "not null"));
        }

        return animator;
    }

    private AnimatorCreator getBackgroundViewAnimatorCreator()
    {
        if (mBackgroundViewAnimatorCreator == null)
        {
            mBackgroundViewAnimatorCreator = new ObjectAnimatorCreator()
            {
                @Override
                protected String getPropertyName()
                {
                    return View.ALPHA.getName();
                }

                @Override
                protected float getValueHidden(View view)
                {
                    return 0.0f;
                }

                @Override
                protected float getValueShown(View view)
                {
                    return 1.0f;
                }

                @Override
                protected float getValueCurrent(View view)
                {
                    return view.getAlpha();
                }

                @Override
                protected void onAnimationStart(boolean show, View view)
                {
                    super.onAnimationStart(show, view);
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onAnimationEnd(boolean show, View view)
                {
                    super.onAnimationEnd(show, view);
                    if (!show)
                    {
                        view.setVisibility(View.INVISIBLE);
                    }
                }
            };
        }
        return mBackgroundViewAnimatorCreator;
    }

    protected void onCreate(Bundle savedInstanceState)
    {
    }

    protected void onSaveInstanceState(Bundle bundle)
    {
    }

    protected void onStart()
    {
    }

    protected void onStop()
    {
    }

    protected boolean onTouchEvent(MotionEvent event)
    {
        return false;
    }

    protected void onTouchOutside(MotionEvent event)
    {
    }

    private SimpleTargetDialog mTargetDialog;

    @Override
    public ITargetDialog target()
    {
        if (mTargetDialog == null)
        {
            mTargetDialog = new SimpleTargetDialog(this);
        }
        return mTargetDialog;
    }

    private final class InternalDialogView extends FrameLayout
    {
        private static final String KEY_SUPER_STATE = "InternalDialogView_super_onSaveInstanceState";

        private final View mBackgroundView;
        private final LinearLayout mContainerView;

        private boolean mShouldNotifyOnCreate = true;
        private Bundle mSavedInstanceState = null;

        public InternalDialogView(Context context)
        {
            super(context);
            mBackgroundView = new InternalBackgroundView(context);
            addView(mBackgroundView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            mContainerView = new InternalContainerView(context);
            addView(mContainerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        @Override
        protected Parcelable onSaveInstanceState()
        {
            final Bundle bundle = new Bundle();
            FDialog.this.onSaveInstanceState(bundle);
            bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState());
            return bundle;
        }

        @Override
        protected void onRestoreInstanceState(Parcelable state)
        {
            if (state instanceof Bundle)
            {
                final Bundle bundle = (Bundle) state;
                super.onRestoreInstanceState(bundle.getParcelable(KEY_SUPER_STATE));
                mSavedInstanceState = bundle;
                notifyCreate();
            } else
            {
                super.onRestoreInstanceState(state);
            }
        }

        private void notifyCreate()
        {
            if (mShouldNotifyOnCreate)
            {
                mShouldNotifyOnCreate = false;
                FDialog.this.onCreate(mSavedInstanceState);
            }
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev)
        {
            if (_lockDialog)
            {
                return true;
            }
            return super.onInterceptTouchEvent(ev);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            if (_lockDialog)
            {
                return true;
            }

            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                final boolean isViewUnder = Utils.isViewUnder(mContentView, (int) event.getX(), (int) event.getY());
                if (isViewUnder)
                {
                    // 不处理
                } else
                {
                    onTouchOutside(event);
                    if (mCanceledOnTouchOutside)
                    {
                        if (mIsDebug)
                        {
                            Log.i(IDialog.class.getSimpleName(), "touch outside try dismiss");
                        }
                        dismiss();
                        return true;
                    }
                }
            }

            if (FDialog.this.onTouchEvent(event))
            {
                return true;
            }

            super.onTouchEvent(event);
            return true;
        }

        @Override
        protected void onAttachedToWindow()
        {
            super.onAttachedToWindow();
            if (mIsDebug)
            {
                Log.i(IDialog.class.getSimpleName(), "onAttachedToWindow");
            }
            notifyCreate();
            notifyStart();
        }

        @Override
        protected void onDetachedFromWindow()
        {
            super.onDetachedFromWindow();
            if (mIsDebug)
            {
                Log.i(IDialog.class.getSimpleName(), "onDetachedFromWindow");
            }
            notifyStop();
        }

        @Override
        public void onViewAdded(View child)
        {
            super.onViewAdded(child);
            if (child != mBackgroundView && child != mContainerView)
            {
                throw new RuntimeException("you can not add view to dialog view");
            }
        }

        @Override
        public void onViewRemoved(View child)
        {
            super.onViewRemoved(child);
            if (child == mBackgroundView || child == mContainerView)
            {
                throw new RuntimeException("you can not remove dialog child");
            }
        }

        @Override
        public void setVisibility(int visibility)
        {
            if (visibility == GONE || visibility == INVISIBLE)
            {
                throw new IllegalArgumentException("you can not hide dialog");
            }
            super.setVisibility(visibility);
        }
    }

    private final class InternalContainerView extends LinearLayout
    {
        public InternalContainerView(Context context)
        {
            super(context);
        }

        @Override
        public void setGravity(int gravity)
        {
            if (mGravity != gravity)
            {
                mGravity = gravity;
                super.setGravity(gravity);
            }
        }

        @Override
        public void setPadding(int left, int top, int right, int bottom)
        {
            if (left < 0)
            {
                left = getPaddingLeft();
            }
            if (top < 0)
            {
                top = getPaddingTop();
            }
            if (right < 0)
            {
                right = getPaddingRight();
            }
            if (bottom < 0)
            {
                bottom = getPaddingBottom();
            }

            if (left != getPaddingLeft() || top != getPaddingTop()
                    || right != getPaddingRight() || bottom != getPaddingBottom())
            {
                super.setPadding(left, top, right, bottom);
            }
        }

        @Override
        public void setVisibility(int visibility)
        {
            if (visibility == GONE || visibility == INVISIBLE)
            {
                throw new IllegalArgumentException("you can not hide container");
            }
            super.setVisibility(visibility);
        }

        @Override
        public void onViewAdded(View child)
        {
            super.onViewAdded(child);
            if (child != mContentView)
            {
                throw new RuntimeException("you can not add view to container");
            }

            if (mIsDebug)
            {
                Log.i(IDialog.class.getSimpleName(), "onContentViewAdded:" + child);
            }
        }

        @Override
        public void onViewRemoved(View child)
        {
            super.onViewRemoved(child);
            if (child == mContentView)
            {
                // 外部直接移除内容view的话，关闭窗口
                dismiss();
            }

            if (mIsDebug)
            {
                Log.i(IDialog.class.getSimpleName(), "onContentViewRemoved:" + child);
            }
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b)
        {
            super.onLayout(changed, l, t, r, b);
            if (changed)
            {
                Utils.checkMatchLayoutParams(this);
            }
            startShowAnimator();
        }

        @Override
        protected void onAttachedToWindow()
        {
            super.onAttachedToWindow();
            if (_state.isShowPart())
            {
                setTryStartShowAnimator(true);
                startShowAnimator();
            }
        }
    }

    private final class InternalBackgroundView extends View
    {
        public InternalBackgroundView(Context context)
        {
            super(context);
            setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void showDialog()
    {
        if (mIsDebug)
        {
            Log.e(IDialog.class.getSimpleName(), "showDialog");
        }

        try
        {
            final ViewGroup container = mActivity.findViewById(android.R.id.content);
            container.addView(_dialogView,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            setState(State.Shown);
        } catch (Exception e)
        {
            e.printStackTrace();
            if (mIsDebug)
            {
                Log.e(IDialog.class.getSimpleName(), "showDialog error:" + e);
            }
            dismissDialog(false);
        }
    }

    private void dismissDialog(boolean isAnimator)
    {
        if (mIsDebug)
        {
            Log.e(IDialog.class.getSimpleName(), "dismissDialog by animator:" + isAnimator);
        }

        try
        {
            final ViewGroup container = mActivity.findViewById(android.R.id.content);
            container.removeView(_dialogView);
        } catch (Exception e)
        {
            e.printStackTrace();
            if (mIsDebug)
            {
                Log.e(IDialog.class.getSimpleName(), "dismissDialog error:" + e);
            }
        } finally
        {
            setState(State.Dismissed);
        }
    }

    private void notifyStart()
    {
        if (mIsDebug)
        {
            Log.i(IDialog.class.getSimpleName(), "notifyStart");
        }

        getActivityLifecycleCallbacks().register(true);
        FDialogHolder.addDialog(FDialog.this);

        FDialog.this.onStart();
        if (mTargetDialog != null)
        {
            mTargetDialog.onStart();
        }

        setLockDialog(false);
        setDefaultConfigBeforeShow();

        getDialogHandler().post(new Runnable()
        {
            @Override
            public void run()
            {
                if (mOnShowListener != null)
                {
                    mOnShowListener.onShow(FDialog.this);
                }
            }
        });
    }

    private void notifyStop()
    {
        if (mIsDebug)
        {
            Log.i(IDialog.class.getSimpleName(), "notifyStop");
        }

        getActivityLifecycleCallbacks().register(false);
        FDialogHolder.removeDialog(FDialog.this);

        stopDismissRunnable();

        FDialog.this.onStop();
        if (mTargetDialog != null)
        {
            mTargetDialog.onStop();
        }

        if (_isAnimatorCreatorModifiedInternal)
        {
            setAnimatorCreator(null);
        }

        getDialogHandler().post(new Runnable()
        {
            @Override
            public void run()
            {
                if (mOnDismissListener != null)
                {
                    mOnDismissListener.onDismiss(FDialog.this);
                }
            }
        });
    }

    private InternalActivityLifecycleCallbacks mActivityLifecycleCallbacks;

    private InternalActivityLifecycleCallbacks getActivityLifecycleCallbacks()
    {
        if (mActivityLifecycleCallbacks == null)
        {
            mActivityLifecycleCallbacks = new InternalActivityLifecycleCallbacks();
        }
        return mActivityLifecycleCallbacks;
    }

    private final class InternalActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks
    {
        public void register(boolean register)
        {
            final Application application = mActivity.getApplication();
            application.unregisterActivityLifecycleCallbacks(this);
            if (register)
            {
                application.registerActivityLifecycleCallbacks(this);
            }
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState)
        {
        }

        @Override
        public void onActivityStarted(Activity activity)
        {
        }

        @Override
        public void onActivityResumed(Activity activity)
        {
        }

        @Override
        public void onActivityPaused(Activity activity)
        {
        }

        @Override
        public void onActivityStopped(Activity activity)
        {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState)
        {
        }

        @Override
        public void onActivityDestroyed(Activity activity)
        {
            if (activity == mActivity)
            {
                if (mIsDebug)
                {
                    Log.e(IDialog.class.getSimpleName(), "onActivityDestroyed");
                }

                FDialogHolder.remove(getOwnerActivity());
                dismiss();
            }
        }
    }

    /**
     * 返回Activity的所有窗口
     */
    public static List<FDialog> getAll(Activity activity)
    {
        return FDialogHolder.get(activity);
    }

    /**
     * 关闭指定Activity的所有窗口
     */
    public static void dismissAll(Activity activity)
    {
        if (activity.isFinishing())
        {
            return;
        }

        final List<FDialog> list = getAll(activity);
        if (list == null || list.isEmpty())
        {
            return;
        }

        for (FDialog item : list)
        {
            item.dismiss();
        }
    }

    private enum State
    {
        TryShow,
        Shown,

        TryDismiss,
        Dismissed;

        public boolean isShowPart()
        {
            return this == Shown || this == TryShow;
        }

        public boolean isDismissPart()
        {
            return this == Dismissed || this == TryDismiss;
        }
    }
}
