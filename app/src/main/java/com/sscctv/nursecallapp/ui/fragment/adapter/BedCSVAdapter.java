package com.sscctv.nursecallapp.ui.fragment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sscctv.nursecallapp.R;

import java.io.File;
import java.util.List;


public class BedCSVAdapter extends BaseAdapter {
	
	Context context;
	private int resource;
	private List<File> files;
	
	static class ViewHolder{
		TextView fileName;
		TextView filePath;

		ViewHolder() {

		}
	}


	public void removeItem(int position) {
		files.remove(position);
	}

	public void allClear() {
		files.clear();
	}
	
	public BedCSVAdapter(Context context, int resource, List<File> files){
		this.files=files;
		this.context=context;
		this.resource=resource;
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
		
		ViewHolder viewHolder;
		
		if (convertView==null){
			convertView= LayoutInflater.from(context).inflate(resource, parent, false);
			
			viewHolder = new ViewHolder();
			viewHolder.fileName= convertView.findViewById(R.id.fileName);
			viewHolder.filePath= convertView.findViewById(R.id.filePath);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder=(ViewHolder) convertView.getTag();
		}
		
		File file = (File) getItem(position);

		viewHolder.fileName.setText(file.getName());
		viewHolder.filePath.setText(file.getPath());
		
		return convertView;
	}

	@Override
	public int getCount() {
		return files.size();
	}

	@Override
	public Object getItem(int position) {
		return files.get(position);
	}

	public String getName(int position) {
		return files.get(position).getName();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
	
