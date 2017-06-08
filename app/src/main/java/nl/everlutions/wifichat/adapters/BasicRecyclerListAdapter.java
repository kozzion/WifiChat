package nl.everlutions.wifichat.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.everlutions.wifichat.R;


public class BasicRecyclerListAdapter extends RecyclerView.Adapter<BasicRecyclerListAdapter.ViewHolder> {

    private final Context mCtx;
    private final View.OnClickListener mClickListener;

    public BasicRecyclerListAdapter(Context ctx, View.OnClickListener clickListener) {
        mCtx = ctx;
        mClickListener = clickListener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(viewType, parent, false);
        view.setOnClickListener(mClickListener);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.nameView.setText(position + " Position episode: ");
    }


    @Override
    public int getItemCount() {
        return 15;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? R.layout.activity_host : R.layout.activity_host;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_host_name)
        public TextView nameView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(view);
        }
    }
}



