package com.yourpackage.azureupload;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import androidx.core.app.NotificationCompat;
import android.net.Uri;

import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.CloudStorageAccount;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

public class AzureUpload extends CordovaPlugin {
    private NotificationManager notificationManager;
    private static final String CHANNEL_ID = "UPLOAD_CHANNEL";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("uploadFiles")) {
            JSONArray files = args.getJSONArray(0);
            this.uploadFiles(files, callbackContext);
            return true;
        }
        return false;
    }

    private void uploadFiles(JSONArray files, CallbackContext callbackContext) {
        try {
            setupNotificationChannel();

            for (int i = 0; i < files.length(); i++) {
                JSONObject fileObj = files.getJSONObject(i);
                String fileUri = fileObj.getString("fileUri");
                String sasToken = fileObj.getString("sasToken");
                String containerName = fileObj.getString("containerName");

                uploadFile(fileUri, sasToken, containerName, i + 1, files.length());
            }

            callbackContext.success("All files uploaded successfully");
        } catch (Exception e) {
            callbackContext.error("Upload failed: " + e.getMessage());
        }
    }

    private void setupNotificationChannel() {
        notificationManager = (NotificationManager) cordova.getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "File Upload", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void uploadFile(String fileUri, String sasToken, String containerName, int current, int total) {
        try {
            String storageConnectionString = "https://yourstorageaccount.blob.core.windows.net/" + containerName + sasToken;
            CloudBlobContainer container = new CloudBlobContainer(new URI(storageConnectionString));
            CloudBlockBlob blob = container.getBlockBlobReference(new File(fileUri).getName());
            File source = new File(Uri.parse(fileUri).getPath());
            blob.upload(new FileInputStream(source), source.length());

            updateNotification("Uploaded " + current + " of " + total, (int)((current / (float)total) * 100));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateNotification(String content, int progress) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(cordova.getActivity(), CHANNEL_ID)
                .setContentTitle("Uploading Files")
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_menu_upload)
                .setProgress(100, progress, false);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
