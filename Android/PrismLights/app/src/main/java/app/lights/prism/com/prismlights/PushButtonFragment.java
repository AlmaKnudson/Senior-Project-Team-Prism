package app.lights.prism.com.prismlights;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;


public class PushButtonFragment extends Fragment implements Animator.AnimatorListener {
    private Animator progressAnimator;

    public PushButtonFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_push_button, container, false);
        progressAnimator = AnimatorInflater.loadAnimator(getActivity(), R.animator.progress_animation);
        progressAnimator.setTarget(layout.findViewById(R.id.authenticationProgress));
        progressAnimator.start();
        progressAnimator.addListener(this);

        return layout;
    }


    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        ((MainActivity) getActivity()).showAuthenticationFailedDialog();
        getFragmentManager().popBackStack();
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(progressAnimator != null) {
            progressAnimator.removeAllListeners();
        }
    }
}
