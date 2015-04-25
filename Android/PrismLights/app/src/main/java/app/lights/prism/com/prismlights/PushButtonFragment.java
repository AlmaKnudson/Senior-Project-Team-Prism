package app.lights.prism.com.prismlights;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;

import java.util.List;


public class PushButtonFragment extends Fragment implements Animator.AnimatorListener, LocationListener{
    private Animator progressAnimator;
    private LocationManager locationManager;

    public PushButtonFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //start the service that update sunrise sunset schedules daily.
        MainActivity mainActivity = (MainActivity)getActivity();
        mainActivity.setAlarmBroadcasting(mainActivity);

        //try to get location.
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_LOW);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);

        //if there is no access to location make
        if (location!=null) {
            mainActivity.setLatitude(location.getLatitude());
            mainActivity.setLongitude(location.getLongitude());
        } else{
                locationManager.requestLocationUpdates(provider, 0, 0, this);
        }
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

    @Override
    public void onLocationChanged(Location location) {
        MainActivity mainActivity = (MainActivity)getActivity();
        mainActivity.setLatitude(location.getLatitude());
        mainActivity.setLongitude(location.getLongitude());
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
