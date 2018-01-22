package com.dmsvo.offlinealchemy.classes.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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
import com.dmsvo.offlinealchemy.classes.db.Converters;

import java.util.List;

/**
 * Created by DmSVo on 08.01.2018.
 */

public class ArticleAdapter extends BaseAdapter {

    Activity context;
    List<CompleteArticle> data;
    private static LayoutInflater inflater = null;

    public ArticleAdapter(Activity context, List<CompleteArticle> articles)
    {
        this.context = context;
        this.data = articles;
        inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItems(List<CompleteArticle> additional)
    {
        data.addAll(additional);
        notifyDataSetChanged();
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

    public void setItem(int i, CompleteArticle cart)
    {
        data.set(i,cart);
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View vi = view;
        if (vi == null)
            vi = inflater.inflate(R.layout.article_view, null);

        CompleteArticle cart = data.get(position);
        Article article = cart.article;

        TextView header = (TextView) vi.findViewById(R.id.header);
        header.setText(article.getName());

        boolean wasRead = article.getWasRead() == 1;
        boolean loaded = article.getLoaded() == 1;

        if (!wasRead && !loaded) {
            header.setTypeface(null, Typeface.BOLD_ITALIC);
        } else if (!wasRead) {
            header.setTypeface(null, Typeface.BOLD);
        } else if (!loaded) {
            header.setTypeface(null, Typeface.ITALIC);
        } else {
            header.setTypeface(null, Typeface.NORMAL);
        }

        boolean fav = article.getFavorite() == 1;
        if (fav) {
            header.setBackgroundColor(context.getResources().getColor(R.color.accentedDark));
        } else {
            header.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        }

        TextView date = (TextView) vi.findViewById(R.id.article_date);
        date.setText(article.getDate().toString());

        TextView commentsView = (TextView) vi.findViewById(R.id.article_comments);
        if (cart.comments != null) {
            commentsView.setText("Комментариев: " + cart.comments.size());
        } else {
            commentsView.setText("Комментариев ~ " + cart.article.getCommentCount());
        }
        // TODO: сделать в виде списка кнопок?
        TextView tagsView = vi.findViewById(R.id.article_tags);
        List<String> tags = cart.article.getTags();
        String combined = "";
        for (String tag : tags)
        {
            combined += tag + " ";
        }
        if (!combined.equals("")) {
            tagsView.setText(combined);
        } else {
            tagsView.setVisibility(View.GONE);
        }
        return vi;
    }
}
