package com.example.socket_chat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Date;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private Button bt1,bt2;
	private EditText ed1,ed2;
	private TextView tv1,tv2;
	private Socket mSocket=null;
	private BufferedReader mBufferedReader;
	private PrintWriter mPrintWriter;
	private AlertDialog.Builder mAlertDialog;
	private Thread net_;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		object();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(mSocket==null){
			net_init();
		}
	}
	
	@Override
	public void onPause(){
		super.onPause();
		
	}
	
	private void net_init(){
		
		Thread net_ = new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message message = new Message();
				
				try {
					mSocket = new Socket(InetAddress.getByName("192.168.2.131"),8080);
					message.what=1;handler.sendMessage(message);
					mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
					mPrintWriter = new PrintWriter(new OutputStreamWriter(mSocket.getOutputStream()),true);
					while(true){
						String mMessage = mBufferedReader.readLine();
						if(mMessage!=null){
						message = handler.obtainMessage(2, mMessage);
						handler.sendMessage(message);
						}
					}
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					message.what=0;handler.sendMessage(message);
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					message.what=0;handler.sendMessage(message);
					e.printStackTrace();
				}
			}});
		net_.start();
		
	}
	
	Handler handler = new Handler(){
		public void handleMessage(Message msg){
			super.handleMessage(msg);
				switch(msg.what){
				case 0:
					Toast.makeText(getApplicationContext(), "oops something wrong", Toast.LENGTH_SHORT).show();
					break;
				case 1:
					Toast.makeText(getApplicationContext(), "connect successed", Toast.LENGTH_SHORT).show();
					tv2.setText(""+mSocket.getLocalAddress());
					break;
				case 2:
					String message = (String) msg.obj;
					tv1.setText(tv1.getText().toString()+"\n"+message);
					save_history();
					break;
			}
		
		}
	};
	
	private void object(){
		bt1 = (Button)this.findViewById(R.id.bt1);
		bt2 = (Button)this.findViewById(R.id.bt2);
		ed1 = (EditText)this.findViewById(R.id.ed1);
		ed2 = (EditText)this.findViewById(R.id.ed2);
		tv1 = (TextView)this.findViewById(R.id.tv1);
		tv2 = (TextView)this.findViewById(R.id.tv2);
		bt1.setOnClickListener(click_);
		bt2.setOnClickListener(click_);
		tv1.setMovementMethod(new ScrollingMovementMethod());
	}
	
	View.OnClickListener click_ = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			switch(id){
			case R.id.bt1:
				if(!(ed2.getText().equals("")&&ed1.getText().equals("")))mPrintWriter.println(ed1.getText()+";"+mSocket.getLocalAddress().toString().replace("/", "")+";"+ed2.getText());
				Toast.makeText(getApplicationContext(), "sent successed",Toast.LENGTH_SHORT).show();
				break;
			case R.id.bt2:
				leave();
				break;
			}
		}
	};
	
	private void leave(){
		mAlertDialog = new AlertDialog.Builder(MainActivity.this);
		mAlertDialog.setTitle("oops!!");
		mAlertDialog.setMessage("are you sure to leave??");
		mAlertDialog.setCancelable(true);
		mAlertDialog.setPositiveButton("yape", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Thread thread = new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							mPrintWriter.println("192.168.2.131;"+mSocket.getLocalAddress().toString().replace("/", "")+";byebye");
							mBufferedReader.close();
							//mSocket.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}finally{
							mPrintWriter.close();
						}
						try{
							net_.interrupt();
						}catch(Exception e){
							e.printStackTrace();
						}
						try{
							mSocket.close();
						}catch(IOException e){
							e.printStackTrace();
						}

					}});
				thread.start();
				save_history();
				finish();
			}
		});
		mAlertDialog.create().show();
	}
	
	
	public void save_history() {
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy MM dd");
		Date mDate = new Date(System.currentTimeMillis());
		String mDay = mSimpleDateFormat.format(mDate);
		String Path = Environment.getExternalStorageDirectory().getPath();
		File Dir = new File(Path+"/chat_history");
		StringBuilder mStringBuilder = new StringBuilder();
		FileInputStream mFileInputStream;
		FileOutputStream mFileOutputStream;
		if(!Dir.exists())Dir.mkdir();
		try{
			File mFile = new File(Path+"/chat_history"+"/"+mDay+".txt");
			if(mFile.exists()){
				mFileInputStream = new FileInputStream(mFile);
				byte[] RData = new byte[mFileInputStream.available()];
				while(mFileInputStream.read(RData)!=-1){
					mStringBuilder.append(new String(RData));
				}
				mFileInputStream.close();
				mFileOutputStream = new FileOutputStream(mFile);
				mStringBuilder.append(tv1.getText().toString());
				mFileOutputStream.write(mStringBuilder.toString().getBytes());
				mFileOutputStream.close();
			}else{
				mFileOutputStream = new FileOutputStream(mFile);
				mFileOutputStream.write(tv1.getText().toString().getBytes());
				mFileOutputStream.close();
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
