package com.dmsvo.offlinealchemy.classes.views;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dmsvo.offlinealchemy.R;
import com.dmsvo.offlinealchemy.classes.activities.ArticleViewActivity;
import com.dmsvo.offlinealchemy.classes.base.Comment;
import com.dmsvo.offlinealchemy.classes.db.AppDb;
import com.dmsvo.offlinealchemy.classes.loader.Loader;

import java.util.Date;
import java.util.List;

/**
 * Created by DmSVo on 08.01.2018.
 */

public class CommentAdapter extends BaseAdapter {

    Context context;
    List<Comment> data;
    private static LayoutInflater inflater = null;

    public CommentAdapter(Context context, List<Comment> comments)
    {
        this.context = context;
        this.data = comments;

        inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

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

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View vi = view;
        if (vi == null)
            vi = inflater.inflate(R.layout.comment_view, null);

        Comment comt = data.get(position);

        try {
            String author = comt.getAuthor();
            TextView authorView = vi.findViewById(R.id.comment_author);
            authorView.setText((position + 1) + ": " + author);
            if (author.equals("evo_lutio")) {
                authorView.setBackgroundColor(
                        context.getResources().getColor(
                                R.color.accent_material_light
                        )
                );
            }

            TextView dateView = vi.findViewById(R.id.comment_date);
            Date date = comt.getDate();
            if (date != null)
                dateView.setText(date.toString());

            TextView bodyView = vi.findViewById(R.id.comment_body);
            String body = comt.getBody();
            if (body != null) {
                bodyView.setText(Html.fromHtml(body));
                bodyView.setMovementMethod(LinkMovementMethod.getInstance());
            }

            int left = comt.getLevel()
                    * (int) context.getResources().getDimension(R.dimen.comment_indent);
            if (left > context.getResources().getDimension(R.dimen.comment_max_indent))
                left = (int)context.getResources().getDimension(R.dimen.comment_max_indent);

            LinearLayout layout = vi.findViewById(R.id.comment_container);
            layout.setPadding(left, 0, 0, 0);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return vi;
    }
}
