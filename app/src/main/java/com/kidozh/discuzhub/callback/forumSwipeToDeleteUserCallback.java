package com.kidozh.discuzhub.callback;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.adapter.forumUsersAdapter;

public class forumSwipeToDeleteUserCallback extends ItemTouchHelper.SimpleCallback {
    private String TAG = forumSwipeToDeleteUserCallback.class.getSimpleName();
    private forumUsersAdapter forumInfoAdapter;
    private Drawable icon;
    private final ColorDrawable background;

    private onSwipedInteraction listener;

    public forumSwipeToDeleteUserCallback(forumUsersAdapter forumInfoAdapter){
        super(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.forumInfoAdapter = forumInfoAdapter;
        icon = ContextCompat.getDrawable(forumInfoAdapter.getContext(), R.drawable.vector_drawable_trashbin);
        background = new ColorDrawable(forumInfoAdapter.getContext().getColor(R.color.colorAccent));
        listener = (onSwipedInteraction) forumInfoAdapter.getContext();
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX,
                dY, actionState, isCurrentlyActive);
        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;

        if (dX > 0) { // Swiping to the right
            background.setBounds(itemView.getLeft(), itemView.getTop(),
                    itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
                    itemView.getBottom());

        } else if (dX < 0) { // Swiping to the left
            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else { // view is unSwiped
            background.setBounds(0, 0, 0, 0);
        }
        background.draw(c);
        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();

        if (dX > 0) { // Swiping to the right
            int iconLeft = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
            int iconRight = itemView.getLeft() + iconMargin;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getLeft(), itemView.getTop(),
                    itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
                    itemView.getBottom());
        } else if (dX < 0) { // Swiping to the left
            int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else { // view is unSwiped
            background.setBounds(0, 0, 0, 0);
        }

        background.draw(c);
        icon.draw(c);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        // get forum first
        //Log.d(TAG,"Swipe view "+position + " "+forumInfoAdapter.getUserList()+" Size ");

        onRecyclerViewSwiped(position);



    }

    private void onRecyclerViewSwiped(int position){
        if(listener!=null){
            Log.d(TAG,"Recyclerview swiped "+position);
            listener.onRecyclerViewSwiped(position);
        }
    }


    public interface onSwipedInteraction{
        void onRecyclerViewSwiped(int position);
    }



}
