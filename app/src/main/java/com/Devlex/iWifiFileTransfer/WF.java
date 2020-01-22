package com.Devlex.iWifiFileTransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import android.content.pm.ActivityInfo;
import android.os.Environment;

import com.google.firebase.analytics.FirebaseAnalytics;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.Devlex.iWifiFileTransfer.misc.RootsCache;
import com.Devlex.iWifiFileTransfer.misc.SAFManager;
import com.Devlex.iWifiFileTransfer.misc.Utils;
import com.Devlex.iWifiFileTransfer.model.DocumentsContract;
import com.Devlex.iWifiFileTransfer.model.RootInfo;
import com.Devlex.iWifiFileTransfer.provider.RecentsProvider;
import com.Devlex.iWifiFileTransfer.provider.RecentsProvider.ResumeColumns;
import com.Devlex.iWifiFileTransfer.setting.SettingsActivity;

import static android.content.ContentValues.TAG;


import static com.Devlex.iWifiFileTransfer.misc.SAFManager.ADD_STORAGE_REQUEST_CODE;
import static com.Devlex.iWifiFileTransfer.provider.ExternalStorageProvider.intcard;
import static com.Devlex.iWifiFileTransfer.provider.ExternalStorageProvider.sdcard;


public class WF extends AppCompatActivity implements MessageDisplayer {
	final String Version = "Version date: March 31, 2017 @ 1450\n";

	//----------------------------------------------------------------------------
	final String rootDir = "/storage/emulated/0/";
	final String LogFilePath = rootDir + "/WifiFileTransferLog_"; //.txt";
	private FirebaseAnalytics mFirebaseAnalytics;
	static final int PORT = 8080;
	private String host = null; //"127.0.0.1";
	private String root = rootDir;
	private String savedFilesFolder = root;  // where upload saves files
	private boolean mShowAsDialog;
	private DrawerLayout mDrawerLayout;

	//  Constant String for Bundles etc
	private final String SavedFolderName = "savedFolderName.txt";  // name of where we save the above
	private final String RunningServer_K = "runningServer";

	SWS server;
	boolean runningServer = false;  // remember if we are running the server
	boolean debug = false;           // control debug output
	boolean addFilenamePrefix = true;  // tell server to add temp prefix to saved files
	boolean useService = false;   // controls if we run the Server in a Service
	boolean serviceIsRunning = false;

	public static String path;

	private CircleMenu circleMenu;
	public List<Integer> subMenuColorListoriginal;
	public List<Drawable> subMenuDrawableListoriginal;

	//- - - - - - - - - - - - - - - - - - - - - - -
	// Define inner class to handle exceptions
	class MyExceptionHandler implements Thread.UncaughtExceptionHandler {
		public void uncaughtException(Thread t, Throwable e) {
			Date dt = new Date();
			String fn = ExcpFilePathPfx + "exception_" + sdf.format(dt) + ".txt";
			try {
				PrintStream ps = new PrintStream(fn);
				e.printStackTrace(ps);
				ps.close();
				System.out.println("SWS wrote trace to " + fn);
				e.printStackTrace(); // capture here also???
				SaveStdOutput.stop(); // close here vs calling flush() in class
			} catch (Exception x) {
				x.printStackTrace();
			}
			lastUEH.uncaughtException(t, e); // call last one  Gives: "Unfortunately ... stopped" message
			return;    //???? what to do here
		}
	}

	//---------------------------------------------------------------------------------------
	// Logging stuff
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss", Locale.US);
	SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd 'T' HH:mm:ss", Locale.US); // showTime
	Thread.UncaughtExceptionHandler lastUEH = null;
	final String ExcpFilePathPfx = root + "logs/WifiFileTransferlog_";

	private TextView hello;
	private Handler handler = new Handler();

	// Request codes for startActivity
	final int FolderChosenForSave = 112;
	final int RESULT_SETTINGS = 31;
	final int DELAYED_MESSAGE = 321;

	public static final int REQUEST_CODE_STORAGE_ACCESS = 5;

	//---------------------------------------------------------
	//  Utility classes to allow Service to send message
	class UpdateUI implements Runnable {
		String updateString;

		public UpdateUI(String updateString) {
			this.updateString = updateString;
		}

		public void run() {
			//  Append new to old
			String text = hello.getText().toString();
			hello.setText(text + "\n" + updateString);
		}
	}

	class MyResultReceiver extends ResultReceiver {
		public MyResultReceiver(Handler handler) {
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
//			System.out.println("oRR resCode="+resultCode +", bndl="+resultData); //<<<<<<<<
			if (resultCode == 100) {
				runOnUiThread(new UpdateUI(resultData.getString("text")));
			} else if (resultCode == 200) {
				runOnUiThread(new UpdateUI(resultData.getString("end")));
			} else {
				runOnUiThread(new UpdateUI("Result Received " + resultCode));
			}
		}
	}  // end class

	MyResultReceiver resultReceiver;
	public String title;

	public static Context context;

	public static Context getcontext(){
		return context;
	}
	//------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		/*getSupportActionBar().setTitle("Wifi Transfer File");
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setIcon((int) R.drawable.ic_action_name);*/
		setContentView(R.layout.act);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		if (Build.VERSION.SDK_INT >= 23) {
			if (!(ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") == 0 && ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") == 0)) {
				ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, 1);
			}

				mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
			hello = (TextView) findViewById(R.id.hello);

			getPreferences();


		}


		context=getApplicationContext();


		// Catch exceptions and write to a separate file
		lastUEH = Thread.getDefaultUncaughtExceptionHandler(); // save previous one
		Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler());
		// end trying to catch exceptions

		//------------------------------------------
		// Make sure have our folder
		File testSWSFolder = new File(root);
		if (!testSWSFolder.exists()) {
			boolean res = testSWSFolder.mkdir();
			System.out.println("SWS created folder=" + testSWSFolder + " " + res);
		}

		// Get the previous Saved files folder
		try {
			FileInputStream fis = openFileInput(SavedFolderName);
			byte[] bfr = new byte[600]; // should never be this long
			int nbrRd = fis.read(bfr);
			String theText = new String(bfr, 0, nbrRd);
			System.out.println("SWS read savedFolder=" + theText);
			fis.close();
			savedFilesFolder = theText;  // set the folder from the previous session
		} catch (Exception x) {
			x.printStackTrace();
		}


		// Test if restarted
		if (savedInstanceState != null) {

			runningServer = savedInstanceState.getBoolean(RunningServer_K);
			if (runningServer) {

			}
		}

		System.setProperty("java.io.tmpdir", savedFilesFolder);  //<<<<<<<<< where to write uploads

		// Some more data about how we were started
		Intent intent = getIntent();
		System.out.println("SWS onCreate() intent=" + intent
				+ "\n >>data=" + intent.getData()
				+ "\n >>savedInstanceState=" + savedInstanceState
				+ "\n >>extras=" + intent.getExtras());

		// Were we started by an Intent?
		Bundle bndl = intent.getExtras();
		if (bndl != null) {
			Set<String> set = bndl.keySet();
			System.out.println(" >> SWS bndl keySet=" + Arrays.toString(set.toArray()));
		}



		mRoots = DocumentsApplication.getRootsCache(this);
        //copy(copiedfile, mov, getcontext());
//setResult(Activity.RESULT_CANCELED);

		//setContentView(R.layout.activity);

		final Context context = this;
		final Resources res = getResources();
		//mShowAsDialog = res.getBoolean(R.bool.show_as_dialog);




//		if (savedInstanceState != null) {
//			mState = savedInstanceState.getParcelable(EXTRA_STATE);
//			mAuthenticated = savedInstanceState.getBoolean(EXTRA_AUTHENTICATED);
//			mActionMode = savedInstanceState.getBoolean(EXTRA_ACTIONMODE);
//		} else {
//			buildDefaultState();
//		}
//
//
//
//
//
//		setSupportActionBar(mToolbar);







		// Hide roots when we're managing a specific root










		circleMenu = (CircleMenu) findViewById(R.id.circle_menu);

		circleMenu.setMainMenu(Color.parseColor("#CDCDCD"), R.drawable.icon_menu, R.drawable.icon_cancel)
				.addSubMenu(Color.parseColor("#258CFF"), R.drawable.micro)
				.addSubMenu(Color.parseColor("#30A400"), R.drawable.inter)
				.addSubMenu(Color.parseColor("#FF4B32"), R.drawable.star)
				.addSubMenu(Color.parseColor("#8A39FF"), R.drawable.icon3)
				.setOnMenuSelectedListener(new OnMenuSelectedListener() {

					@Override
					public void onMenuSelected(int index) {
						subMenuColorListoriginal = circleMenu.subMenuColorList;

						subMenuDrawableListoriginal = circleMenu.subMenuDrawableList;
						Bundle bundle = new Bundle();
						switch (index) {
							case 0:

								if (((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getIpAddress() == 0) {
									showMsg("IP address not found - Please connect to your Wifi");
									try {
										Thread.sleep(2000);
									} catch (Exception e) {
									}
									Message obtainMessage = new Handler().obtainMessage(321);
								}
								else {
									final ContentResolver resolver = context.getContentResolver();
									final List<UriPermission> perms = resolver
											.getPersistedUriPermissions();

									if (perms.isEmpty()) {
										android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(WF.this);
										Spanned message = Utils.fromHtml("Select your  " + "<b>" + "SD Card" + "</b>" + "  then Press  " + "<b>" + "SELECT" + "</b>" +
												"  to grant permission from next screen");


										builder.setTitle("Grant accesss to External Storage")
												.setMessage(message)
												.setPositiveButton("Give Access", new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialogInterfaceParam, int code) {
												/*GifImageView gifImageView = (GifImageView) findViewById(R.id.gifImageView);
												gifImageView.setImageDrawable(R.drawable.sdgif);*/

														final ImageView imgView = (ImageView) findViewById(R.id.gifImageView);

														imgView.setVisibility(View.VISIBLE);
														Toast.makeText(WF.this, "Follow the steps!", Toast.LENGTH_SHORT).show();

														imgView.setOnClickListener(new View.OnClickListener() {
															@Override
															public void onClick(View v) {
																Toast.makeText(WF.this, "Repeat the steps!", Toast.LENGTH_SHORT).show();
																Toast.makeText(WF.this, "Repeat the steps!", Toast.LENGTH_SHORT).show();

																imgView.setVisibility(View.GONE);

																Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
																intent.setPackage("com.android.documentsui");
																try {
																	startActivityForResult(intent, ADD_STORAGE_REQUEST_CODE);


																} catch (ActivityNotFoundException e) {
																	CrashReportingManager.logException(e, true);
																}
															}
														});


													}
												})
												.setNegativeButton("Cancel", null);
										DialogFragment.showThemedDialog(builder);

							/*

											Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
							startActivityForResult(intent, ADD_STORAGE_REQUEST_CODE);*/
									} else {



								/*Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
								intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
								intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
								intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
								intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
								intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
								startActivityForResult(intent, 42);*/

										SDcardpath();
										Toast.makeText(WF.this, "Server started -SDCard Selected", Toast.LENGTH_SHORT).show();
										if (circleMenu.subMenuDrawableList.size() < 5) {
											circleMenu.addSubMenu(Color.parseColor("#FF6A00"), R.drawable.stop);
										}
										circleMenu.itemNum = Math.min(5, 5);
										bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "1");
										bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "SDCard Storage");
										bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "SDCard Selected");
										mFirebaseAnalytics.logEvent("SDCard_Selected", bundle);
										break;
									}
									break;
								}
							case 1:

								if (((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getIpAddress() == 0) {
									showMsg("IP address not found - Please connect to your Wifi");
									try {
										Thread.sleep(2000);
									} catch (Exception e) {
									}
									Message obtainMessage = new Handler().obtainMessage(321);
								}
								else {
									Internalpath();
									Toast.makeText(WF.this, "Server started -Internal Storage Selected", Toast.LENGTH_SHORT).show();
									if (circleMenu.subMenuDrawableList.size() < 5) {
										circleMenu.addSubMenu(Color.parseColor("#FF6A00"), R.drawable.stop);
									}
									circleMenu.itemNum = Math.min(5, 5);
									bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "2");
									bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Internal Storage");
									bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Internal Storage Selected");
									mFirebaseAnalytics.logEvent("Internal_Storage_Selected", bundle);
									break;
								}
								break;
							case 2:
								Toast.makeText(WF.this, "Rate this app,Thnx!", Toast.LENGTH_SHORT).show();
								startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=com.Devlex.iWifiFileTransfer")));
								bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "3");
								bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Rate");
								bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Rate");
								mFirebaseAnalytics.logEvent("Rate_Selected", bundle);
								break;
							case 3:
								Toast.makeText(WF.this, "About", Toast.LENGTH_SHORT).show();
								showMsg("Wifi Transfer File\nVersion date: May 24, 2017 \nEmail: bachir.khiati@gmail.com");
								bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "4");
								bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Abour");
								bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "About");
								mFirebaseAnalytics.logEvent("About_Selected", bundle);
								break;
							case 4:
								Stoppedserver();
								circleMenu.itemNum = Math.min(4, 4);
								circleMenu.subMenuColorList = subMenuColorListoriginal;
								circleMenu.subMenuDrawableList = subMenuDrawableListoriginal;
								Toast.makeText(WF.this, "Server Stopped", Toast.LENGTH_SHORT).show();
								break;
							default:
						}

					}

				}).setOnMenuStatusChangeListener(new OnMenuStatusChangeListener() {

			@Override
			public void onMenuOpened() {
			}

			@Override
			public void onMenuClosed() {
			}

		});
	}


	// end onCreate()





	//===============================
	@Override
	protected void onResume() {
		super.onResume();
		System.out.println("SWS onResume() RunServerService running=" + isMyServiceRunning(RunServerService.class));
		getPreferences();
		//if (!updateipp()) {
		this.serviceIsRunning = isMyServiceRunning(RunServerService.class);
		if (this.serviceIsRunning) {
			showMessage("SWS >>> RunServerService is running at " + new Date());
		}
	}
	//


	public void updateipp() {

		TextView textIpaddr = (TextView) findViewById(R.id.ipaddr);
		textIpaddr.setGravity(1);
		if (((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getIpAddress() == 0) {
			showMsg("IP address not found - Please connect to your Wifi");
			try {
				Thread.sleep(2000);
			} catch (Exception e) {
			}
			Message obtainMessage = new Handler().obtainMessage(321);
		}
		String formatedIpAddress = String.format("%d.%d.%d.%d", Integer.valueOf(((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getIpAddress() & MotionEventCompat.ACTION_MASK), Integer.valueOf((((WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE)).getConnectionInfo().getIpAddress() >> 8) & MotionEventCompat.ACTION_MASK), Integer.valueOf((((WifiManager) getApplicationContext().getSystemService("wifi")).getConnectionInfo().getIpAddress() >> 16) & MotionEventCompat.ACTION_MASK), Integer.valueOf((((WifiManager) getApplicationContext().getSystemService("wifi")).getConnectionInfo().getIpAddress() >> 24) & MotionEventCompat.ACTION_MASK));
		if (this.runningServer) {
			textIpaddr.setText("Link: http://" + formatedIpAddress + ":" + PORT + getLocalIpAddress());
		} else {
			textIpaddr.setText("Waiting for the server to start!");
		}
	} // end onResume()


	//-----------------------------------------
	private void getPreferences() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String debugKey = getResources().getString(R.string.set_debug_text_key);
		debug = preferences.getBoolean(debugKey, true);
		String addPrefixKey = getResources().getString(R.string.add_prefix_key);
		addFilenamePrefix = preferences.getBoolean(addPrefixKey, true);
		String useServiceKey = getResources().getString(R.string.use_service_key);
		useService = preferences.getBoolean(useServiceKey, false);
	}

	//--------------------------------------
	public String getLocalIpAddress() {
		try {
			for (Enumeration<?> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
				NetworkInterface intf = (NetworkInterface) en.nextElement();
				String val = "";
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					System.out.println("SWS hostAddress=" + inetAddress.getHostAddress().toString()); //<<<<<
					if (!inetAddress.isLoopbackAddress()) {
						val = inetAddress.getHostAddress().toString(); //  <<< Last vs first ???
					}
				}
				return val;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	//-----------------------------------------------------------------
	private boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	//========================================================
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.simple_web_server, menu);
		super.onCreateOptionsMenu(menu);


		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.

		switch (item.getItemId()) {

			case R.id.set_saved_folder:
				// Set the folder to save to
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setTitle("Set folder to save to");
				alert.setMessage("Enter folder name to upload to."
						+ "\nFiles currently written to " + savedFilesFolder);

				// Set an EditText view to get user input
				final EditText input = new EditText(this);
				input.setText(savedFilesFolder);  // show current value
				alert.setView(input);

				alert.setPositiveButton("Set filename", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String newFN = input.getText().toString();
						if (newFN == null || newFN.length() == 0)
							return;        // exit if user quit

						String wpFileMsg = "New Folder will be created";
						File testFile = new File(savedFilesFolder);
						if (testFile.exists()) {
							wpFileMsg = "Folder exists";
						}
						Toast.makeText(WF.this, "Files to be saved to: "
										+ savedFilesFolder + "\n" + wpFileMsg,
								Toast.LENGTH_LONG).show();
						// Save name for next time???
						try {
							FileOutputStream fos = openFileOutput(SavedFolderName, Context.MODE_PRIVATE);
							fos.write(savedFilesFolder.getBytes());
							fos.close();
						} catch (Exception x) {
							x.printStackTrace();
						}
					}
				});
				alert.setNeutralButton("Choose file", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Choose the file to write to
						Intent intent2 = new Intent(Intent.ACTION_GET_CONTENT);
						intent2.setType("file/*");
//	    	    		intent2.setDataAndTypeAndNormalize(Uri.parse("file://"), "*/*"); //???? too many

						//  How to get String ids below???
						intent2.putExtra("START_PATH", new File(savedFilesFolder));
						intent2.putExtra(Intent.EXTRA_TITLE, "Choose folder to save to");  //????? Where does this go
//	    	    		System.out.println("selectBtn clicked intent="+intent2);
						System.out.println("SWS selectBtnClicked to choose folder to copy to, intent=" + intent2);
						startActivityForResult(intent2, FolderChosenForSave);
						// Should the copy be done immediately after the file is chosen?
					}
				});


				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

				alert.show();

				return true;

			case R.id.action_settings:
				// Starts the Settings activity on top of the current activity
				Intent intent2 = new Intent(this, SettingsActivity.class);
				startActivityForResult(intent2, RESULT_SETTINGS);
				return true;

			case R.id.About_ID:
				showMsg("Norm's Simple Web Server program\n"
						+ Version
						+ "email: radder@hotmail.com");
				return true;


			case R.id.Exit_ID:
				finish();
				return true;

			default:
				System.err.println("SWS unkn menuitem=" + item.getItemId());
				break;

		}


		return super.onOptionsItemSelected(item);
	}

	Context mContext;

	//----------------------------------------------------------
	//  Handle what selected activity found
	@Override
	public void onActivityResult(int reqCode, int resCode, Intent data) {
		System.out.print("onActivityResult started");

		super.onActivityResult(reqCode, resCode, data);

		// Process results from started activities
		if (reqCode == RESULT_SETTINGS) {
			getPreferences();  // retrieve what user did
			return;
		}
		if (reqCode == FolderChosenForSave) {
			if (data == null) {
				System.out.println("SWS oAR data=null");
				return;
			}
			String filePath = data.getStringExtra("FileName");
			String fldr = data.getData().getPath();         // Here is the standard place for response
			System.out.println("SWS onActRes filePath=" + filePath + "< fldr=" + fldr + "<");
			//>>>>>>>>> NEEDS WORK HERE
			return;
		}



		if (reqCode == CODE_FORWARD && resCode != RESULT_CANCELED) {

			// Remember that we last picked via external app
			final String packageName = getCallingPackageMaybeExtra();
			final ContentValues values = new ContentValues();
			values.put(ResumeColumns.EXTERNAL, 1);
			getContentResolver().insert(RecentsProvider.buildResume(packageName), values);

			// Pass back result to original caller
			setResult(resCode, data);
			finish();
		} else if(reqCode == CODE_SETTINGS){
			if(reqCode == RESULT_FIRST_USER){
				recreate();
			}
		}  else if(reqCode == ADD_STORAGE_REQUEST_CODE){
			SAFManager.onActivityResult(this, reqCode, resCode, data);
		} else {
			super.onActivityResult(reqCode, resCode, data);
		}


		if (resCode != RESULT_OK)
			return;
		else {
			Uri treeUri = data.getData();
			pickedDir = DocumentFile.fromTreeUri(this, treeUri);


			grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            this.getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
SDcardpath();
		}
	}

	private String getCallingPackageMaybeExtra() {
		final String extra = getIntent().getStringExtra(DocumentsContract.EXTRA_PACKAGE_NAME);
		return (extra != null) ? extra : getCallingPackage();
	}

	public static DocumentFile pickedDir;

	//--------------------------------------------------------------
	//  Save values to allow us to restore state when rotated
	@Override
	public void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		System.out.println("***** SWS onSaveInstanceState()");


	}

	//===============================================================
	public void startBtnClicked() {

//		showMsg("Should start server now");

		if (useService) {
			runAsService();

		} else {
			File wwwroot = new File(root).getAbsoluteFile();
			boolean quiet = false;
			server = new SWS(this, host, PORT, wwwroot, quiet);

			try {
				server.start();
				runningServer = true;
				showMsg("server started - host=" + host + " port=" + PORT + "\nwwwroot=" + wwwroot
						+ "\naddPrefix=" + addFilenamePrefix + ", savedFolde=" + savedFilesFolder);
			} catch (IOException ioe) {
				System.err.println(">*>*> SWS Couldn't start server:\n" + ioe);
			}
		}
	}

	//--------------------------------------------------------
	//  Run SWS in a Service
	private void runAsService() {
		final Intent intent = new Intent(getBaseContext(), RunServerService.class);
		intent.putExtra(RunServerService.TheRootID, root);
		intent.putExtra(RunServerService.SaveFolderID, savedFilesFolder);
		resultReceiver = new MyResultReceiver(handler); //????null);
		intent.putExtra("receiver", resultReceiver);
		startService(intent);
	}

	//----------------------------------------------


	public void Internalstorage() {
		Internalpath();
	}

	private void Internalpath() {
		stopServer();
		if (this.useService) {
			runAsService();
			return;
		}
		this.root = intcard+"/";
		if(sdcard==null){
			this.root = "/storage/emulated/0/";
		}

			this.server = new SWS(this, this.host, PORT, new File(this.root).getAbsoluteFile(), false);
			try {
				this.server.start();
				this.runningServer = true;
				updateipp();
			} catch (IOException ioe) {
				System.err.println(">*>*> SWS Couldn't start server:\n" + ioe);
			}

	}

	public void Sdcard() {
		SDcardpath();
	}

	public int check = 0;

	private void SDcardpath() {
		stopServer();

		if (this.useService) {
			runAsService();
			return;
		}


			this.root = sdcard+"/"; //"/storage/";}
		if(sdcard==null){
			this.root = "/storage/";
						}

	/*	}
		}*/

			this.server = new SWS(this, this.host, PORT, new File(this.root).getAbsoluteFile(), false);
			try {
				this.server.start();
				this.runningServer = true;
				updateipp();
			} catch (IOException ioe) {
				System.err.println(">*>*> SWS Couldn't start server:\n" + ioe);
			}

	}


	public void stopBtnClicked() {
		Stoppedserver();
	}

	private void Stoppedserver() {
		stopServer();
		updateipp();
		showMessage("SWS >>> Server stopped " + isMyServiceRunning(RunServerService.class));
	}

	private void stopServer() {
		if (this.useService) {
			stopService(new Intent(this, RunServerService.class));
		} else {
			if (this.server != null) {
				this.server.stop();
			}
			this.server = null;
		}
		this.runningServer = false;
	}  // end stopServer()

	//--------------------------------------------------------------

	@Override
	public void onPause() {
		super.onPause();
		System.out.println("SWS onPause() at " + sdfTime.format(new Date()));
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		System.out.println("<<<<<<<<< SWS onDestroy stopping at " + new Date());
		if (isFinishing())
			stopServer();

	}

	//====================================================
	@Override
	public void showMessage(String message) {
		final StringBuilder buf = new StringBuilder(message);

		handler.post(new Runnable() {
			@Override
			public void run() {
				String text = hello.getText().toString();
				hello.setText(text + "\n" + buf);
				System.out.println("SWS buf=" + buf);        //<<<<<<<
			}
		});
	}

	//------------------------------------------
	//  Show a message in an Alert box
	private void showMsg(String msg) {

		AlertDialog ad = new AlertDialog.Builder(this).create();
		ad.setCancelable(false); // This blocks the 'BACK' button
		ad.setMessage(msg);
		ad.setButton(DialogInterface.BUTTON_POSITIVE, "Clear messsge", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		ad.show();
	}

//===================================================================================================


	/*private void newcopyFile(File fileInput, String outputParentPath,
							 String mimeType, String newFileName) {


		DocumentFile documentFileGoal = DocumentFile.fromTreeUri(this, treeUri);

		String[] parts = outputParentPath.split("\\/");
		for (int i = 3; i < parts.length; i++) {
			if (documentFileGoal != null) {
				documentFileGoal = documentFileGoal.findFile(parts[i]);
			}
		}
		if (documentFileGoal == null) {
			Toast.makeText(WF.this, "Directory not found", Toast.LENGTH_SHORT).show();
			return;
		}

		DocumentFile documentFileNewFile = documentFileGoal.createFile(mimeType, newFileName);

		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			outputStream = getContentResolver().openOutputStream(documentFileNewFile.getUri());
			inputStream = new FileInputStream(fileInput);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			if (outputStream != null) {
				byte[] buffer = new byte[1024];
				int read;
				if (inputStream != null) {
					while ((read = inputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, read);
					}
				}
				if (inputStream != null) {
					inputStream.close();
				}
				inputStream = null;
				outputStream.flush();
				outputStream.close();
				outputStream = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
*/


	//=======================================================================================0

	private static final String EXTRA_STATE = "state";
	private static final String EXTRA_AUTHENTICATED = "authenticated";
	private static final String EXTRA_ACTIONMODE = "actionmode";
	private static final String EXTRA_SEARCH_STATE = "searchsate";

	private static final int CODE_FORWARD = 42;
	private static final int CODE_SETTINGS = 92;


	private SearchView mSearchView;

	private Toolbar mToolbar;
	private Spinner mToolbarStack;

	private ActionBarDrawerToggle mDrawerToggle;
	private View mRootsContainer;
	private View mInfoContainer;


	private boolean mIgnoreNextNavigation;
	private boolean mIgnoreNextClose;
	private boolean mIgnoreNextCollapse;

	private boolean mSearchExpanded;
	private boolean mSearchResultShown;

	private RootsCache mRoots;
	private BaseActivity.State mState;
	private boolean mAuthenticated;
	private FrameLayout mRateContainer;
	private boolean mActionMode;
	private RootInfo mParentRoot;

	
	/*@Override
	public void onCreate(Bundle icicle) {
		setTheme(R.style.Theme_Document);
		if(Utils.hasLollipop()){
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		}
		else if(Utils.hasKitKat()){
			setTheme(R.style.Theme_Document_Translucent);
		}
		setUpStatusBar();

*//*		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
				.penaltyLog()
				.build());
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll()
				.penaltyLog()
				.build());
*//*
		super.onCreate(icicle);

		mRoots = DocumentsApplication.getRootsCache(this);

		setResult(Activity.RESULT_CANCELED);
		setContentView(R.layout.activity);

		final Context context = this;
		final Resources res = getResources();
		mShowAsDialog = res.getBoolean(R.bool.show_as_dialog);

		mDirectoryContainer = (DirectoryContainerView) findViewById(R.id.container_directory);
		mRateContainer = (FrameLayout) findViewById(R.id.container_rate);

		initControls();

		if (icicle != null) {
			mState = icicle.getParcelable(EXTRA_STATE);
			mAuthenticated = icicle.getBoolean(EXTRA_AUTHENTICATED);
			mActionMode = icicle.getBoolean(EXTRA_ACTIONMODE);
		} else {
			buildDefaultState();
		}

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setTitleTextAppearance(context, R.style.TextAppearance_AppCompat_Widget_ActionBar_Title);
		if(Utils.hasKitKat() && !Utils.hasLollipop()) {
			((LinearLayout.LayoutParams) mToolbar.getLayoutParams()).setMargins(0, getStatusBarHeight(this), 0, 0);
			mToolbar.setPadding(0, getStatusBarHeight(this), 0, 0);
		}


		mToolbarStack = (Spinner) findViewById(R.id.stack);
		mToolbarStack.setOnItemSelectedListener(mStackListener);

		setSupportActionBar(mToolbar);

		mRootsContainer = findViewById(R.id.drawer_roots);
		mInfoContainer = findViewById(R.id.container_info);

		if (!mShowAsDialog) {
			// Non-dialog means we have a drawer
			mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
			mDrawerLayout.setDrawerListener(mDrawerListener);
			//mDrawerLayout.setDrawerShadow(R.drawable.ic_drawer_shadow, GravityCompat.START);
			lockInfoContainter();
		}

		changeActionBarColor();
		initProtection();

		// Hide roots when we're managing a specific root
		if (mState.action == ACTION_MANAGE) {
			if (mShowAsDialog) {
				findViewById(R.id.container_roots).setVisibility(View.GONE);
			} else {
				mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			}
		}

		if (mState.action == ACTION_CREATE) {
			final String mimeType = getIntent().getType();
			final String title = getIntent().getStringExtra(IntentUtils.EXTRA_TITLE);
			SaveFragment.show(getFragmentManager(), mimeType, title);
		} else if (mState.action == ACTION_OPEN_TREE) {
			PickFragment.show(getFragmentManager());
		}

		if (mState.action == ACTION_BROWSE) {
			final Intent moreApps = new Intent(getIntent());
			moreApps.setComponent(null);
			moreApps.setPackage(null);
			RootsFragment.show(getFragmentManager(), moreApps);
		} else if (mState.action == ACTION_OPEN || mState.action == ACTION_CREATE
				|| mState.action == ACTION_GET_CONTENT || mState.action == ACTION_OPEN_TREE) {
			RootsFragment.show(getFragmentManager(), new Intent());
		}

		if (!mState.restored) {
			if (mState.action == ACTION_MANAGE) {
				final Uri rootUri = getIntent().getData();
				new WF.RestoreRootTask(rootUri).executeOnExecutor(getCurrentExecutor());
			} else {
				if(isDownloadAuthority(getIntent())){
					onRootPicked(getDownloadRoot(), true);
				} else if(ConnectionUtils.isServerAuthority(getIntent())){
					RootInfo root = getIntent().getExtras().getParcelable(EXTRA_ROOT);
					onRootPicked(root, true);
				} else if(Utils.isQSTile(getIntent())){
					NetworkConnection networkConnection = NetworkConnection.getDefaultServer(this);
					RootInfo root = mRoots.getRootInfo(networkConnection);
					onRootPicked(root, true);
				} else{
					try {
						new WF.RestoreStackTask().execute();
					}
					catch (SQLiteFullException e){
						CrashReportingManager.logException(e);
					}
				}
			}
		} else {
			onCurrentDirectoryChanged(ANIM_NONE);
		}

		if(!PermissionUtil.hasStoragePermission(this)) {
			requestStoragePermissions();
		}
	}*/






	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public int getGravity() {
		if(Utils.hasJellyBeanMR1()){
			Configuration config = getResources().getConfiguration();
			if(config.getLayoutDirection() != View.LAYOUT_DIRECTION_LTR){
				return Gravity.LEFT;
			}
		}
		return Gravity.RIGHT;
	}



















		// Open drawer means we hide most actions







	/**
	 * Set state mode based on explicit user action.
	 */


	/**
	 * refresh Data currently shown
	 */









































	public static WF get(Fragment fragment) {
		return (WF) fragment.getActivity();
	}

	private Drawable oldBackground;

















	/*public static boolean copy(File copy, String directory, Context con) {
		FileInputStream inStream = null;
		OutputStream outStream = null;
		DocumentFile dir= getDocumentFileIfAllowedToWrite(new File(directory), con);
		String mime = mime(copy.toURI().toString());
		DocumentFile copy1= dir.createFile(mime, copy.getName());
		try {
			inStream = new FileInputStream(copy);
			outStream =
					con.getContentResolver().openOutputStream(copy1.getUri());
			byte[] buffer = new byte[16384];
			int bytesRead;
			while ((bytesRead = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, bytesRead);

			}


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {

				inStream.close();

				outStream.close();


				return true;


			} catch (IOException e) {
				e.printStackTrace();
			}


		}

		return false;
	}



	public static DocumentFile getDocumentFileIfAllowedToWrite(File file, Context con) {

		List<UriPermission> permissionUris = con.getContentResolver().getPersistedUriPermissions();

		for (UriPermission permissionUri : permissionUris) {

			Uri treeUri = permissionUri.getUri();
			DocumentFile rootDocFile = DocumentFile.fromTreeUri(con, treeUri);
			String rootDocFilePath = FileUtil.getFullPathFromTreeUri(treeUri, con);

			if (file.getAbsolutePath().startsWith(rootDocFilePath)) {

				ArrayList<String> pathInRootDocParts = new ArrayList<String>();
				while (!rootDocFilePath.equals(file.getAbsolutePath())) {
					pathInRootDocParts.add(file.getName());
					file = file.getParentFile();
				}

				DocumentFile docFile = null;

				if (pathInRootDocParts.size() == 0) {
					docFile = DocumentFile.fromTreeUri(con, rootDocFile.getUri());
				} else {
					for (int i = pathInRootDocParts.size() - 1; i >= 0; i--) {
						if (docFile == null) {
							docFile = rootDocFile.findFile(pathInRootDocParts.get(i));
						} else {
							docFile = docFile.findFile(pathInRootDocParts.get(i));
						}
					}
				}
				if (docFile != null && docFile.canWrite()) {
					return docFile;
				} else {
					return null;
				}

			}
		}
		return null;
	}

	public static String mime(String URI) {
		String type="";
		String extention = MimeTypeMap.getFileExtensionFromUrl(URI);
		if (extention != null) {
			type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extention);
		}
		return type;
	}*/


	public static boolean copy(File copy, String directory, Context con) {

		DocumentFile dir= getDocumentFileIfAllowedToWrite(new File(directory), con);
		String mime = mime(copy.toURI().toString());
		DocumentFile copy1= dir.createFile(mime, copy.getName());
		try {
			inStream = new FileInputStream(copy);
			outStream =
					con.getContentResolver().openOutputStream(copy1.getUri());
			byte[] buffer = new byte[16384];
			int bytesRead;
			while ((bytesRead = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, bytesRead);

			}


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {

				inStream.close();

				outStream.close();


				return true;


			} catch (IOException e) {
				e.printStackTrace();
			}


		}

		return false;
	}



    public static DocumentFile getDocumentFileIfAllowedToWrite(File file, Context con) {

		List<UriPermission> permissionUris = con.getContentResolver().getPersistedUriPermissions();

		for (UriPermission permissionUri : permissionUris) {

			Uri treeUri = permissionUri.getUri();
			DocumentFile rootDocFile = DocumentFile.fromTreeUri(con, treeUri);
			String rootDocFilePath = FileUtil.getFullPathFromTreeUri(treeUri, con);

			if (file.getAbsolutePath().startsWith(rootDocFilePath)) {

				ArrayList<String> pathInRootDocParts = new ArrayList<String>();
				while (!rootDocFilePath.equals(file.getAbsolutePath())) {
					pathInRootDocParts.add(file.getName());
					file = file.getParentFile();
				}

				DocumentFile docFile = null;

				if (pathInRootDocParts.size() == 0) {
					docFile = DocumentFile.fromTreeUri(con, rootDocFile.getUri());
				} else {
					for (int i = pathInRootDocParts.size() - 1; i >= 0; i--) {
						if (docFile == null) {
							docFile = rootDocFile.findFile(pathInRootDocParts.get(i));
						} else {
							docFile = docFile.findFile(pathInRootDocParts.get(i));
						}
					}
				}
				if (docFile != null && docFile.canWrite()) {
					return docFile;
				} else {
					return null;
				}

			}
		}
		return null;
	}


	public static FileInputStream inStream = null;
	public static OutputStream outStream = null;
	public static String type="";
    public static String mime(String URI) {
        type="";
        String extention = MimeTypeMap.getFileExtensionFromUrl(URI);
        if (extention != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extention);
        }
        return type;
    }

   // ===========================================================
    //=============================================================

    @SuppressWarnings("null")
    public static boolean copyFile(final File source, final File target,
                                   Context context) {
        FileInputStream inStream = null;
        OutputStream outStream = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inStream = new FileInputStream(source);

            // First try the normal way
            if (isWritable(target)) {
                // standard way
                outStream = new FileOutputStream(target);
                inChannel = inStream.getChannel();
                outChannel = ((FileOutputStream) outStream).getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                    // Workaround for Kitkat ext SD card
                    Uri uri = MediaStoreHack.getUriFromFile(
                            target.getAbsolutePath(), context);
                    outStream = context.getContentResolver()
                            .openOutputStream(uri);
                } else {
                    return false;
                }

                if (outStream != null) {
                    // Both for SAF and for Kitkat, write to output stream.
                    byte[] buffer = new byte[16384]; // MAGIC_NUMBER
                    int bytesRead;
                    while ((bytesRead = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }
                }

            }
        } catch (Exception e) {
            Log.e("AmazeFileUtils",
                    "Error when copying file from "
                            + source.getAbsolutePath() + " to "
                            + target.getAbsolutePath(), e);
            return false;
        } finally {
            try {
                inStream.close();
            } catch (Exception e) {
                // ignore exception
            }
            try {
                outStream.close();
            } catch (Exception e) {
                // ignore exception
            }
            try {
                inChannel.close();
            } catch (Exception e) {
                // ignore exception
            }
            try {
                outChannel.close();
            } catch (Exception e) {
                // ignore exception
            }
        }
        return true;
    }




    public static final boolean isWritable(final File file) {
        if (file == null)
            return false;
        boolean isExisting = file.exists();

        try {
            FileOutputStream output = new FileOutputStream(file, true);
            try {
                output.close();
            } catch (IOException e) {
                // do nothing.
            }
        } catch (FileNotFoundException e) {
            return false;
        }
        boolean result = file.canWrite();

        // Ensure that file is not created during this process.
        if (!isExisting) {
            file.delete();
        }

        return result;
    }



	private static final Pattern DIR_SEPORATOR = Pattern.compile("/");

	/**
	 * Raturns all available SD-Cards in the system (include emulated)
	 *
	 * Warning: Hack! Based on Android source code of version 4.3 (API 18)
	 * Because there is no standart way to get it.
	 * TODO: Test on future Android versions 4.4+
	 *
	 * @return paths to all available SD-Cards in the system (include emulated)
	 */
	/* returns external storage paths (directory of external memory card) as array of Strings */
	public String[] getExternalStorageDirectories() {

		List<String> results = new ArrayList<>();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //Method 1 for KitKat & above
			File[] externalDirs = getExternalFilesDirs(null);

			for (File file : externalDirs) {
				String path = file.getPath().split("/Android")[0];

				boolean addPath = false;

				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					addPath = Environment.isExternalStorageRemovable(file);
				}
				else{
					addPath = Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(file));
				}

				if(addPath){
					results.add(path);
				}
			}
		}

		if(results.isEmpty()) { //Method 2 for all versions
			// better variation of: http://stackoverflow.com/a/40123073/5002496
			String output = "";
			try {
				final Process process = new ProcessBuilder().command("mount | grep /dev/block/vold")
						.redirectErrorStream(true).start();
				process.waitFor();
				final InputStream is = process.getInputStream();
				final byte[] buffer = new byte[1024];
				while (is.read(buffer) != -1) {
					output = output + new String(buffer);
				}
				is.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
			if(!output.trim().isEmpty()) {
				String devicePoints[] = output.split("\n");
				for(String voldPoint: devicePoints) {
					results.add(voldPoint.split(" ")[2]);
				}
			}
		}

		//Below few lines is to remove paths which may not be external memory card, like OTG (feel free to comment them out)
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			for (int i = 0; i < results.size(); i++) {
				if (!results.get(i).toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
					Log.d(TAG, results.get(i) + "TOG might not be extSDcard");
					results.remove(i--);
				}
			}
		} else {
			for (int i = 0; i < results.size(); i++) {
				if (!results.get(i).toLowerCase().contains("ext") && !results.get(i).toLowerCase().contains("sdcard")) {
					Log.d(TAG, results.get(i)+" might not be extSDcard");
					results.remove(i--);
				}
			}
		}

		String[] storageDirectories = new String[results.size()];
		for(int i=0; i<results.size(); ++i) storageDirectories[i] = results.get(i);
		Log.d(TAG, storageDirectories + "TOG storageDirectories");

		return storageDirectories;
	}

	final  static HashSet<String> out= new HashSet<String>();

	public static HashSet<String> getExternalMounts() {
		//final  HashSet<String> out = new HashSet<String>();
		String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
		String s = "";
		try {
			final Process process = new ProcessBuilder().command("mount")
					.redirectErrorStream(true).start();
			process.waitFor();
			final InputStream is = process.getInputStream();
			final byte[] buffer = new byte[1024];
			while (is.read(buffer) != -1) {
				s = s + new String(buffer);
			}
			is.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}

		// parse output
		final String[] lines = s.split("\n");
		for (String line : lines) {
			if (!line.toLowerCase(Locale.US).contains("asec")) {
				if (line.matches(reg)) {
					String[] parts = line.split(" ");
					for (String part : parts) {
						if (part.startsWith("/"))
							if (!part.toLowerCase(Locale.US).contains("vold"))
								out.add(part);
					}
				}
			}
		}
		Log.d(TAG, out+" TOG out out");

		return out;
	}


}
