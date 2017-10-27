/*
    *
    * Created by Krishna.R.K on 10/25/2017.
    *
*/
var express = require('express');
var router = express.Router(),
    db         = require('./connectdb')(),
    formidable = require('formidable'),
    fs         = require('fs'),
    path       = require('path');

var vision = require('@google-cloud/vision')
var gcloud = vision({
    projectId: 'storeup-164304',

    // The path to your key file:
    keyFilename: 'StoreUp-6dfe8732b53c.json'
});


router.post('/getImageOcr',function (res,req,next) {
    //console.log(req.body.data);
    /*var type = vision.v1.types.Feature.Type.TEXT_DETECTION;
    var featuresElement = { type:type};
    var features = [featuresElement];

    var gcsImageUri = 'gs://storeup-7952a.appspot.com/images/image%3A30.jpg';
    var source = {
        gcsImageUri : gcsImageUri
    };
    var image = {
        content: fs.readFileSync('unnamed.jpg').toString('base64')
    };
    var requestsElement = {
        image : image,
        features : features
    };

    var requests = [requestsElement];

    gcloud.batchAnnotateImages({requests: requests}).then(function(results) {
        var response = results[0].responses[0].fullTextAnnotation;

        console.log(response);
        // doThingsWith(response)
    }).catch(function(err) {
            console.error(err);
        });*/
     var type = vision.v1.types.Feature.Type.TEXT_DETECTION;
     var typeLogo = vision.v1.types.Feature.Type.LOGO_DETECTION;
     var featuresElement = { type:type};
     var featuresElementLogo = { type:typeLogo};
     var features = [featuresElement,featuresElementLogo];

     var gcsImageUri = 'gs://storeup-7952a.appspot.com/images/image%3A30.jpg';
     var source = {
     gcsImageUri : gcsImageUri
     };
     var image = {
     content: fs.readFileSync('unnamed.jpg').toString('base64')
     };
     var requestsElement = {
     image : image,
     features : features
     };

     var requests = [requestsElement];

     gcloud.batchAnnotateImages({requests: requests}).then(function(results) {
         var text = results[0].responses[0].fullTextAnnotation.text;
         var logo = results[0].responses[0].logoAnnotations[0].description;
         var address="";
         console.log("Logo is: \n"+logo);
         if(logo=="Target" || logo=="Target Corporation"){
             var count=0;
             for(var i=0;i<text.length;i++){
                 if(count>1){
                     break;
                 }
                 if(count==1){
                     address+=text.charAt(i);
                 }
                 if(text.charAt(i)=='\n'){
                     count++;
                 }
             }
             console.log(address);
         }
         /*console.log("Logo is: \n"+logo);
         console.log("Text is: \n"+text);*/

         //fooProcess(data, successFooProcess, failureFooProcess);
         //console.log();

         //console.log(response);
     // doThingsWith(response)
     }).catch(function(err) {
     console.error(err);
     });
});

/*function successFooProcess(result) {
    // do something
}

function failureFooProcess(error) {
    // do something
}

function fooProcess(data, success, failure) {
    if (true) {
        var result = "";
        success(result);
    }
    else {
        var error = "";
        failure(error);
    }
}*/
module.exports = router;
