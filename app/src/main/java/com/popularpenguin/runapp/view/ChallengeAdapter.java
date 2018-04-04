package com.popularpenguin.runapp.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public ChallengeAdapter(List<Challenge> challengeList, ChallengeAdapterOnClickHandler handler) {
        mChallengeList = challengeList;
        mClickHandler = handler;
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = R.layout.list_item;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(layout, parent, false);

        return new ChallengeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
        Challenge challenge = mChallengeList.get(position);

        holder.bind(challenge);
    }

    // TODO: Set animation (refer to XYZ Reader's ReaderAdapter class)

    @Override
    public int getItemCount() {
        return mChallengeList.size();
    }

    class ChallengeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.tv_item_name) TextView nameText;
        @BindView(R.id.tv_item_description) TextView descriptionText;

        ChallengeViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        void bind(@NonNull Challenge challenge) {
            nameText.setText(challenge.getName());
            descriptionText.setText(challenge.getDescription());
        }

        void clearAnimation() {
            itemView.clearAnimation();
        }

        @Override
        public void onClick(View view) {
            mClickHandler.onClick(getAdapterPosition());
        }
    }
}
