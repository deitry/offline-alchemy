package com.dmsvo.offlinealchemy.classes.views;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dmsvo.offlinealchemy.R;
import com.dmsvo.offlinealchemy.classes.base.Article;
import com.dmsvo.offlinealchemy.classes.base.Comment;
import com.dmsvo.offlinealchemy.classes.base.CompleteArticle;
import com.dmsvo.offlinealchemy.classes.base.Tag;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by DmSVo on 13.01.2018.
 */

public class TagAdapter extends BaseAdapter {

    Context context;
    List<Tag> data;
    public TagAdapter(Context context, List<Tag> tags)
    {
        this.context = context;
        this.data = tags;

        inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    private static LayoutInflater inflater = null;

//    public void addItems(Set<Tag> additional)
//    {
//        data.addAll(additional);
//        notifyDataSetChanged();
//    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItem(int i, Tag tag)
    {
        data.set(i,tag);
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View vi = view;
        if (vi == null)
            vi = inflater.inflate(R.layout.tag_view, null);

        Tag tag = data.get(position);

        TextView name = (TextView) vi.findViewById(R.id.tagName);
        name.setText(tag.getData());
        name.setTypeface(null, Typeface.BOLD);

        TextView count = (TextView) vi.findViewById(R.id.tagCount);
        count.setText("Количество статей: " + tag.getCount());

        return vi;
    }
}
