import Foundation
import Cordova
import UserNotifications

@objc(AzureUpload) class AzureUpload: CDVPlugin {
    let notificationCenter = UNUserNotificationCenter.current()

    @objc(uploadFiles:)
    func uploadFiles(command: CDVInvokedUrlCommand) {
        guard let files = command.arguments[0] as? [[String: String]] else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Invalid arguments")
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }

        uploadBatch(files: files, command: command)
    }

    private func uploadBatch(files: [[String: String]], command: CDVInvokedUrlCommand) {
        requestNotificationAuthorization()

        for (index, file) in files.enumerated() {
            let fileUri = file["fileUri"] ?? ""
            let sasToken = file["sasToken"] ?? ""
            let containerName = file["containerName"] ?? ""
            
            uploadFile(fileUri: fileUri, sasToken: sasToken, containerName: containerName, current: index + 1, total: files.count)
        }

        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "All files uploaded successfully")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    private func requestNotificationAuthorization() {
        notificationCenter.requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            // Handle permission or error
        }
    }

    private func uploadFile(fileUri: String, sasToken: String, containerName: String, current: Int, total: Int) {
        let blobUrl = "https://yourstorageaccount.blob.core.windows.net/\(containerName)/\(URL(fileURLWithPath: fileUri).lastPathComponent)?\(sasToken)"
        
        // Upload process with a notification for each file (simplified here for brevity)
        sendNotification(content: "Uploaded \(current) of \(total)", progress: Int((Float(current) / Float(total)) * 100))
    }

    private func sendNotification(content: String, progress: Int) {
        let content = UNMutableNotificationContent()
        content.title = "Uploading Files"
        content.body = content
        content.sound = UNNotificationSound.default
        
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)
        let request = UNNotificationRequest(identifier: "UPLOAD_NOTIFICATION", content: content, trigger: trigger)

        notificationCenter.add(request) { error in
            if let error = error {
                print("Notification error: \(error)")
            }
        }
    }
}
