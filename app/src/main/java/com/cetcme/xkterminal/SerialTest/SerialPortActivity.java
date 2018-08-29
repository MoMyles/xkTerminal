/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.cetcme.xkterminal.SerialTest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.TextView;

import com.cetcme.xkterminal.MyApplication;
import com.cetcme.xkterminal.R;

import android_serialport_api.SerialPort;

public class SerialPortActivity extends Activity {

	byte mValueToSend;
	boolean mByteReceivedBack;
	Object mByteReceivedBackSemaphore = new Object();
	Integer mIncoming = 0;
	Integer mOutgoing = 0;
	Integer mLost = 0;
	Integer mCorrupted = 0;

	SendingThread mSendingThread;
	TextView mTextViewOutgoing;
	TextView mTextViewIncoming;
	TextView mTextViewLost;
	TextView mTextViewCorrupted;

	private MyApplication mApplication;
	private SerialPort mSerialPort;
	private OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;

	private class ReadThread extends Thread {

		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				int size;
				try {
					byte[] buffer = new byte[64];
					if (mInputStream == null) return;
					size = mInputStream.read(buffer);
					if (size > 0) {
						onDataReceived(buffer, size);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	private class SendingThread extends Thread {
		@Override
		public void run() {
			while (!isInterrupted()) {
				synchronized (mByteReceivedBackSemaphore) {
					mByteReceivedBack = false;
					try {
						if (mOutputStream != null) {
							mOutputStream.write(mValueToSend);
						} else {
							return;
						}
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}
					mOutgoing++;
					// Wait for 100ms before sending next byte, or as soon as
					// the sent byte has been read back.
					try {
						mByteReceivedBackSemaphore.wait(100);
						if (mByteReceivedBack == true) {
							// Byte has been received
							mIncoming++;
						} else {
							// Timeout
							mLost++;
						}
						runOnUiThread(new Runnable() {
							public void run() {
								mTextViewOutgoing.setText(mOutgoing.toString());
								mTextViewLost.setText(mLost.toString());
								mTextViewIncoming.setText(mIncoming.toString());
								mTextViewCorrupted.setText(mCorrupted.toString());
							}
						});
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	private void DisplayError(int resourceId) {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Error");
		b.setMessage(resourceId);
		b.setPositiveButton("OK", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				SerialPortActivity.this.finish();
			}
		});
		b.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_serial_port);

		mApplication = (MyApplication) getApplication();
		try {
//			mSerialPort = mApplication.getGpsSerialPort();
//			mSerialPort = mApplication.getAisSerialPort();
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();

			/* Create a receiving thread */
			mReadThread = new ReadThread();
			mReadThread.start();
		} catch (SecurityException e) {
			DisplayError(R.string.error_security);
		} catch (Exception e) {
			DisplayError(R.string.error_unknown);
		}

		mTextViewOutgoing = findViewById(R.id.TextViewOutgoingValue);
		mTextViewIncoming = findViewById(R.id.TextViewIncomingValue);
		mTextViewLost = findViewById(R.id.textViewLostValue);
		mTextViewCorrupted = findViewById(R.id.textViewCorruptedValue);
		if (mSerialPort != null) {
			mSendingThread = new SendingThread();
			mSendingThread.start();
		}
	}

	protected void onDataReceived(byte[] buffer, int size) {

		synchronized (mByteReceivedBackSemaphore) {
			int i;
			for (i = 0; i < size; i++) {
				if ((buffer[i] == mValueToSend) && (mByteReceivedBack == false)) {
					mValueToSend++;
					// This byte was expected
					// Wake-up the sending thread
					mByteReceivedBack = true;
					mByteReceivedBackSemaphore.notify();
				} else {
					// The byte was not expected
					mCorrupted++;
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		if (mReadThread != null)
			mReadThread.interrupt();
		if (mSendingThread != null)
			mSendingThread.interrupt();

		mApplication.closeSerialPort();
		mSerialPort = null;

		super.onDestroy();
	}
}
