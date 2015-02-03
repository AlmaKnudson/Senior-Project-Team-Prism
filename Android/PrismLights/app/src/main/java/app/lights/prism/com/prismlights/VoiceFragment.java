package app.lights.prism.com.prismlights;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

import ai.wit.sdk.IWitListener;
import ai.wit.sdk.Wit;
import ai.wit.sdk.model.WitOutcome;

public class VoiceFragment extends Fragment implements IWitListener {

    private Wit wit;
    private TextView micStatus;
    private TextView witResponse;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String accessToken = "2UZT7OIHBRHNJTFZLOW222ND5SVNRYM7";
        wit = new Wit(accessToken, this);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_voice_control, container, false);
        ImageButton mic = (ImageButton) layout.findViewById(R.id.micButton);
        micStatus = (TextView) layout.findViewById(R.id.micText);
        witResponse = (TextView) layout.findViewById(R.id.micResultText);
        wit.captureTextIntent("Turn lights on");
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    wit.toggleListening();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return layout;
    }

    @Override
    public void witDidGraspIntent(ArrayList<WitOutcome> witOutcomes, String s, Error error) {
        System.out.println(witOutcomes);
        if(witOutcomes != null && !witOutcomes.isEmpty() && error == null) {
            WitOutcome outcome = witOutcomes.get(0);
            witResponse.setText(outcome.get_text());
        }
        micStatus.setText(this.getActivity().getText(R.string.waiting));
    }

    @Override
    public void witDidStartListening() {
        micStatus.setText(this.getActivity().getText(R.string.listening));
    }

    @Override
    public void witDidStopListening() {
        micStatus.setText(this.getActivity().getText(R.string.processing));
    }

    @Override
    public void witActivityDetectorStarted() {
        micStatus.setText(this.getActivity().getText(R.string.listening));
    }

    @Override
    public String witGenerateMessageId() {
        return null;
    }
}
