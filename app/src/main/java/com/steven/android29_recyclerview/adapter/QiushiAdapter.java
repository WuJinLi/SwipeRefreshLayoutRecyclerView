package com.steven.android29_recyclerview.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.steven.android29_recyclerview.R;
import com.steven.android29_recyclerview.model.QiushiModel;

import java.util.List;

/**
 * Created by StevenWang on 16/6/5.
 */
public class QiushiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context = null;
    private List<QiushiModel.ItemsEntity> list = null;
    private LayoutInflater inflater = null;
    private static final int STATE1 = 0, STATE2 = 1;

    public QiushiAdapter(Context context, List<QiushiModel.ItemsEntity> list) {
        this.context = context;
        this.list = list;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case STATE1:
                view = inflater.inflate(R.layout.item_listview_main1, parent, false);
                return new ViewHolder1(view);
            case STATE2:
                view = inflater.inflate(R.layout.item_listview_main2, parent, false);
                return new ViewHolder2(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder2) {
            ViewHolder2 viewHolder2 = (ViewHolder2) holder;
            viewHolder2.textView_item_content.setText(list.get(position).getContent());
            if (list.get(position).getUser() != null) {
                viewHolder2.textView_item_login.setText(list.get(position).getUser().getLogin
                        ());
            }
            viewHolder2.textView_item_commentscount.setText(list.get(position)
                    .getComments_count() + "");
        } else {
            ViewHolder1 viewHolder1 = (ViewHolder1) holder;
            viewHolder1.textView_item_content.setText(list.get(position).getContent());
            if (list.get(position).getUser() != null) {
                viewHolder1.textView_item_login.setText(list.get(position).getUser().getLogin
                        ());
            }
            viewHolder1.textView_item_commentscount.setText(list.get(position)
                    .getComments_count() + "");

            //加载图片Picasso
            String imageUrl = getImageUrl(list.get(position).getImage() + "");
            if (imageUrl != null) {
                // 使用Picasso框架加载图片
                Picasso.with(context).load(imageUrl)
                        .noFade()
                        /*.resize()
                        .resizeDimen()*/
                        .placeholder(android.R.drawable.ic_search_category_default)
                        .error(android.R.drawable.stat_notify_error)
                        .into(viewHolder1.imageView_item_show);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public int getItemViewType(int position) {
        String imageUrl = getImageUrl(list.get(position).getImage() + "");
        return (imageUrl == null) ? STATE2 : STATE1;
    }

    public void reloadListView(List<QiushiModel.ItemsEntity> _list, boolean isClear) {
        if (isClear) {
            list.clear();
        }
        list.addAll(_list);
        notifyDataSetChanged();
    }


    class ViewHolder1 extends RecyclerView.ViewHolder {
        private ImageView imageView_item_show;
        private TextView textView_item_content;
        private TextView textView_item_login;
        private TextView textView_item_commentscount;

        public ViewHolder1(View convertView) {
            super(convertView);
            imageView_item_show = ((ImageView) convertView.findViewById(R.id.imageView_item_show));
            textView_item_content = ((TextView) convertView.findViewById(R.id
                    .textView_item_content));
            textView_item_login = ((TextView) convertView.findViewById(R.id.textView_item_login));
            textView_item_commentscount = ((TextView) convertView.findViewById(R.id
                    .textView_item_commentscount));
        }
    }

    class ViewHolder2 extends RecyclerView.ViewHolder {
        private TextView textView_item_content;
        private TextView textView_item_login;
        private TextView textView_item_commentscount;

        public ViewHolder2(View convertView) {
            super(convertView);
            textView_item_content = ((TextView) convertView.findViewById(R.id
                    .textView_item_content));
            textView_item_login = ((TextView) convertView.findViewById(R.id.textView_item_login));
            textView_item_commentscount = ((TextView) convertView.findViewById(R.id
                    .textView_item_commentscount));
        }
    }

    // 根据图片的名称拼凑图片的网络访问地址
    private String getImageUrl(String imageName) {
        String urlFirst = "", urlSecond = "";
        if (imageName.indexOf('.') > 0) {
            StringBuilder sb = new StringBuilder();
            if (imageName.indexOf("app") == 0) {
                urlSecond = imageName.substring(3, imageName.indexOf('.'));
                switch (urlSecond.length()) {
                    case 8:
                        urlFirst = imageName.substring(3, 7);
                        break;
                    case 9:
                        urlFirst = imageName.substring(3, 8);
                        break;
                    case 10:
                        urlFirst = imageName.substring(3, 9);
                        break;
                }
            } else {
                urlSecond = imageName.substring(0, imageName.indexOf('.'));
                urlFirst = imageName.substring(0, 6);
            }

            sb.append("http://pic.qiushibaike.com/system/pictures/");
            sb.append(urlFirst);
            sb.append("/");
            sb.append(urlSecond);
            sb.append("/");
            sb.append("small/");
            sb.append(imageName);
            return sb.toString();
        } else {
            return null;
        }
    }
}
