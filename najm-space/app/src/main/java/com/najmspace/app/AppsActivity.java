package com.najmspace.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppsActivity extends Activity {
    private final ArrayList<ResolveInfo> apps = new ArrayList<ResolveInfo>();

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        final PackageManager pm = getPackageManager();
        Intent q = new Intent(Intent.ACTION_MAIN, null);
        q.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> found = pm.queryIntentActivities(q, 0);
        apps.addAll(found);
        Collections.sort(apps, new Comparator<ResolveInfo>() {
            @Override public int compare(ResolveInfo a, ResolveInfo c) {
                return a.loadLabel(pm).toString().compareToIgnoreCase(c.loadLabel(pm).toString());
            }
        });

        ArrayList<String> labels = new ArrayList<String>();
        for (ResolveInfo r : apps) labels.add(r.loadLabel(pm).toString());

        ListView list = new ListView(this);
        list.setBackgroundColor(Color.rgb(16,20,28));
        list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, labels));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
                ResolveInfo r = apps.get(pos);
                Intent i = pm.getLaunchIntentForPackage(r.activityInfo.packageName);
                if (i != null) startActivity(i);
            }
        });
        setContentView(list);
    }
}
