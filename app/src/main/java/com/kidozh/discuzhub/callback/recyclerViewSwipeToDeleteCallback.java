package com.kidozh.discuzhub.callback;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class recyclerViewSwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    private String TAG = recyclerViewSwipeToDeleteCallback.class.getSimpleName();
    private RecyclerView.Adapter adapter;
    //private Drawable icon;
    private final ColorDrawable background;
    private final Context context;
    private onRecyclerviewSwiped mListener;

    public recyclerViewSwipeToDeleteCallback(Context context, RecyclerView.Adapter adapter){
        super(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
        this.context = context;
        background = new ColorDrawable();
        if(context instanceof onRecyclerviewSwiped){
            mListener = (onRecyclerviewSwiped) context;
        }
        else {
            throw new RuntimeException(context.toString() + " must implement onRecyclerviewSwiped");
        }


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
        //int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconMargin = itemView.getHeight() / 2;
        //int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight()) / 2;
        //int iconBottom = iconTop + icon.getIntrinsicHeight();
        int iconBottom = iconTop;

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
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        // get forum first
        mListener.onSwiped(position,direction);


    }

    public interface onRecyclerviewSwiped{
        public void onSwiped(int position, int direction);
    }




}
