package arashincleric.com.econsquarestudy;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class EnterEmailFragment extends Fragment {

    private OnEnterEmailFragmentInteractionListener mListener;
    private String userEmail;

    private EditText emailEntry;
    private EditText emailEntryConfirm;
    private Button submitBtn;

    /**
     * Use this to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EnterEmailFragment.
     */
    public static EnterEmailFragment newInstance() {
        EnterEmailFragment fragment = new EnterEmailFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public EnterEmailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
//        if (getArguments() != null) {
//        }
    }

    @Override
    public void onResume(){
        super.onResume();
        // If the Android version is lower than Jellybean, use this call to hide
        // the status bar.
        if (Build.VERSION.SDK_INT < 16) {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else{
            final View decorView = getActivity().getWindow().getDecorView();
            // Hide the status bar.
            final int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
//            decorView.setSystemUiVisibility(uiOptions);
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0){
                        decorView.setSystemUiVisibility(uiOptions);
                    }
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_enter_email, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        emailEntry = (EditText) view.findViewById(R.id.emailEntryInput);
        emailEntryConfirm = (EditText) view.findViewById(R.id.emailEntryInputConfirm);

        submitBtn = (Button) view.findViewById(R.id.submit_btn);
        submitBtn.setOnClickListener(new View.OnClickListener() { //Attach even to button
            @Override
            public void onClick(View v) {
                boolean emailMatch = confirmEmail();
                if(emailMatch){
                    mListener.sendUsername(emailEntry.getText().toString());
                }
                else{
                    new AlertDialog.Builder(v.getContext())
                            .setMessage(R.string.email_error)
                            .setNeutralButton(R.string.cancel_btn, null)
                            .show();
                }
            }
        });
    }

    /**
     * Check if the two emails match and that they are not empty
     * @return True if emails matched and weren't blank
     */
    public boolean confirmEmail(){
        String email = emailEntry.getText().toString();
        String emailConfirm = emailEntryConfirm.getText().toString();

        return !email.isEmpty() && !emailConfirm.isEmpty() && email.equals(emailConfirm);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnEnterEmailFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnEnterEmailFragmentInteractionListener");
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
     */
    public interface OnEnterEmailFragmentInteractionListener {
        public void sendUsername(String username);
    }

}
