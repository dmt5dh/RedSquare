package arashincleric.com.econsquarestudy;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class TaskScreenFragment extends Fragment {

    public static String ARG_MAX_SCREEN_TIME = "ARG_MAX_SCREEN_TIME";
    private CountDownTimer timer;
    private long millisTimeRemaining;

    protected OnTaskScreenFragmentInteractionListener mTaskListener;

    public TaskScreenFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            millisTimeRemaining = getArguments().getInt(ARG_MAX_SCREEN_TIME);
        }
    }

    public void startTimer(){
        timer = new CountDownTimer(millisTimeRemaining, 100) {
            public void onTick(long millisUntilFinished) {
                millisTimeRemaining = millisUntilFinished;
                performTick(Math.round(millisUntilFinished));
            }

            public void onFinish() {
                performTick(0);
                mTaskListener.switchTaskScreen(isActiveScreen());
            }
        }.start();
    }

    public abstract boolean isActiveScreen();

    public void pauseTimer(){
        timer.cancel();
    }

    public void performTick(long millisUntilFinished){
        String time = String.format(getResources().getString(R.string.remaining_time),
                (millisUntilFinished / 1000) + 1);
//        remainingTimerView.setText(time);
        mTaskListener.countDown(time);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_task_screen, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mTaskListener = (OnTaskScreenFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        timer.cancel();
        super.onDetach();
        mTaskListener = null;
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
    public interface OnTaskScreenFragmentInteractionListener {
        public void countDown(String time);
        public void switchTaskScreen(boolean isActiveScreen);
    }

}
