package arashincleric.com.econsquarestudy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment for a rest screen
 */
public class RestScreenFragment extends TaskScreenFragment {

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment RestScreenFragment.
     */
    public static RestScreenFragment newInstance(int maxRestTime) {
        RestScreenFragment fragment = new RestScreenFragment();
        Bundle args = new Bundle();
        args.putInt(TaskScreenFragment.ARG_MAX_SCREEN_TIME, maxRestTime);
        fragment.setArguments(args);
        return fragment;
    }

    public RestScreenFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rest_screen, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        if(getArguments() != null){
            int maxScreenTime = getArguments().getInt(TaskScreenFragment.ARG_MAX_SCREEN_TIME);

            performTick(maxScreenTime);
            startTimer();
        }
    }

    @Override
    public boolean isActiveScreen(){
        return false;
    }

}
