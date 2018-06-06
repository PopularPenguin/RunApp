package com.popularpenguin.runapp.adapter;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.popularpenguin.runapp.R;
import com.popularpenguin.runapp.data.Challenge;
import com.popularpenguin.runapp.data.Session;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {

    public interface SessionAdapterOnClickHandler {
        void onClick(int position);
    }

    private final List<Session> mSessionList;
    private final Resources mResources;
    private final SessionAdapterOnClickHandler mClickHandler;
    private int lastPosition = -1; // save the last view position for animations

    public SessionAdapter(List<Session> sessionList,
                          Resources resources,
                          SessionAdapterOnClickHandler handler) {

        mSessionList = sessionList;
        mResources = resources;
        mClickHandler = handler;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = R.layout.session_list_item;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(layout, parent, false);

        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        Session session = mSessionList.get(position);

        holder.bind(session);

        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return mSessionList.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull SessionViewHolder holder) {
        holder.clearAnimation();
    }

    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(view.getContext(),
                    R.anim.slide_up_bottom);
            view.startAnimation(animation);
            lastPosition = position;
        }
    }

    class SessionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.tv_session_item_name) TextView nameText;
        @BindView(R.id.tv_session_item_description) TextView descriptionText;
        @BindView(R.id.tv_session_item_time) TextView timeText;

        SessionViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        void bind(@NonNull Session session) {
            int challengeColor = getChallengeColor(session.getChallenge());
            itemView.getRootView().setBackgroundColor(challengeColor);

            String nameAndDate = String.format(Locale.US, "%s -\n%s",
                    session.getChallenge().getName(),
                    session.getDate());
            nameText.setText(nameAndDate);
            descriptionText.setText(session.getChallenge().getDescription());
            timeText.setText(session.getTimeString());

            // if the challenge failed, the time's color is red, else it is green
            if (session.getTime() >= session.getChallenge().getTimeToComplete()) {
                timeText.setTextColor(mResources.getColor(R.color.red));
            }
            else {
                timeText.setTextColor(mResources.getColor(R.color.green));
            }

            // set the content description dynamically
            itemView.setContentDescription(session.getChallenge().getDescription());
        }

        void clearAnimation() {
            itemView.clearAnimation();
        }

        int getChallengeColor(@NonNull Challenge challenge) {
            Resources resources = itemView.getResources();

            switch (challenge.getChallengeRating()) {
                case Challenge.EASY:
                    return resources.getColor(R.color.pastelBlue);

                case Challenge.MEDIUM:
                    return resources.getColor(R.color.cream);

                case Challenge.HARD:
                    return resources.getColor(R.color.lavaRed);

                default:
                    throw new IllegalArgumentException("Invalid challenge rating");
            }
        }

        @Override
        public void onClick(View view) {
            mClickHandler.onClick(getAdapterPosition());
        }
    }
}
