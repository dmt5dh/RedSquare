package arashincleric.com.econsquarestudy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Random;


public class ActiveScreenFragment extends TaskScreenFragment {

    private static String ARG_GOLD_PROBABILITY = "ARG_GOLD_PROBABILITY";

    private ImageView redSquare;
    private ImageView goldSquare;

    private int screenWidth;
    private int screenHeight;

    private Random random;
    private RelativeLayout headLayout;
    private Button gotoRestBtn;

    private OnActiveScreenFragmentInteractionListener mActiveListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ActiveScreenFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ActiveScreenFragment newInstance(int maxActiveTime,
                                                   double goldPropability) {
        ActiveScreenFragment fragment = new ActiveScreenFragment();
        Bundle args = new Bundle();
        args.putInt(TaskScreenFragment.ARG_MAX_SCREEN_TIME, maxActiveTime);
        args.putDouble(ARG_GOLD_PROBABILITY, goldPropability);
        fragment.setArguments(args);
        return fragment;
    }

    public ActiveScreenFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        random = new Random();

        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_active_screen, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
        }

        gotoRestBtn = (Button)view.findViewById(R.id.gotoRestBtn);
        gotoRestBtn.setVisibility(View.GONE);
        gotoRestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: go to restbtn event here
                mTaskListener.switchTaskScreen(isActiveScreen());
            }
        });

        headLayout = (RelativeLayout) view.findViewById(R.id.activeScreenLayout);
        ViewTreeObserver observer = headLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                screenWidth = headLayout.getWidth(); //X coord
                screenHeight = headLayout.getHeight(); //Y coord

                headLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                drawRedSquare(screenWidth, screenHeight);
                if (getArguments() != null) {
                    double ran = random.nextDouble();
                    if (ran < getArguments().getDouble(ARG_GOLD_PROBABILITY)) {
                        drawGoldSquare(screenWidth, screenHeight);
                        mActiveListener.sendData(true);
                    }
                    else{
                        mActiveListener.sendData(false);
                    }
                }
            }
        });

        int maxScreenTime = getArguments().getInt(TaskScreenFragment.ARG_MAX_SCREEN_TIME);

        performTick(maxScreenTime);
        startTimer();

    }

    //Because can't do it in the listener above
    //TODO: delete this comment ^^
    //This can be one method but code more readable with two
    public void drawRedSquare(int screenWidth, int screenHeight){
        if (redSquare != null) {
            headLayout.removeView(redSquare);
        }

        redSquare = new ImageView(getContext());
        redSquare.setImageResource(R.drawable.red_square);
        int redX = random.nextInt(screenWidth);
        int redY = random.nextInt(screenHeight);

        if (redX > (screenWidth - 150)) { //the square is 55 pixels we want it to be reasonably in the screen
            redX = redX - 150;
        } else if (redX < 150) {
            redX = redX + 150;
        }

        if (redY > (screenHeight - 150)) {
            redY = redY - 150;
        } else if (redY < 150) {
            redY = redY + 150;
        }

        redSquare.setX(redX);
        redSquare.setY(redY);
        headLayout.addView(redSquare);

        redSquare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActiveListener.redClicked();
                headLayout.removeView(redSquare);
                gotoRestBtn.setVisibility(View.VISIBLE);
            }
        });
    }

    public void drawGoldSquare(int screenWidth, int screenHeight){
        if(goldSquare != null){
            headLayout.removeView(goldSquare);
        }

        goldSquare = new ImageView(getContext());
        goldSquare.setImageResource(R.drawable.gold_square);
        int goldX = random.nextInt(screenWidth);
        int goldY = random.nextInt(screenHeight);

        while (Math.abs(redSquare.getX() - goldX) < 200 //Enough buffer so we dont accidently click one or the other
                || Math.abs(redSquare.getY() - goldY) < 200
                || goldX > (screenWidth - 150) || goldX < 55
                || goldY > (screenHeight - 150) || goldY < 55) {
            goldX = random.nextInt(screenWidth);
            goldY = random.nextInt(screenHeight);
        }

        goldSquare.setX(goldX);
        goldSquare.setY(goldY);
        headLayout.addView(goldSquare);

        goldSquare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActiveListener.goldClicked();
                headLayout.removeView(goldSquare);
            }
        });

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mActiveListener = (OnActiveScreenFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnActiveScreenFragmentInteractionListener");
        }
    }

    @Override
    public boolean isActiveScreen(){
        return true;
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
    public interface OnActiveScreenFragmentInteractionListener {
        // TODO: Update argument type and name
        public void goldClicked();
        public void redClicked();
        public void sendData(boolean goldPresent);
    }

}
