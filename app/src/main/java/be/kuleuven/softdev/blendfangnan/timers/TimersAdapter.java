package be.kuleuven.softdev.blendfangnan.timers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public class TimersAdapter extends RecyclerView.Adapter<TimersAdapter.MyViewHolder> implements ItemTouchHelperAdapter {

    private List<MyTimer> timersList;
    private int timerPosition;
    private String receivedLabel;
    private int receivedTime;
    private MyTimer mRecentlyDeletedItem;
    int mRecentlyDeletedItemPosition;

    TimersAdapter(List<MyTimer> timersList) {
        this.timersList = timersList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView currentTextView;
        TextView label;

        MyViewHolder(View view) {
            super(view);
            currentTextView = (TextView) view.findViewById(R.id.currentTextView);
            label = (TextView) view.findViewById(R.id.label);

            view.setOnClickListener(this);
        }

        public void onClick(View view) {
            MyTimer myTimer = timersList.get(getAdapterPosition());
            customDialog("Timer " + String.valueOf(getAdapterPosition() + 1) + ": " + myTimer.getLabel());
            timerPosition = getAdapterPosition();
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.timers_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        MyTimer myTimer = timersList.get(holder.getLayoutPosition());

        holder.currentTextView.setText(String.valueOf(myTimer.showSecondsLeftProperly()));
        holder.label.setText(myTimer.getLabel());
    }

    @Override
    public int getItemCount() {
        return timersList.size();
    }

    public int getTimerPosition() {
        return timerPosition;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++)
                Collections.swap(timersList, i, i+1);
        }
        else {
            for (int i = fromPosition; i > toPosition; i--)
                Collections.swap(timersList, i, i - 1);
        }
        timersList.get(fromPosition).setActive(false);
        timersList.get(toPosition).setActive(false);
        notifyItemMoved(toPosition, fromPosition);

    }

    @Override
    public void onItemDismiss(int position) {
        deleteItem(position);
    }

    private void deleteItem(int position) {
        mRecentlyDeletedItem = timersList.get(position);
        mRecentlyDeletedItemPosition = position;
        timersList.remove(position);
        notifyItemRemoved(position);
        showUndoSnackbar();
    }

    private void showUndoSnackbar() {
        View view = MainActivity.mainActivity.findViewById(R.id.recycler_view);
        Snackbar snackbar = Snackbar.make(view, "You just deleted a timer.",
                Snackbar.LENGTH_LONG);
        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoDelete();
            }
        });
        snackbar.show();
    }

    private void undoDelete() {
        timersList.add(mRecentlyDeletedItemPosition,
                mRecentlyDeletedItem);
        notifyItemInserted(mRecentlyDeletedItemPosition);
    }

    private void customDialog(String title) {

        LayoutInflater factory = LayoutInflater.from(MainActivity.mainActivity);
        View textEntryView = factory.inflate(R.layout.alert_dialog, null);

        LinearLayout layout1 = (LinearLayout) textEntryView.findViewById(R.id.layout1);
        LinearLayout layout2 = (LinearLayout) textEntryView.findViewById(R.id.layout2);

        final EditText editText1 = (EditText) textEntryView.findViewById(R.id.editText1);

        final NumberPicker numberPicker1 = (NumberPicker) textEntryView.findViewById(R.id.numberPicker1);
        numberPicker1.setMinValue(0);
        numberPicker1.setMaxValue(59);

        final NumberPicker numberPicker2 = (NumberPicker) textEntryView.findViewById(R.id.numberPicker2);
        numberPicker2.setMinValue(0);
        numberPicker2.setMaxValue(59);

        layout1.setMinimumWidth(layout2.getWidth());

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.mainActivity);
        builderSingle.setTitle(title);
        builderSingle.setMessage("Enter your label and time:");

        builderSingle.setView(textEntryView);

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builderSingle.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!(numberPicker1.getValue() == 0 && numberPicker2.getValue() == 0)) {
                    receivedTime = ((numberPicker1.getValue() * 60) + (numberPicker2.getValue()));
                    timersList.get(timerPosition).setSeconds(receivedTime);
                    timersList.get(timerPosition).setSecondsLeft(receivedTime);
                    timersList.get(timerPosition).setInitiated(false);
                    timersList.get(timerPosition).setActive(false);
                }
                if (!editText1.getText().toString().equals("")) {
                    receivedLabel = editText1.getText().toString();
                    timersList.get(timerPosition).setLabel(receivedLabel);
                }
                notifyDataSetChanged();
            }
        });

        builderSingle.setNeutralButton("Start Over", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                timersList.get(timerPosition).setSecondsLeft(timersList.get(timerPosition).getSeconds());
                notifyDataSetChanged();
            }
        });

        builderSingle.show();
    }
}