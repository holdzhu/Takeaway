package com.hezhu.takeaway;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

@SuppressLint("HandlerLeak")
public class CommonActivity extends Activity {
	public static boolean isMainPage;
	public static boolean isChange = false;
	public static boolean isExit = false;
	private int lastPosition = -1;
	public static List<ItemHolder> list = new ArrayList<ItemHolder>();
	public static List<ItemHolder> defaultList = new ArrayList<ItemHolder>();
	public static List<ItemHolder> favoriteList = new ArrayList<ItemHolder>();
	public static List<ItemHolder> currentList;
	public static String versionName;
	public static int versionCode;
	public static String ListVersionName;
	public static int ListVersionCode;
	public static int newVersionCode;
	public static String newVersionName;
	public static String newPathName;
	public static int newListVersionCode;
	public static String newListVersionName;
	public static String newListString;
	public static int[][] themeColor = {
		{0xff222222,0xffffffff},
		{0xff0064b6,0xddffffff},
		{0xffee6f90,0xddffffff},
		{0xff87ac06,0xddffffff},
		{0xff993399,0xddffffff},
		{0xffff3333,0xddffffff},
		{0xffff8033,0xddffffff},
		{0xffffda00,0xddffffff},
		{0xff4b5057,0xddffffff},
		{0xff5e4737,0xddffffff}
	};
	public static final String[] themename = {
		"�ڰ���","�����","���ҷ�","�����","������","ӣ�Һ�","�ȴ���","���ʻ�","��ҹ��","Ħ����"
	};
	public static SharedPreferences sp;
	public static SharedPreferences.Editor editor;
	public DatabaseHelper sqlite = new DatabaseHelper(this);
	private static final String VersionURL = "https://hezhu-hezhu.rhcloud.com/takeaway/xml/version.php";
	private static final String ListURL = "https://hezhu-hezhu.rhcloud.com/takeaway/xml/list.php";
	private static final String DownloadURL = "https://hezhu-hezhu.rhcloud.com/takeaway/files/";
	private static final String FeedbackURL = "https://hezhu-hezhu.rhcloud.com/takeaway/feedback_b.php";
	ProgressDialog updateDialog;
	//��������
	OnClickListener updateListListener = new OnClickListener(){

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			if(isConnected()){
				showProgressDialog("���ڸ�������","���Եȡ�");
				new Thread(new Runnable(){
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							URL url = new URL(ListURL);
			                URLConnection conn = url.openConnection();
			                conn.setConnectTimeout(10000);
			                conn.setReadTimeout(10000);
			                InputStream in = conn.getInputStream();
			    			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			    			StringBuffer buffer = new StringBuffer();
			    			String line = "";
			    			while((line = reader.readLine())!=null){
			    				buffer.append(line);
			    			}
			    			newListString = buffer.toString();
			                handle.sendEmptyMessage(2);
						} catch (Exception e) {
							// TODO Auto-generated catch block
			                handle.sendEmptyMessage(-1);
							e.printStackTrace();
						}
					}
					
				}).start();
			} else Toast.makeText(getApplication(), "������������", Toast.LENGTH_SHORT).show();
		}
		
	};
	//����Ҫ�õ��Ķ���
	Handler handle = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(!updateDialog.isShowing())return;
			updateDialog.cancel();
			switch(msg.what){
			case -1:
				Toast.makeText(getApplication(), "�������", Toast.LENGTH_SHORT).show();
				break;
			case 0:
				if(newVersionCode>versionCode){
					new AlertDialog.Builder(CommonActivity.this)
					.setTitle("�����°汾")
					.setMessage("����Ӧ�����°汾("+newVersionName+")���Ƿ���£�")
					.setPositiveButton("��", new OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Intent intent = new Intent(Intent.ACTION_VIEW);  
							intent.setData(Uri.parse(DownloadURL + newPathName));  
							startActivity(intent);  
						}
						
					})
					.setNegativeButton("��",	 null)
					.show();
				} else {
					if(newListVersionCode>ListVersionCode){
						new AlertDialog.Builder(CommonActivity.this)
						.setTitle("�����°汾")
						.setMessage("���ֵ�����Ϣ���°汾("+newListVersionName+")���Ƿ���£�")
						.setPositiveButton("��", updateListListener)
						.setNegativeButton("��",	 null)
						.show();
					} else Toast.makeText(getApplication(), "����Ӧ�ü�������Ϣ��Ϊ���°汾���������", Toast.LENGTH_SHORT).show();
				}
				break;
			case 1:
				Toast.makeText(getApplication(), "���ͳɹ�����л��������ͽ���", Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Toast.makeText(getApplication(), "���³ɹ�", Toast.LENGTH_SHORT).show();
				updateList(newListString);
    			initDefaultList();
    			if(!isMainPage)initFavorite();
				break;
			case 3:
				if(newListVersionCode>ListVersionCode)updateListListener.onClick(updateDialog, 0);
				else Toast.makeText(getApplication(), "���ĵ�����ϢΪ���°汾���������", Toast.LENGTH_SHORT).show();
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	//��Menu����
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	//��������
	public void setThemeColor(int index){
		findViewById(R.id.MainLayout).setBackgroundColor(themeColor[index][0]);
		findViewById(R.id.listView).setBackgroundColor(themeColor[index][1]);
		editor.putInt("theme", index).commit();
	}
	//����Menu����¼�
	@SuppressWarnings("deprecation")
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = LayoutInflater.from(this);
		switch(item.getItemId()) {
		case R.id.menu_quit:
			finish();
			isExit = true;
			if(isMainPage)
				android.os.Process.killProcess(android.os.Process.myPid());
			break;
		case R.id.menu_about:
			AlertDialog dialog = new AlertDialog.Builder(CommonActivity.this)
			.setIcon(R.drawable.ic_launcher)
			.setTitle(getString(R.string.app_name))
			.setMessage("Ӧ�ð汾��"+versionName+"\n���ݰ汾��"+ListVersionName+"\n���ߣ�����\n���䣺hezhu.takeaway@gmail.com")
			.setNegativeButton("�ر�", null)
			.show();
			WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
//			params.width = ((getWindowManager().getDefaultDisplay().getWidth()<getWindowManager().getDefaultDisplay().getHeight())
//					?getWindowManager().getDefaultDisplay().getWidth():getWindowManager().getDefaultDisplay().getHeight())*3/4;
			params.width = getWindowManager().getDefaultDisplay().getWidth()*4/5;
			dialog.getWindow().setAttributes(params);
			break;
		case R.id.menu_feedback:
			if(isConnected()){
				final EditText et = new EditText(this);
				et.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
				et.setGravity(Gravity.TOP);
				et.setSingleLine(false);
				et.setHorizontallyScrolling(false);
	//			et.setHeight(((getWindowManager().getDefaultDisplay().getWidth()>getWindowManager().getDefaultDisplay().getHeight())
	//					?getWindowManager().getDefaultDisplay().getWidth():getWindowManager().getDefaultDisplay().getHeight())/3);
				et.setHeight(getWindowManager().getDefaultDisplay().getHeight()/3);
				new AlertDialog.Builder(this)
				.setTitle("���/����")
				.setView(et)
				.setPositiveButton("����", new OnClickListener(){
	
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						if(et.getText().toString().replaceAll("[\n|\r| ]","") == ""){
							Toast.makeText(getApplication(), "���ݲ���Ϊ��", Toast.LENGTH_SHORT).show();
							return;
						}
							showProgressDialog("���ڷ������/����","���Եȡ�");
							new Thread(new Runnable(){
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									try {
										URL url = new URL(FeedbackURL + "?text="+URLEncoder.encode(et.getText().toString().replace("\n", "<br />").replace("+", "&#43;")));
						                URLConnection conn = url.openConnection();
						                conn.setConnectTimeout(10000);
						                conn.setReadTimeout(10000);
						                conn.getInputStream();
						                handle.sendEmptyMessage(1);
									} catch (Exception e) {
										// TODO Auto-generated catch block
						                handle.sendEmptyMessage(-1);
										e.printStackTrace();
									}
								}
								
							}).start();
					}
					
				})
				.setNegativeButton("ȡ��", null).show();
				//dialog.getWindow().setAttributes(dialog.getWindow().getAttributes());
			} else Toast.makeText(getApplication(), "������������", Toast.LENGTH_SHORT).show();
			break;
		case R.id.menu_update_list:
			if(isConnected()){
				showProgressDialog("���ڼ�����","���Եȡ�");
				new Thread(new Runnable(){
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							URL url = new URL(VersionURL);
			                URLConnection conn = url.openConnection();
			                conn.setConnectTimeout(10000);
			                conn.setReadTimeout(10000);
			                InputStream in = conn.getInputStream();
			                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			                DocumentBuilder builder = factory.newDocumentBuilder();
			                Document doc = builder.parse(in);
			                newListVersionCode = Integer.parseInt(doc.getElementsByTagName("lvc").item(0).getFirstChild().getNodeValue());
			                handle.sendEmptyMessage(3);
						} catch (Exception e) {
							// TODO Auto-generated catch block
			                handle.sendEmptyMessage(-1);
							e.printStackTrace();
						}
					}
					
				}).start();
			} else Toast.makeText(getApplication(), "������������", Toast.LENGTH_SHORT).show();
			break;
		case R.id.menu_update:
			if(isConnected()){
				showProgressDialog("���ڼ�����","���Եȡ�");
				new Thread(new Runnable(){
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							URL url = new URL(VersionURL);
			                URLConnection conn = url.openConnection();
			                conn.setConnectTimeout(10000);
			                conn.setReadTimeout(10000);
			                InputStream in = conn.getInputStream();
			                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			                DocumentBuilder builder = factory.newDocumentBuilder();
			                Document doc = builder.parse(in);
			                newVersionCode = Integer.parseInt(doc.getElementsByTagName("vc").item(0).getFirstChild().getNodeValue());
			                newVersionName = doc.getElementsByTagName("vn").item(0).getFirstChild().getNodeValue();
			                newPathName = doc.getElementsByTagName("pn").item(0).getFirstChild().getNodeValue();
			                newListVersionCode = Integer.parseInt(doc.getElementsByTagName("lvc").item(0).getFirstChild().getNodeValue());
			                newListVersionName = doc.getElementsByTagName("lvn").item(0).getFirstChild().getNodeValue();
			                handle.sendEmptyMessage(0);
						} catch (Exception e) {
							// TODO Auto-generated catch block
			                handle.sendEmptyMessage(-1);
							e.printStackTrace();
						}
					}
					
				}).start();
			} else Toast.makeText(getApplication(), "������������", Toast.LENGTH_SHORT).show();
			break;
		case R.id.menu_add:
			final View vi = inflater.inflate(R.layout.add_item_dialog, null);
			new AlertDialog.Builder(this).setTitle("��ӵ���").setView(vi).setPositiveButton("���", new OnClickListener(){

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					if(((TextView) vi.findViewById(R.id.editTextName)).getText().toString().replaceAll("[\n|\r| ]","") == ""){
						Toast.makeText(getApplication(), "���ֲ���Ϊ��", Toast.LENGTH_SHORT).show();
						return;
					}
					Cursor cursor = sqlite.getReadableDatabase().rawQuery("select * from CustomList", null);
					int sequence = cursor.getCount();
					SQLiteDatabase dbw = sqlite.getWritableDatabase();
					ContentValues values = new ContentValues();
					values.put("name", ((TextView) vi.findViewById(R.id.editTextName)).getText().toString());
					values.put("number", ((TextView) vi.findViewById(R.id.editTextNumber)).getText().toString().replace("��", ","));
					values.put("detail", ((TextView) vi.findViewById(R.id.editTextDetail)).getText().toString());
					values.put("sequence", sequence);
					dbw.insert("CustomList", null, values);
					initList();
					if(((CheckBox) vi.findViewById(R.id.AddCheckBox)).isChecked())addFavorite(list.size()-1, list);
					initFavorite();
					setListPosition(list.size());
					if(!isMainPage)isChange = true;
				}
				
			}).setNegativeButton("ȡ��", null).show();
			//dialog.getWindow().setAttributes(dialog.getWindow().getAttributes());
			break;
		case R.id.menu_theme:
			final int oldTheme = sp.getInt("theme", 0);
			new AlertDialog.Builder(this).setTitle("��ѡ������").setSingleChoiceItems(themename, oldTheme, new OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					setThemeColor(which);
				}
				
			}).setPositiveButton("����", null).setNegativeButton("ȡ��", new OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					setThemeColor(oldTheme);
				}
				
			}).show();
			//dialog.getWindow().setAttributes(dialog.getWindow().getAttributes());
			break;
		case 97:
			if(isFavorite(lastPosition, currentList))removeFavorite(lastPosition, currentList);
			else addFavorite(lastPosition, currentList);
			if(!isMainPage){
				initFavorite();
				setListPosition(lastPosition);
			}
			break;
		case 98:
			final View editVi = inflater.inflate(R.layout.add_item_dialog, null);
			final TextView Name = (TextView) editVi.findViewById(R.id.editTextName);
			final TextView Number = (TextView) editVi.findViewById(R.id.editTextNumber);
			final TextView Detail = (TextView) editVi.findViewById(R.id.editTextDetail);
			final CheckBox cb = (CheckBox) editVi.findViewById(R.id.AddCheckBox);
			final ItemHolder ih = currentList.get(lastPosition);
			Name.setText(ih.Name);
			String NumberText = "";
			for(int i=0;i<ih.Number.length;i++) {
				NumberText+=ih.Number[i];
				if(i != ih.Number.length - 1) NumberText = NumberText + ",";
			}
			Number.setText(NumberText);
			Detail.setText(ih.Detail);
			cb.setChecked(isFavorite(lastPosition, currentList));
			new AlertDialog.Builder(this).setTitle("�༭����").setView(editVi).setPositiveButton("����", new OnClickListener(){

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					if(((TextView) editVi.findViewById(R.id.editTextName)).getText().toString().replaceAll("[\n|\r| ]","") == ""){
						Toast.makeText(getApplication(), "���ֲ���Ϊ��", Toast.LENGTH_SHORT).show();
						return;
					}
					SQLiteDatabase dbw = sqlite.getWritableDatabase();
					ContentValues values = new ContentValues();
					values.put("name", Name.getText().toString());
					values.put("number", Number.getText().toString().replace("��", ","));
					values.put("detail", Detail.getText().toString());
					dbw.update("CustomList", values, "_id=?", new String[]{Integer.toString(ih.ID)});
					initList();
					if(cb.isChecked())addFavorite(lastPosition, currentList);
					else removeFavorite(lastPosition, currentList);
					initFavorite();
					setListPosition(lastPosition);
					if(!isMainPage)isChange = true;
				}
				
			}).setNegativeButton("ȡ��", null).show();
			//dialog.getWindow().setAttributes(dialog.getWindow().getAttributes());
			break;
		case 99:
			int ID = currentList.get(lastPosition).ID;
			SQLiteDatabase dbr = sqlite.getReadableDatabase();
			SQLiteDatabase dbw = sqlite.getWritableDatabase();
			Cursor cursor = dbr.rawQuery("select * from CustomList where _id=?", new String[]{Integer.toString(ID)});
			cursor.moveToFirst();
			int sequence = cursor.getInt(4);
			cursor = dbr.rawQuery("select * from CustomList", null);
			dbw.execSQL("delete from CustomList where _id=?", new String[]{Integer.toString(ID)});
			dbw.execSQL("update CustomList set sequence=sequence-1 where sequence>?", new String[]{Integer.toString(sequence)});
			initList();
			initFavorite();
			setListPosition(lastPosition);
			if(!isMainPage)isChange = true;
			dbr.close();
			dbw.close();
			break;
		case 100:
			showDetail(lastPosition);
			break;
		default:
			ItemHolder _ih = currentList.get(lastPosition);
			if(item.getItemId()-101<_ih.Number.length)Call(_ih.Number[item.getItemId()-101]);
			else Send(_ih.SMS[item.getItemId()-_ih.Number.length-101]);
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	//���ͽ���
	public void selectNumber(final int position) {
		final ItemHolder ih = currentList.get(position);
		List<String> items = new ArrayList<String>();
		for(String s:ih.Number)items.add("����绰 "+s);
		if(ih.SMS!=null)for(String s:ih.SMS)items.add("���Ͷ��� "+s);
		new AlertDialog.Builder(this).setTitle("��ѡ�񶩲ͷ�ʽ").setItems(items.toArray(new String[]{}), new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				if(arg1<ih.Number.length)Call(ih.Number[arg1]);
				else Send(ih.SMS[arg1-ih.Number.length]);
			}
			
		}).setNegativeButton("ȡ��", null).show();
	}
	//����ָ������
	public void Call(String number) {
		Intent intent = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+number)); 
		startActivity(intent);
		Toast.makeText(getApplication(), "�밴���ż�", Toast.LENGTH_SHORT).show();
	}
	//������Ϣ
	public void Send(String number) {
		startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("smsto:" + number)));  
	}
	//��ʾ��ϸ��Ϣ
	public void showDetail(final int position) {
		ItemHolder ih = currentList.get(position);
		Builder dialog = new AlertDialog.Builder(CommonActivity.this).setTitle(ih.Name)
		.setPositiveButton("����", new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				selectNumber(position);
			}
			
		}).setNeutralButton((isFavorite(position, currentList))?"ȡ���ղ�":"�ղ�", new OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				if(isFavorite(position, currentList)) removeFavorite(position, currentList);
				else addFavorite(position, currentList);
				if(!isMainPage){
					initFavorite();
					setListPosition(position);
				}
			}
			
		})
		.setNegativeButton("ȡ��", null);
		if(ih.MenuList!=null) dialog.setAdapter(new SimpleAdapter(CommonActivity.this, ih.MenuList, R.layout.listview_menu_item ,new String[]{"MenuName","MenuPrice","MenuTitle"}, new int[]{R.id.MenuName,R.id.MenuPrice,R.id.MenuTitle}),null).show();
		else dialog.setMessage((ih.Detail=="")?"������ϸ��Ϣ���������°汾":(ih.Detail)).show();
	}
	//�����б�������Ĭ���б�
	public void initList(){
		list.clear();
		for(int i=0;i<defaultList.size();i++){
			list.add(defaultList.get(i));
		}
		ListView lv = (ListView) findViewById(R.id.listView);
		addFavoriteList();
		lv.setAdapter(new ItemAdapter(this,list));
	}
	//����ղ��б����б�
	private void addFavoriteList() {
		// TODO Auto-generated method stub
		SQLiteDatabase dbr = sqlite.getReadableDatabase();
		Cursor cursor = dbr.rawQuery("select * from CustomList order by sequence", null);
		while(cursor.moveToNext()){
			ItemHolder item = new ItemHolder();
			item.Name = cursor.getString(1);
			item.Number = cursor.getString(2).split(",");
			item.Detail = cursor.getString(3);
			item.isDefault = false;
			item.ID = cursor.getInt(0);
			list.add(item);
		}
		dbr.close();
	}
	//�ƶ����б�ָ��λ��
	public void setListPosition(int position){
		((ListView) findViewById(R.id.listView)).setSelection(position);
	}
	//��ʼ���б����ԣ�����������¼�
	public void initListView(){
		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				showDetail(position);
			}
		});
		
		lv.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				lastPosition = arg2;
				arg0.showContextMenu();
				return true;
			}
			
		});
		
		lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener(){

			@Override
			public void onCreateContextMenu(ContextMenu arg0, View arg1,
					ContextMenuInfo arg2) {
				// TODO Auto-generated method stub
				ItemHolder ih = currentList.get(lastPosition);
				arg0.setHeaderTitle(ih.Name);
				arg0.add(0, 97, 0, (isFavorite(lastPosition, currentList))?"ȡ���ղ�":"�ղ�");
				if(!ih.isDefault){
					arg0.add(0, 98, 0, "�༭����");
					arg0.add(0, 99, 0, "ɾ������");
				}
				arg0.add(0, 100, 0, "�鿴��ϸ��Ϣ");
				for(int i=0;i<ih.Number.length;i++)
					arg0.add(0, 101+i, 0, "����绰 "+ih.Number[i]);
				if(ih.SMS!=null)for(int i=0;i<ih.SMS.length;i++)
					arg0.add(0, 101+ih.Number.length+i, 0, "���Ͷ��� "+ih.SMS[i]);
			}
			
		});
	}
	//��ʼ���ղؽ���
	public void initFavorite(){
		if(isMainPage)return;
		TextView tvf = (TextView) findViewById(R.id.FavoriteTip);
		Cursor cursor = sqlite.getReadableDatabase().rawQuery("select * from FavoriteList order by sequence", null);
		favoriteList.clear();
		if(cursor.getCount()>0){
			int count = 0;
			while(cursor.moveToNext()){
				Boolean b = (cursor.getInt(2)==1)?true:false;
				int list_id = cursor.getInt(1);
				Boolean isExist = false;
				for(int i=0;i<list.size();i++){
					if((b==list.get(i).isDefault) && (list_id==list.get(i).ID)){
						favoriteList.add(list.get(i));
						isExist = true;
						break;
					}
				}
				if(!isExist)removeFavorite(cursor.getInt(2),list_id);
				else count++;
			}
			if(count==0)tvf.setVisibility(0);
			else tvf.setVisibility(8);
		} else tvf.setVisibility(0);
		initListView();
		ListView lv = (ListView) findViewById(R.id.listView);
		ItemAdapter adapter = new ItemAdapter(this,favoriteList);
		lv.setAdapter(adapter);
	}
	//����ղ�
	public void addFavorite(int position, List<ItemHolder> list){
		int is_default=(list.get(position).isDefault)?1:0;
		int list_id=list.get(position).ID;
		SQLiteDatabase dbr = sqlite.getReadableDatabase();
		Cursor cursor = dbr.rawQuery("select * from FavoriteList where list_id=? and is_default=?", 
				new String[]{Integer.toString(list_id),Integer.toString(is_default)});
		if(cursor.getCount()==0){
			cursor = dbr.rawQuery("select * from FavoriteList", null);
			ContentValues values = new ContentValues();
			values.put("list_id", list_id);
			values.put("is_default", is_default);
			values.put("sequence", cursor.getCount());
			sqlite.getWritableDatabase().insert("FavoriteList", null, values);
		}
		dbr.close();
	}
	//�Ƴ��ղأ�ֱ�ӣ�
	public void removeFavorite(int is_default, int list_id){
		Cursor cursor = sqlite.getReadableDatabase().rawQuery("select * from FavoriteList where list_id=? and is_default=?", 
				new String[]{Integer.toString(list_id),Integer.toString(is_default)});
		if(cursor.getCount()==1){
			cursor.moveToFirst();
			int sequence = cursor.getInt(3);
			SQLiteDatabase dbw = sqlite.getWritableDatabase();
			dbw.execSQL("delete from FavoriteList where list_id=? and is_default=?", 
				new String[]{Integer.toString(list_id),Integer.toString(is_default)});
			dbw.execSQL("update FavoriteList set sequence=sequence-1 where sequence>?", new String[]{Integer.toString(sequence)});
			dbw.close();
		}
	}
	//�Ƴ��ղ�
	public void removeFavorite(int position, List<ItemHolder> list){
		removeFavorite(((list.get(position).isDefault)?1:0),(list.get(position).ID));
	}
	//�ж��Ƿ�Ϊ�ղ�
	public boolean isFavorite(int position, List<ItemHolder> list){
		return sqlite.getReadableDatabase().rawQuery("select * from FavoriteList where list_id=? and is_default=?", 
				new String[]{Integer.toString(list.get(position).ID),Integer.toString((list.get(position).isDefault)?1:0)}).getCount()!=0;
	}
	//���ı�����Ĭ���б�
	public void updateList(String listXML){
		try {
			OutputStream os = openFileOutput("list.xml",MODE_PRIVATE);
			OutputStreamWriter osw = new OutputStreamWriter(os);
			osw.write(listXML);
			osw.close();
			os.close();
		} catch (Exception e) {}
	}
	//�ж��Ƿ�����������
	private boolean isConnected(){
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		return (cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)||(cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED);
	}
	//���������б�������
	public void initDefaultList(){
		defaultList.clear();
		list.clear();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (Exception e) {}
        try {
        	NamedNodeMap attributes;
        	Document doc = builder.parse(this.openFileInput("list.xml"));
			ListVersionCode = Integer.parseInt(doc.getFirstChild().getAttributes().getNamedItem("c").getNodeValue());
			ListVersionName = doc.getFirstChild().getAttributes().getNamedItem("n").getNodeValue();
	        NodeList nl = doc.getElementsByTagName("e");
	        for(int i=0;i<nl.getLength();i++){
	        	ItemHolder item = new ItemHolder();
            	attributes = nl.item(i).getAttributes();
	        	item.ID = Integer.parseInt(attributes.getNamedItem("i").getNodeValue());
	        	item.Number = attributes.getNamedItem("m").getNodeValue().split(",");
	        	if(attributes.getNamedItem("s")!=null)item.SMS = attributes.getNamedItem("s").getNodeValue().split(",");
	        	item.Name = attributes.getNamedItem("n").getNodeValue();
	        	item.isDefault = true;
	        	if(attributes.getNamedItem("d")==null){
	        		List<HashMap<String,String>> MenuList = new ArrayList<HashMap<String,String>>();
	        		NodeList row = nl.item(i).getChildNodes();
	                for(int j=0;j<row.getLength();j++){
	                	if(row.item(j).getNodeType() != Node.ELEMENT_NODE)continue;
	                	attributes = row.item(j).getAttributes();
	                	HashMap<String,String> map = new HashMap<String,String>();
	                	map.put("MenuName", (attributes.getNamedItem("n")) != null?attributes.getNamedItem("n").getNodeValue():"");
	                	map.put("MenuPrice", (attributes.getNamedItem("p")) != null?attributes.getNamedItem("p").getNodeValue():"");
	                	map.put("MenuTitle", (attributes.getNamedItem("t")) != null?attributes.getNamedItem("t").getNodeValue():"");
	                	MenuList.add(map);
	                }
	                item.MenuList = MenuList;
	        	} else item.Detail = attributes.getNamedItem("d").getNodeValue();
	        	defaultList.add(item);
	        	list.add(item);
	    		editor.putInt("ListVersionCode", ListVersionCode).commit();
	        }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ListView lv = (ListView) findViewById(R.id.listView);
		addFavoriteList();
		lv.setAdapter(new ItemAdapter(this,list));
	}
	//��ʾ�ȴ��򣨸����ã�
	public void showProgressDialog(String title, String message){
		updateDialog = new ProgressDialog(CommonActivity.this);
		updateDialog.setTitle(title);
		updateDialog.setMessage(message);
		updateDialog.setCanceledOnTouchOutside(false);
		updateDialog.show();
	}
}
