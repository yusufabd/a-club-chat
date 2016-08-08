package net.idey.gcmchat.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.idey.gcmchat.R;
import net.idey.gcmchat.models.User;

import java.util.ArrayList;

/**
 * Created by yusuf.abdullaev on 8/8/2016.
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder>{

    private Context mContext;
    private ArrayList<User> mUserArrayList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, email;
        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView)itemView.findViewById(R.id.text_view_row_user_name);
            email = (TextView)itemView.findViewById(R.id.text_view_row_user_email);
        }
    }

    public UsersAdapter(Context context, ArrayList<User> userArrayList) {
        mContext = context;
        mUserArrayList = userArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = mUserArrayList.get(position);
        holder.name.setText(user.getName());
        holder.email.setText(user.getEmail());
    }

    @Override
    public int getItemCount() {
        return mUserArrayList.size();
    }


}
