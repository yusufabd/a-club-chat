package net.idey.gcmchat.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.idey.gcmchat.R;
import net.idey.gcmchat.models.Message;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by yusuf.abdullaev on 8/2/2016.
 */
public class ChatRoomThreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private String userId;
    private int SELF = 100;
    private static String today;

    private Context mContext;
    private ArrayList<Message> messageArrayList;

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView message, timestamp;

        public ViewHolder(View itemView) {
            super(itemView);
            message = (TextView)itemView.findViewById(R.id.text_view_message);
            timestamp = (TextView)itemView.findViewById(R.id.text_view_timestamp);
        }
    }

    public ChatRoomThreadAdapter(String userId, Context mContext, ArrayList<Message> messageArrayList) {
        this.userId = userId;
        this.mContext = mContext;
        this.messageArrayList = messageArrayList;

        Calendar calendar = Calendar.getInstance();
        today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == SELF){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_self, parent, false);
        }else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_other, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageArrayList.get(position);
        if (message.getUser().getId().equals(userId)){
            return SELF;
        }

        return position;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = messageArrayList.get(position);
        ((ViewHolder)holder).message.setText(message.getMessage());

        String timestamp = message.getCreatedAt();
        if (message.getUser().getName() != null){
            timestamp = message.getUser().getName() + ", " + timestamp;
        }
        ((ViewHolder)holder).timestamp.setText(timestamp);
    }

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }

    public static String getTimeStamp(String dateString){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = "";

        try {
            Date date = format.parse(dateString);
            SimpleDateFormat todayFormat = new SimpleDateFormat("dd");
            String todayDate = todayFormat.format(date);
            format = todayDate.equals(today) ? new SimpleDateFormat("hh:mm a") : new SimpleDateFormat("dd LLL, hh:mm a");
            timestamp = format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timestamp;
    }


}
