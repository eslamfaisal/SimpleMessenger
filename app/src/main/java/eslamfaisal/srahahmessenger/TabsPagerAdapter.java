package eslamfaisal.srahahmessenger;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 2:
                return new RequestsFragment();
            case 0:
                return new ChatsFragment();
            case 1:
                return new FriendsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position){
        switch (position) {
            case 2:
                return "Requests";
            case 0:
                return "Chats";
            case 1:
                return "Friends";
            default:
                return null;
        }
    }
}
