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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder> {

    public interface ChallengeAdapterOnClickHandler {
        void onClick(int position);
    }

    private final List<Challenge> mChallengeList;
    private final ChallengeAdapterOnClickHandler mClickHandler;
    private int lastPosition = -1; // save the last view position for animations

    public ChallengeAdapter(List<Challenge> challengeList, ChallengeAdapterOnClickHandler handler) {
        mChallengeList = challengeList;
        mClickHandler = handler;
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = R.layout.challenge_list_item;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(layout, parent, false);

        return new ChallengeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
        Challenge challenge = mChallengeList.get(position);

        holder.bind(challenge);

        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return mChallengeList.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ChallengeViewHolder holder) {
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

    class ChallengeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.tv_challenge_item_name) TextView nameText;
        @BindView(R.id.tv_challenge_item_description) TextView descriptionText;
        @BindView(R.id.tv_challenge_item_fastest_time) TextView timeText;

        ChallengeViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        void bind(@NonNull Challenge challenge) {
            int challengeColor = getChallengeColor(challenge);
            itemView.getRootView().setBackgroundColor(challengeColor);

            nameText.setText(challenge.getName());
            descriptionText.setText(challenge.getDescription());
            timeText.setText(challenge.getFastestTimeString());
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
