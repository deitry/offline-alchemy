package com.dmsvo.offlinealchemy.classes.runnables;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dmsvo.offlinealchemy.classes.base.Article;
import com.dmsvo.offlinealchemy.classes.base.CompleteArticle;
import com.dmsvo.offlinealchemy.classes.activities.ArticleViewActivity;
import com.dmsvo.offlinealchemy.classes.activities.Main2Activity;
import com.dmsvo.offlinealchemy.R;
import com.dmsvo.offlinealchemy.classes.views.ArticleAdapter;

import java.util.List;

/**
 * Created by DmSVo on 08.01.2018.
 */

public class UpdateListView implements Runnable {

    private Main2Activity activity;
    private List<CompleteArticle> articles;
    private Menu contextMenu;

    public UpdateListView(@NonNull Main2Activity activity,
                          @NonNull List<CompleteArticle> articles
    ) {
        this.activity = activity;
        this.articles = articles;
    }

    @Override
    public void run() {
        try {

            final ListView articlesView = activity.findViewById(R.id.articleslist);
            ArticleAdapter adapter = (ArticleAdapter) articlesView.getAdapter();
            if (adapter == null) {
                adapter = new ArticleAdapter(
                        activity,
                        articles);             //android.R.layout.simple_list_item_1,
                articlesView.setAdapter(adapter);
                articlesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        ArticleAdapter adapt = (ArticleAdapter) adapterView.getAdapter();
                        if (adapt != null && adapt.getSelection() >= 0) {
                            // если выделен какой-либо элемент, то по клику
                            // мы не открываем новый элемент, а переносим выделение
                            adapt.setSelection(i);
                            return;
                        }

                        Intent intent = new Intent(activity, ArticleViewActivity.class);

                        CompleteArticle cart = (CompleteArticle) adapterView.getItemAtPosition(i);
                        if (cart != null) {
                            cart.article.setWasRead(1);

                            if (cart.comments == null)
                            {
                                // загружаем комментарии из бд
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ProgressBar pbar = activity.findViewById(R.id.progressBar);
                                                pbar.setVisibility(View.VISIBLE);
                                            }
                                        });

                                        cart.comments = activity.getLoader().GetComments(cart.article.getId());
                                        intent.putExtra(activity.OPEN_ARTICLE, cart);

                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ProgressBar pbar = activity.findViewById(R.id.progressBar);
                                                pbar.setVisibility(View.INVISIBLE);
                                            }
                                        });

                                        activity.startActivityForResult(intent,1);
                                    }
                                }).start();
                            } else {
                                intent.putExtra(activity.OPEN_ARTICLE, cart);
                                activity.startActivityForResult(intent,1);
                            }
                        }
                    }
                });
            } else {
                // не просто добавлять, а в конкретные места?
                adapter.addItems(articles);
            }
            final ArticleAdapter constAdapter = adapter;
            ActionMode.Callback modeCallBack = new ActionMode.Callback() {

                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    if (menu != null) {
                        int selId = constAdapter.getSelection();
                        if (selId >= 0) {
                            CompleteArticle cart = (CompleteArticle) constAdapter.getItem(selId);
                            int fav = cart.article.getFavorite();
                            int read = cart.article.getWasRead();

                            MenuItem item = menu.findItem(R.id.action_set_favorite);
                            if (fav > 0) {
                                item.setIcon(R.drawable.ic_action_unfav);
                            } else {
                                item.setIcon(R.drawable.ic_action_favorite);
                            }

                            item = menu.findItem(R.id.action_set_read);
                            if (read > 0) {
                                item.setIcon(R.drawable.ic_action_unread);
                            } else {
                                item.setIcon(R.drawable.ic_action_read);
                            }
                        }
                        return true;
                    }

                    return false;
                }

                public void onDestroyActionMode(ActionMode mode) {
                    mode = null;
                    constAdapter.setSelection(-1);
                }

                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mode.setTitle("");
                    mode.getMenuInflater().inflate(R.menu.article_context_menu, menu);

                    UpdateListView.this.contextMenu = menu;


                    return true;
                }

                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    int id = item.getItemId();
                    switch (id) {
                        case R.id.action_set_favorite: {
                            try {
                                int selId = constAdapter.getSelection();
                                CompleteArticle cart = (CompleteArticle) constAdapter.getItem(selId);
                                int fav = cart.article.getFavorite() == 1 ? 0 : 1;
                                cart.article.setFavorite(fav);
                                constAdapter.setItem(selId, cart);
                                if (fav > 0) {
                                    item.setIcon(R.drawable.ic_action_unfav);
                                } else {
                                    item.setIcon(R.drawable.ic_action_favorite);
                                }
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        activity.getLoader().SaveInDb(cart);
                                    }
                                }).start();
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                            break;
                        }
                        case R.id.action_set_read: {
                            try {
                                int selId = constAdapter.getSelection();
                                CompleteArticle cart = (CompleteArticle) constAdapter.getItem(selId);
                                int read = cart.article.getWasRead() == 1 ? 0 : 1;
                                cart.article.setWasRead(read);
                                constAdapter.setItem(selId, cart);
                                if (read > 0) {
                                    item.setIcon(R.drawable.ic_action_unread);
                                } else {
                                    item.setIcon(R.drawable.ic_action_read);
                                }
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        activity.getLoader().SaveInDb(cart);
                                    }
                                }).start();

                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                            break;
                        }
                        case R.id.action_delete_article:
                            Toast.makeText(activity,
                                    "Пока что не умею удалять :(",
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }

                    return false;
                }
            };

            articlesView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick (AdapterView parent, View view, int position, long id) {
                    System.out.println("Long click");
                    constAdapter.setSelection(position);
                    final ActionMode actionMode = activity.startActionMode(modeCallBack);
                    constAdapter.setOnSelectionChangedListener(new ArticleAdapter.OnSelectionChangedListener() {
                        @Override
                        public void OnSelectionChanged() {
                            modeCallBack.onPrepareActionMode(actionMode, contextMenu);
                        }
                    });
                    view.setSelected(true);
                    parent.setSelection(position);
                    return true;
                }
            });
//            articlesView.setOnItemLongClickListener((parent, view, position, id) -> {
//
//                return true;
//            });

            ProgressBar pbar = activity.findViewById(R.id.progressBar);
            pbar.setVisibility(View.INVISIBLE);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
