package eslamfaisal.srahahmessenger;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import eslamfaisal.srahahmessenger.modules.Message;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM_FROM_ME = 0;
    private final int VIEW_TYPE_ITEM_TO_ME = 2;
    private final int VIEW_TYPE_LOADING = 1;

    private List<Message> mMessagesList;

    public MessageAdapter(List<Message> mMessagesList) {
        this.mMessagesList = mMessagesList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM_FROM_ME) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_from_me, parent, false);
            return new MessageFromMeViewHolder(view);
        } else if (viewType == VIEW_TYPE_ITEM_TO_ME) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_to_me, parent, false);
            return new MessageToMeViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = mMessagesList.get(position);
        if (holder instanceof MessageFromMeViewHolder) {
            MessageFromMeViewHolder fromMeViewHolder = (MessageFromMeViewHolder) holder;
            fromMeViewHolder.messageText.setText(message.getMessage());
        } else if (holder instanceof MessageToMeViewHolder) {
            MessageToMeViewHolder toMeViewHolder = (MessageToMeViewHolder) holder;
            toMeViewHolder.messageText.setText(message.getMessage());
        }
    }

//    @Override
//    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
//
//        Message message = mMessagesList.get(position);
//        holder.messageText.setText(message.getMessage());
//    }

    @Override
    public int getItemViewType(int position) {
        Message message = mMessagesList.get(position);

        String from_or_to = message.getFrom();

        if (from_or_to.equals("toMe")) {
            return VIEW_TYPE_ITEM_TO_ME;
        } else {
            return VIEW_TYPE_ITEM_FROM_ME;
        }
        // return contacts.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }

    public class MessageFromMeViewHolder extends RecyclerView.ViewHolder {

        TextView messageText;
        CircleImageView userImageProfile;

        public MessageFromMeViewHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message_text_view_of_message);
            userImageProfile = itemView.findViewById(R.id.user_image_of_message);

        }
    }

    public class MessageToMeViewHolder extends RecyclerView.ViewHolder {

        TextView messageText;
        CircleImageView userImageProfile;

        public MessageToMeViewHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message_text_view_of_message_tome);
            userImageProfile = itemView.findViewById(R.id.user_image_of_message_tome);

        }
    }
}
