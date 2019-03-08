package com.bookyrself.bookyrself.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class EventsWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new EventsWidgetFactory(this, intent);
    }
}
