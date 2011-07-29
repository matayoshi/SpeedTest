/*
BSD License

Copyright(c) 2011, N.Matayoshi All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

・Redistributions of source code must retain the above copyright notice, 
  this list of conditions and the following disclaimer.
・Redistributions in binary form must reproduce the above copyright notice, 
  this list of conditions and the following disclaimer in the documentation 
  and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package nmtysh.android.test.speedtest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class SpeedTestActivity extends Activity {
	List<String> list;
	EditText urlEdit;
	private ArrayAdapter<String> adapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ListView listView = (ListView) findViewById(android.R.id.list);

		urlEdit = (EditText) findViewById(R.id.url_address);
		Button button = (Button) findViewById(R.id.use_http_client);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				runHttpClient();
			}
		});
		button = (Button) findViewById(R.id.use_http_url_connection);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				runHttpURLConnection();
			}
		});

		list = new LinkedList<String>();
		adapter = new ArrayAdapter<String>(this, R.layout.list, list);
		listView.setAdapter(adapter);
	}

	AsyncTask<Void, Integer, Void> task = null;

	private void runHttpClient() {
		final String url = urlEdit.getText().toString();
		if (url == null
				|| (!url.startsWith("http://") && !url.startsWith("https://"))) {
			list.add("URL error!");
			adapter.notifyDataSetChanged();
			return;
		}
		task = new AsyncTask<Void, Integer, Void>() {
			long startTime;
			ProgressDialog progress;

			// 実行準備。事前処理
			@Override
			protected void onPreExecute() {
				super.onPreExecute();

				progress = new ProgressDialog(SpeedTestActivity.this);
				progress.setMessage(getString(R.string.progress_message));
				progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progress.setIndeterminate(false);
				progress.setCancelable(true);
				progress.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						task.cancel(true);
					}
				});
				progress.setMax(10);
				progress.setProgress(0);
				progress.show();

				startTime = System.currentTimeMillis();
			}

			// バックグラウンドで実行する
			@Override
			protected Void doInBackground(Void... params) {
				// 10回実行した内の平均を取る
				for (int i = 0; i < 10; i++) {
					HttpClient client = new DefaultHttpClient();
					InputStreamReader in = null;
					// BufferedReader br = null;
					try {
						HttpGet get = new HttpGet(url);
						HttpResponse response = client.execute(get);
						if (response.getStatusLine().getStatusCode() < 400) {
							in = new InputStreamReader(response.getEntity()
									.getContent());
							// br = new BufferedReader(in);
							// while (br.readLine() != null) {
							while (in.read() != -1) {
								;
							}
						}
					} catch (IOException e) {
					} finally {
						/*
						 * if (br != null) { try { br.close(); } catch
						 * (IOException e) { } br = null; }
						 */
						if (in != null) {
							try {
								in.close();
							} catch (IOException e) {
							}
							in = null;
						}
					}
					publishProgress(i + 1);
					// Dos攻撃と勘違いされないように
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
				return null;
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				super.onProgressUpdate(values);
				if (progress == null) {
					return;
				}
				progress.setProgress(values[0]);
			}

			// 実行完了。後始末。
			@Override
			protected void onPostExecute(Void result) {
				long endTime = System.currentTimeMillis();

				// プログレスダイアログをキャンセル
				progress.cancel();
				progress = null;

				list.add("HttpClient:" + url + " " + (endTime - startTime)
						+ "msec/10" + " " + (endTime - startTime) / 10 + "msec");
				adapter.notifyDataSetChanged();
			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				progress.dismiss();
				progress = null;
			}
		}.execute();
	}

	private void runHttpURLConnection() {
		final String url = urlEdit.getText().toString();
		if (url == null
				|| (!url.startsWith("http://") && !url.startsWith("https://"))) {
			list.add("URL error!");
			adapter.notifyDataSetChanged();
			return;
		}
		task = new AsyncTask<Void, Integer, Void>() {
			long startTime;
			ProgressDialog progress;

			// 実行準備。事前処理
			@Override
			protected void onPreExecute() {
				super.onPreExecute();

				progress = new ProgressDialog(SpeedTestActivity.this);
				progress.setMessage(getString(R.string.progress_message));
				progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progress.setIndeterminate(false);
				progress.setCancelable(true);
				progress.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						task.cancel(true);
					}
				});
				progress.setMax(10);
				progress.setProgress(0);
				progress.show();

				startTime = System.currentTimeMillis();
			}

			// バックグラウンドで実行する
			@Override
			protected Void doInBackground(Void... params) {
				// 10回実行した内の平均を取る
				for (int i = 0; i < 10; i++) {
					HttpURLConnection connection = null;
					InputStreamReader in = null;
					// BufferedReader br = null;
					try {
						connection = (HttpURLConnection) (new URL(url))
								.openConnection();
						connection.setRequestMethod("GET");
						connection.connect();
						in = new InputStreamReader(connection.getInputStream());
						// br = new BufferedReader(in);
						// while (br.readLine() != null) {
						while (in.read() != -1) {
							;
						}
					} catch (IOException e) {
					} finally {
						/*
						 * if (br != null) { try { br.close(); } catch
						 * (IOException e) { } br = null; }
						 */
						if (in != null) {
							try {
								in.close();
							} catch (IOException e) {
							}
							in = null;
						}
						if (connection != null) {
							connection.disconnect();
						}
					}
					publishProgress(i + 1);
					// Dos攻撃と勘違いされないように
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
				return null;
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				super.onProgressUpdate(values);
				if (progress == null) {
					return;
				}
				progress.setProgress(values[0]);
			}

			// 実行完了。後始末。
			@Override
			protected void onPostExecute(Void result) {
				long endTime = System.currentTimeMillis();

				// プログレスダイアログをキャンセル
				progress.cancel();
				progress = null;

				list.add("HttpURLConnection:" + url + " "
						+ (endTime - startTime) + "msec/10" + " "
						+ (endTime - startTime) / 10 + "msec");
				adapter.notifyDataSetChanged();
			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				progress.dismiss();
				progress = null;
			}
		}.execute();
	}
}
// EOF