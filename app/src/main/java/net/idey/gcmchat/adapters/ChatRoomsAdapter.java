package net.idey.gcmchat.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.idey.gcmchat.R;
import net.idey.gcmchat.activities.ChatRoomActivity;
import net.idey.gcmchat.helpers.Tags;
import net.idey.gcmchat.models.ChatRoom;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by yusuf.abdullaev on 7/31/2016.
 */
public class ChatRoomsAdapter extends RecyclerView.Adapter<ChatRoomsAdapter.ViewHolder> implements Tags{

    private Context mContext;
    private ArrayList<ChatRoom> chatRooms;
    private static String today;
    private LinearLayout.LayoutParams layoutParams;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, message, time, count;
        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView)itemView.findViewById(R.id.text_view_row_name);
            message = (TextView)itemView.findViewById(R.id.text_view_row_message);
            time = (TextView)itemView.findViewById(R.id.text_view_row_time);
            count = (TextView)itemView.findViewById(R.id.text_view_row_unread_count);
        }
    }

    public ChatRoomsAdapter(Context context, ArrayList<ChatRoom> chatRooms) {
        mContext = context;
        this.chatRooms = chatRooms;


        today = String.valueOf(Calendar.DAY_OF_MONTH);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_room_list_row, parent, false);
        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.setLayoutParams(layoutParams);
        final ChatRoom chatRoom = chatRooms.get(position);
        holder.name.setText(chatRoom.getName());
        holder.message.setText(chatRoom.getLastMessage());
        if (chatRoom.getUnreadCount() > 0){
            holder.count.setVisibility(View.VISIBLE);
            holder.count.setText(String.valueOf(chatRoom.getUnreadCount()));
        }else {
            holder.count.setVisibility(View.GONE);
        }

        holder.time.setText(getTimestamp(chatRoom.getTimestamp()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ChatRoomActivity.class);
                intent.putExtra(CHAT_ROOM_ID, chatRoom.getId());
                intent.putExtra(NAME, chatRoom.getName());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    private String getTimestamp(String dateString){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = "";
        today = today.length() < 2 ? "0" + today : today;

        try {
            Date date = format.parse(dateString);
            SimpleDateFormat todayFormat = new SimpleDateFormat("dd");
            String dateToday = todayFormat.format(date);
            format = dateToday.equals(today) ? new SimpleDateFormat("hh:mm a") : new SimpleDateFormat("dd LLL, hh:mm a");
            String date1 = format.format(date);
            timestamp = date1.toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }

}
