var exec = require('cordova/exec');

var AzureUpload = {
    uploadFiles: function(files, success, error) {
        exec(success, error, "AzureUpload", "uploadFiles", [files]);
    }
};

module.exports = AzureUpload;
