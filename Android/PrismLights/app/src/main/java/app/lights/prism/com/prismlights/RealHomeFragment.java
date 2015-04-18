package app.lights.prism.com.prismlights;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageButton;

import com.philips.lighting.hue.sdk.PHHueSDK;

import java.util.HashSet;

public class RealHomeFragment extends Fragment implements ViewPager.OnPageChangeListener, CacheUpdateListener{

    public static final String favoritePosition="FAVORITE_POSITION";
    private ViewPager viewPager;
    private View bulbIconSelected;
    private View bulbIconDeselected;
    private View groupIconSelected;
    private View groupIconDeselected;
    private View favoriteIconSelected;
    private View favoriteIconDeselected;

    public static final int disabledOverlay = Color.argb(125, 0, 0, 0);
    public static final int offOverlay = Color.argb(50, 0, 0, 0);

    public static final String lightPositionString = "CURRENT_BULB_ID";
    public static final String groupOrLightString = "GROUP_OR_LIGHT";
    private ImageButton editButton;
    private ImageButton addButton;

    public RealHomeFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_real_home, container, false);
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        viewPager.setAdapter(new HomePageAdapter(getChildFragmentManager()));
        viewPager.setOnPageChangeListener(this);
        groupIconDeselected = view.findViewById(R.id.groupsIconDeselected);
        groupIconSelected = view.findViewById(R.id.groupsIconSelected);
        favoriteIconDeselected = view.findViewById(R.id.favoritesIconDeselected);
        favoriteIconSelected = view.findViewById(R.id.favoritesIconSelected);
        bulbIconSelected = view.findViewById(R.id.bulbsIconSelected);
        bulbIconDeselected = view.findViewById(R.id.bulbsIconDeselected);
        editButton = (ImageButton) view.findViewById(R.id.editButton);
        addButton = (ImageButton) view.findViewById(R.id.addButton);
        onPageSelected(0);
        return view;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        updateFromCache();
        switch(position) {
            case 0:
                turnOffView(groupIconSelected, groupIconDeselected);
                turnOffView(favoriteIconSelected, favoriteIconDeselected);
                turnOnView(bulbIconSelected, bulbIconDeselected);
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                        AddLightFragment addLightFragment = new AddLightFragment();
                        fragmentTransaction.replace(R.id.container, addLightFragment);
                        fragmentTransaction.addToBackStack("lightsettings");
                        fragmentTransaction.commit();
                    }
                });
                editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                        LightsEditFragment editLightsFragment = new LightsEditFragment();
                        fragmentTransaction.replace(R.id.container, editLightsFragment);
                        fragmentTransaction.addToBackStack("lightsettings");
                        fragmentTransaction.commit();
                    }
                });
                return;
            case 1:
                turnOnView(groupIconSelected, groupIconDeselected);
                turnOffView(favoriteIconSelected, favoriteIconDeselected);
                turnOffView(bulbIconSelected, bulbIconDeselected);
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                        AddGroupFragment addGroupFragment = new AddGroupFragment();
                        fragmentTransaction.replace(R.id.container, addGroupFragment);
                        fragmentTransaction.addToBackStack("lightsettings");
                        fragmentTransaction.commit();
                    }
                });
                return;
            case 2:
                turnOffView(groupIconSelected, groupIconDeselected);
                turnOnView(favoriteIconSelected, favoriteIconDeselected);
                turnOffView(bulbIconSelected, bulbIconDeselected);
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                        AddFavoriteFragment addFavoriteFragment = new AddFavoriteFragment();
                        fragmentTransaction.replace(R.id.container, addFavoriteFragment);
                        fragmentTransaction.addToBackStack("lightsettings");
                        fragmentTransaction.commit();
                    }
                });
                return;
            default:
                turnOffView(groupIconSelected, groupIconDeselected);
                turnOffView(favoriteIconSelected, favoriteIconDeselected);
                turnOnView(bulbIconSelected, bulbIconDeselected);

        }
    }

    private void turnOffView(View selected, View deselected) {
        deselected.setVisibility(View.VISIBLE);
        selected.setVisibility(View.INVISIBLE);
    }

    private void turnOnView(View selected, View deselected) {
        selected.setVisibility(View.VISIBLE);
        deselected.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void cacheUpdated() {
        updateFromCache();
    }

    private void updateFromCache() {
        Fragment fragment = getChildFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + viewPager.getCurrentItem());
        //Favorites fragment does not implement this
        if(fragment instanceof CacheUpdateListener) {
            ((CacheUpdateListener) fragment).cacheUpdated();
        }
    }


    class HomePageAdapter extends FragmentPagerAdapter {
        public HomePageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return new LightsFragment();
                case 1:
                    return new GroupsFragment();
                case 2:
                    return new FavoritesFragment();
                default:
                    return new FavoritesFragment();
            }

        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
