package com.bookyrself.bookyrself.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bookyrself.bookyrself.R;
import com.bookyrself.bookyrself.utils.TinyDB;

import java.util.ArrayList;

public class EventsWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private ArrayList<String> eventDetailList = new ArrayList<>();
    private TinyDB tinyDB;


    EventsWidgetFactory(Context context, Intent intent) {
        this.context = context;
        this.tinyDB = new TinyDB(context);
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        initData();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return eventDetailList.size();
    }

    // Return view item
    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.item_events_widget);
        view.setTextViewText(R.id.widget_events_item_eventname, eventDetailList.get(position));

        return view;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private void initData() {
        eventDetailList = tinyDB.getListString("attendingEventsString");
    }
}
