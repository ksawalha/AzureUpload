var exec = require('cordova/exec');

var AzureUpload = {
    uploadFile: function(fileUri, sasToken, containerName, success, error) {
        exec(success, error, "AzureUpload", "uploadFile", [fileUri, sasToken, containerName]);
    }
};

module.exports = AzureUpload;
