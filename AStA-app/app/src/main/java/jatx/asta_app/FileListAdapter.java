package jatx.asta_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by jatx on 20.09.17.
 */

public class FileListAdapter extends BaseAdapter {
    private MainActivity mActivity;
    private List<File> mFileList;
    private File mCurrentDir;

    public FileListAdapter(MainActivity activity, File[] files, File currentDir) {
        mActivity = activity;
        mCurrentDir = currentDir;
        mFileList = new ArrayList<>();
        mFileList.add(new File(currentDir, ".."));

        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                if (f1.isDirectory() && f2.isFile()) {
                    return -1;
                } else if (f1.isFile() && f2.isDirectory()) {
                    return +1;
                } else {
                    return f1.getAbsolutePath().compareTo(f2.getAbsolutePath());
                }
            }
        });

        for (File file: files) {
            mFileList.add(file);
        }
    }

    @Override
    public int getCount() {
        return mFileList.size();
    }

    @Override
    public File getItem(int i) {
        return mFileList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) mActivity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_file_list, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.item_file_list_img);
            viewHolder.textView = (TextView) convertView.findViewById(R.id.item_file_list_text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        File file = getItem(i);

        viewHolder.textView.setText(file.getName());
        boolean isFolder = file.isDirectory();
        viewHolder.imageView.setImageResource(isFolder ? R.drawable.ic_folder : R.drawable.ic_file);

        return convertView;
    }

    private static class ViewHolder {
        public ImageView imageView;
        public TextView textView;
    }
}
