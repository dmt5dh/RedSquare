package arashincleric.com.econsquarestudy;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


public class RestScreenFragment extends Fragment {

    private OnRestScreenFragmentInteractionListener mListener;

    public static String ARG_MAX_REST = "ARG_MAX_REST";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment RestScreenFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RestScreenFragment newInstance(int maxRestTime) {
        RestScreenFragment fragment = new RestScreenFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MAX_REST, maxRestTime);
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

    public void performTick(long millisUntilFinished){
        String time = String.format(getResources().getString(R.string.remaining_time),
                (millisUntilFinished / 1000) + 1);
//        remainingTimerView.setText(time);
        mListener.countDown(time);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rest_screen, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        if(getArguments() != null){
            int maxScreenTime = getArguments().getInt(ARG_MAX_REST);
            //Listener does not hit until first tick "skipping" a second
            performTick(maxScreenTime);

            new CountDownTimer(maxScreenTime, 100) {
                public void onTick(long millisUntilFinished) {
                    performTick(Math.round(millisUntilFinished));
                }

                public void onFinish() {
                    performTick(0);
                    mListener.goToActive();
                }
            }.start();
        }
    }

//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnRestScreenFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnRestScreenFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnRestScreenFragmentInteractionListener {
        public void countDown(String time);
        public void goToActive();
    }

}
