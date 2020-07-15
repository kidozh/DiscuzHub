package com.kidozh.discuzhub.callback;

import android.content.Context;
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

import org.jetbrains.annotations.NotNull;

public class RecyclerViewItemTouchCallback extends ItemTouchHelper.Callback {
    private String TAG = RecyclerViewItemTouchCallback.class.getSimpleName();
    private Context context;
    private Drawable icon;
    private final ColorDrawable background;

    private onInteraction listener;

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    public RecyclerViewItemTouchCallback(Context context){
        this.context = context;
        icon = ContextCompat.getDrawable(context, R.drawable.vector_drawable_trashbin);
        background = new ColorDrawable(context.getColor(R.color.colorWarn));
        if(context instanceof onInteraction){

            listener = (onInteraction) context;
        }
        else {
            Log.e(TAG,"Context "+context+" doesn't implement "+onInteraction.class.getSimpleName());
        }

    }

//    @Override
//    public void onChildDraw(@NotNull Canvas c, @NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
//        super.onChildDraw(c, recyclerView, viewHolder, dX,
//                dY, actionState, isCurrentlyActive);
//        View itemView = viewHolder.itemView;
//        int backgroundCornerOffset = 20;
//
//        if (dX > 0) { // Swiping to the right
//            background.setBounds(itemView.getLeft(), itemView.getTop(),
//                    itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
//                    itemView.getBottom());
//
//        } else if (dX < 0) { // Swiping to the left
//            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
//                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
//        } else { // view is unSwiped
//            background.setBounds(0, 0, 0, 0);
//        }
//        background.draw(c);
//        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
//        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
//        int iconBottom = iconTop + icon.getIntrinsicHeight();
//
//        if (dX > 0) { // Swiping to the right
//            int iconLeft = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
//            int iconRight = itemView.getLeft() + iconMargin;
//            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
//
//            background.setBounds(itemView.getLeft(), itemView.getTop(),
//                    itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
//                    itemView.getBottom());
//        } else if (dX < 0) { // Swiping to the left
//            int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
//            int iconRight = itemView.getRight() - iconMargin;
//            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
//
//            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
//                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
//        } else { // view is unSwiped
//            background.setBounds(0, 0, 0, 0);
//        }
//
//        background.draw(c);
//        icon.draw(c);
//    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        if(listener !=null){
            listener.onRecyclerViewMoved(viewHolder.getAdapterPosition(),target.getAdapterPosition());
        }
        else {
            //return false;
        }

        return true;

    }

    @Override
    public boolean canDropOver(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder current, @NonNull RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        // get forum first
        if(listener !=null){
            listener.onRecyclerViewSwiped(position,direction);
        }



    }

    public interface onInteraction{
        void onRecyclerViewSwiped(int position, int direction);
        void onRecyclerViewMoved(int fromPosition, int toPosition);
    }




}
