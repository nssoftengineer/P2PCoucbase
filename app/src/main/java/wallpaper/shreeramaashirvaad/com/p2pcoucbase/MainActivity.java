package wallpaper.shreeramaashirvaad.com.p2pcoucbase;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.DocumentChange;
import com.couchbase.lite.Manager;
import com.couchbase.lite.listener.LiteListener;
import com.couchbase.lite.replicator.Replication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private Manager manager;
    private Database database;
    private LiteListener listener;
    private TextView textViewMessage, textViewTitle;
    private CBL cbl;
    String deviceIP;
    MulticastSocket socket;
    WifiManager.MulticastLock multicastLock;
    FloatingActionButton floatingActionButton;
    ListView mListView;
    ArrayList<DeviceInfo> infoArrayList;
    MultiSelectionAdapter<DeviceInfo> multiSelectionAdapter;
    JSONObject jsonObject = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewMessage = (TextView) findViewById(R.id.textViewMessage);
        textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (multiSelectionAdapter != null) {
                    ArrayList<DeviceInfo> mArrayProducts = multiSelectionAdapter.getCheckedItems();
                    ArrayList<String> documentIds = createDocuments("Message From:" + deviceIP);
                    for (int i = 0; i < mArrayProducts.size(); i++) {

                        replication(documentIds, mArrayProducts.get(i).getDeviceip());
                    }
                }

            }
        });
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermission();
        }
        deviceIP = Utility.getLocalIpAddress(MainActivity.this);

        textViewMessage = (TextView) findViewById(R.id.textViewMessage);
        cbl = (CBL) getApplication();
        database = cbl.getDatabase();
        try {
            manager = cbl.getManager();
        } catch (Exception e) {
            e.printStackTrace();
        }

        startListener();
        startObserveDatabaseChange();
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        multicastLock = wifi.createMulticastLock("multicastLock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();

        Thread t = new Thread(new SocketListener());
        t.start();
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                String possibleEmail = account.name;
                String temp;

                temp = possibleEmail.substring(0, possibleEmail.indexOf("@"));
                try {
                    textViewTitle.setText(temp);
                    jsonObject.put(Constant.USERNAME, temp);
                    break;
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }

        try {
            jsonObject.put(Constant.DEVICE_IP, deviceIP);
            jsonObject.put(Constant.MOBILE, "8115255300");
            jsonObject.put(Constant.STATUS, "1");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    new messageSender().execute(jsonObject);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        bindComponents();
        init();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    void startObserveDatabaseChange() {
        database.addChangeListener(new Database.ChangeListener() {
            @Override
            public void changed(Database.ChangeEvent event) {
                List<DocumentChange> changes = event.getChanges();

                for (DocumentChange change : changes) {
                    System.out.println("Id of the changing doc " + change.getDocumentId());
                    saveImageFromDocument(change.getDocumentId());
                }
            }
        });
    }

    void saveImageFromDocument(String name) {
        final Document document = database.getExistingDocument(name);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewMessage.setText(document.getProperties().toString());
            }
        });


    }

    void startListener() {
        // Credentials credentials = new Credentials("hello", "pw123");
        listener = new LiteListener(manager, Constant.LOCALPORT, null);
        Thread thread = new Thread(listener);
        thread.start();

    }

    private ArrayList<String> createDocuments(String message) {
        ArrayList<String> documentIds = new ArrayList();
        Document document = database.createDocument();
        Map<String, Object> hsmap = new HashMap<>();
        hsmap.put("message", message);
        try {
            document.putProperties(hsmap);
            documentIds.add(0, document.getId());
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        return documentIds;
    }

    void replication(ArrayList<String> documentIds, String ip) {
        // for loop to get the image
        URL urlNew = null;
        try {
            urlNew = new URL("http", ip, Constant.LOCALPORT, "/" + cbl.getDatabase().getName());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        System.out.println("The documents IDs are " + documentIds.toString());

        if (documentIds.size() > 0) {
            Replication push = database.createPushReplication(urlNew);
            push.setDocIds(documentIds);
            push.setContinuous(false);
            push.start();
        }


    }

    public class messageSender extends AsyncTask<JSONObject, String, JSONObject> {
        protected JSONObject doInBackground(JSONObject... message) {

            try {

                byte[] buf;
                if (message[0] != null) {
                    buf = message[0].toString().getBytes();
                    InetAddress address = InetAddress.getByName(Constant.BROADCAST_IP);
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, Constant.BROADCAST_PORT);
                    socket.send(packet);
                }


            } catch (SocketException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (UnknownHostException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            } catch (IOException e3) {
                // TODO Auto-generated catch block
                e3.printStackTrace();
            }

            return message[0];
        }
    }

    class SocketListener implements Runnable {
        public void run() {
            try {
                InetAddress sessAddr = InetAddress.getByName(Constant.BROADCAST_IP);
                socket = new MulticastSocket(Constant.BROADCAST_PORT);
                socket.joinGroup(sessAddr);

                while (true) {
                    DatagramPacket recpacket;
                    byte[] recbuf = new byte[256];

                    recpacket = new DatagramPacket(recbuf, recbuf.length);
                    socket.receive(recpacket);
                    System.out.println("Received packet");
                    String s = new String(recpacket.getData());
                    JSONObject jsonObj = null;
                    try {
                        jsonObj = new JSONObject(s);
                        if (!jsonObj.get(Constant.DEVICE_IP).equals(deviceIP)) {
                            System.out.println("Received packet IP: " + jsonObj.get(Constant.DEVICE_IP) + " Name: " + jsonObj.get(Constant.USERNAME));
                            if (jsonObj.get(Constant.DEVICE_IP).equals(Constant.ZERO_IP)) {
                                continue;
                            }
                            DeviceInfo deviceInfo = new DeviceInfo(jsonObj.get(Constant.DEVICE_IP).toString(), jsonObj.get(Constant.MOBILE).toString(), jsonObj.get(Constant.USERNAME).toString(), jsonObj.get(Constant.STATUS).toString());
                            boolean isExist = false;
                            for (int i = 0; i < infoArrayList.size(); i++) {
                                if (infoArrayList.get(i).getDeviceip().equals(deviceInfo.getDeviceip())) {
                                    isExist = true;
                                    break;
                                } else {
                                    isExist = false;
                                }

                            }

                            if (!isExist)
                                infoArrayList.add(deviceInfo);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    multiSelectionAdapter.notifyDataSetChanged();
                                }
                            });

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            } catch (IOException e) {
                Log.e(getClass().getName(), e.getMessage());
            }
        }
    }

    private void bindComponents() {
        mListView = (ListView) findViewById(android.R.id.list);
    }

    private void init() {
        infoArrayList = new ArrayList<DeviceInfo>();
        multiSelectionAdapter = new MultiSelectionAdapter<DeviceInfo>(this, infoArrayList);
        mListView.setAdapter(multiSelectionAdapter);


    }


    private void requestPermission() {
        int getAccountAccess = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getAccountAccess = checkSelfPermission(Manifest.permission.GET_ACCOUNTS);
        }

        List<String> permissions = new ArrayList<String>();

        if (getAccountAccess != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.GET_ACCOUNTS);
        }

        if (!permissions.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), Constant.PERMISSIONS);
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            jsonObject.put("status", "0");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    boolean permissionState;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constant.PERMISSIONS: {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d("Permissions", "Permission Granted: " + permissions[i]);
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        if (!permissionState)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermission();
                                boolean showRationale = shouldShowRequestPermissionRationale(permissions[i]);

                                if (!showRationale) {
                                    permissionState = true;
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                            MainActivity.this);

                                    alertDialogBuilder.setTitle("Permission Deny");
                                    alertDialogBuilder.setMessage("Application unable to run while " + permissions[i] + " permission deny! Open app setting and enable permission.");

                                    // set dialog message
                                    alertDialogBuilder
                                            .setCancelable(false)
                                            .setPositiveButton("Setting",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            dialog.cancel();
                                                            Intent intent = new Intent();
                                                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                                                            intent.setData(uri);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    });


                                    // create alert dialog
                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                    // show it
                                    alertDialog.show();
                                }
                            }
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }
}
