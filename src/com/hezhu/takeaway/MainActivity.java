package com.hezhu.takeaway;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends CommonActivity {

	private static final String welcome_info = "��ӭʹ����������������һ��רΪ�����ҽ����������������Ӧ�ã�Ӧ���а����˽��������֪�������̼ң�����һ�㼴�ɽ�������֧���ղ��Լ���Ե������̼ҵȹ��ܣ��Ӵ˲��ؿ���������룬���˵������и���ʵ�ù��ܡ�\n��Ӧ�ò���ʱ���¡�\n\n���ٰ�ɫ��Ⱦ��������������";
	private static final String welcome_info_2 = "��Ӧ��Ŀǰ�������ƽ׶Σ���ӭ��ҽ��Լ�������ͽ��鷢�͵�����hezhu.takeaway@gmail.com�����˵����еġ����/���顱��������������������Ա����ǲ��ϵ����ƹ��ܣ���ָ�л��";
	private static final String update_info = 
			"�Ľ� UI��\n" +
			"�޸� ���Ŷ��ͺ�������BUG��";
	private static int OwnListVersionCode = 10;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sp = getSharedPreferences("data",Context.MODE_PRIVATE);
		editor = sp.edit();
		if(sp.getInt("ListVersionCode", 0) < OwnListVersionCode)moveListXML();
		initDefaultList();
		try {
			versionName = getPackageManager().getPackageInfo("com.hezhu.takeaway", 0).versionName;
			versionCode = getPackageManager().getPackageInfo("com.hezhu.takeaway", 0).versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(sp.getBoolean("isFirst", true))
		{
			new AlertDialog.Builder(this).setTitle("��ӭʹ��").setMessage(welcome_info).setPositiveButton("��һҳ", new OnClickListener(){

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					new AlertDialog.Builder(MainActivity.this).setTitle("��ӭʹ��").setMessage(welcome_info_2).setNegativeButton("�ر�", null).show();
				}
				
			}).setNegativeButton("����ָ��", null).show();
			editor.putBoolean("isFirst", false);
		}
		int currentVersionCode;
		try{
			currentVersionCode = sp.getInt("versionCode", versionCode);
		} catch (Exception e) {
			currentVersionCode = (int) sp.getLong("versionCode", versionCode);
		}
		if(currentVersionCode!=versionCode)
		{
			new AlertDialog.Builder(this).setTitle("�°汾�ĸ�������").setMessage(update_info).setPositiveButton("�ر�", null).show();
		}
		editor.putInt("versionCode", versionCode);
		editor.commit();
		initListView();
		
		((Button) findViewById(R.id.FavoriteButton)).setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
				startActivity(intent);
			}
			
		});
		((TextView) findViewById(R.id.Title)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				openOptionsMenu();
			}
		});
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		currentList = list;
		if(isExit){
			finish();
			android.os.Process.killProcess(android.os.Process.myPid());
		}
		if(isChange)initList();
		isChange = false;
		isMainPage = true;
		setThemeColor(sp.getInt("theme", 0));
		super.onStart();
	}
	
	private void moveListXML(){
		try {
			InputStream in = this.getAssets().open("list.xml");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringBuffer buffer = new StringBuffer();
			String line = "";
			while((line = reader.readLine())!=null){
				buffer.append(line);
			}
			updateList(buffer.toString());
		} catch (Exception e) {}
	}
}