package app.lights.prism.com.prismlights;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Creates an image view that animates the bulbs through 4 colors
 */
public class LoadingImageView extends FrameLayout {

    private CircularArray<ImageView> coloredBulbs;
    private int currentShown;

    private static final long animationDurationIn = 400;
    private static final long animationDurationOut = 1000;


    public LoadingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct(context);
    }

    public LoadingImageView(Context context) {
        super(context);
        construct(context);
    }

    public LoadingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        construct(context);
    }

    /**
     * sets up all the bulb images
     * @param context
     */
    private void construct(Context context) {
        //uses a circular array so I can increase the index every time I switch bulbs
        coloredBulbs = new CircularArray<ImageView>();
        ImageView firstBulb = new ImageView(context);
        ImageView secondBulb = new ImageView(context);
        ImageView thirdBulb = new ImageView(context);
        ImageView fourthBulb = new ImageView(context);
        firstBulb.setImageResource(R.drawable.first_bulb_color);
        secondBulb.setImageResource(R.drawable.second_bulb_color);
        secondBulb.setVisibility(GONE);
        thirdBulb.setImageResource(R.drawable.third_bulb_color);
        thirdBulb.setVisibility(GONE);
        fourthBulb.setImageResource(R.drawable.fourth_bulb_color);
        fourthBulb.setVisibility(GONE);
        coloredBulbs.add(firstBulb);
        coloredBulbs.add(secondBulb);
        coloredBulbs.add(thirdBulb);
        coloredBulbs.add(fourthBulb);
        addView(firstBulb);
        addView(secondBulb);
        addView(thirdBulb);
        addView(fourthBulb);
    }

    /**
     * Animates the bulbs when it's visible
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        animateBulbs();

    }

    /**
     * Animates the bulbs by fading from one view to another
     */
    private void animateBulbs() {
        final ImageView currentImageView = coloredBulbs.get(currentShown);
        ImageView nextImageView = coloredBulbs.get(currentShown + 1);
        currentImageView.setAlpha(1f);
        currentImageView.setVisibility(VISIBLE);
        nextImageView.setAlpha(0f);
        nextImageView.setVisibility(VISIBLE);
        currentImageView.animate().alpha(0f).setDuration(animationDurationOut).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                currentImageView.setVisibility(GONE);
                currentShown++;
                animateBulbs();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                System.out.println("animation cancelled");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        nextImageView.animate().alpha(1f).setDuration(animationDurationIn).setListener(null);
    }

    /**
     * Stop the animation by cancelling it on all the bulbs
     */
    private void stopAnimateBulb() {
        for(ImageView imageView : coloredBulbs) {
            imageView.animate().cancel();
        }
    }

    /**
     * Stops animating the bulbs when it's not visible
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimateBulb();
    }
}
