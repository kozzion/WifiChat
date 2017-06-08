package nl.everlutions.wifichat.adapters;


import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import nl.everlutions.wifichat.R;


public class HostRecyclerListAdapter extends RecyclerView.Adapter<HostRecyclerListAdapter.ViewHolder> {

    private final Context mCtx;
    private final View.OnClickListener mClickListener;
    private ArrayList<NsdServiceInfo> mHostItems = new ArrayList<>();

    public HostRecyclerListAdapter(Context ctx, View.OnClickListener clickListener) {
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
        holder.nameView.setText("ServiceName: " + mHostItems.get(position).getServiceName());
        holder.ipView.setText("IP-Address: " + mHostItems.get(position).getHost().toString());
        holder.portView.setText("Port: " + mHostItems.get(position).getPort());
    }


    @Override
    public int getItemCount() {
        return mHostItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? R.layout.list_item_host : R.layout.list_item_host;
    }

    public void addHostItem(NsdServiceInfo hostItem) {
        mHostItems.add(hostItem);
        notifyDataSetChanged();
    }

    public NsdServiceInfo getHostItem(int position) {
        return mHostItems.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        //        @BindView(R.id.item_host_name)
        public TextView nameView;
        public TextView ipView;
        public TextView portView;

        public ViewHolder(View view) {
            super(view);
//            ButterKnife.bind(view);
            nameView = (TextView) view.findViewById(R.id.item_host_name);
            ipView = (TextView) view.findViewById(R.id.item_host_ip);
            portView = (TextView) view.findViewById(R.id.item_host_port);
        }
    }
}



